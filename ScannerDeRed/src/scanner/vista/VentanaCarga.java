package scanner.vista;

import scanner.modelo.ResultadoEscaneo;
import scanner.modelo.Metodos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VentanaCarga extends JFrame {

    private JProgressBar barra;
    private JLabel texto1;
    private JLabel textoTiempo;    
    private JLabel textoCont;
    private JButton botonResultados;
    private JButton botonDetener;

    private int Di, Ci, Bi, Ai;
    private int Df, Cf, Bf, Af;
    private long cantIps;
    private int tiempoMax;
    private int cantPings;

    private final ArrayList<ResultadoEscaneo> arrayDatos = new ArrayList<>();
    private volatile boolean escaneoDetenido = false;

    private ExecutorService executor;

    public VentanaCarga(long cantIps, String IpInicio, String IpFinal, int tiempoMax, int cantPings, boolean modoOscuro) {
        this.cantIps = cantIps;
        this.tiempoMax = tiempoMax;
        this.cantPings = cantPings;

        String[] ini = IpInicio.split("\\.");
        Di = Integer.parseInt(ini[0]);
        Ci = Integer.parseInt(ini[1]);
        Bi = Integer.parseInt(ini[2]);
        Ai = Integer.parseInt(ini[3]);

        String[] fin = IpFinal.split("\\.");
        Df = Integer.parseInt(fin[0]);
        Cf = Integer.parseInt(fin[1]);
        Bf = Integer.parseInt(fin[2]);
        Af = Integer.parseInt(fin[3]);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Scanner de red");
        setSize(550, 260);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/lupa.png")).getImage());
        
        
        // Colores según modo
        Color bg = modoOscuro ? new Color(45, 45, 45) : new Color(250, 252, 254);
        Color fg = modoOscuro ? Color.WHITE : Color.BLACK;
        Color barraFg = modoOscuro ? new Color(100, 180, 255) : new Color(46, 134, 222);
        Color botonBg = new Color(46, 134, 222);
        Color botonDetBg = new Color(200, 50, 50);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(bg);
        panelPrincipal.setBorder(new EmptyBorder(20, 25, 20, 25));
        add(panelPrincipal);

        // Panel de texto
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setBackground(bg);

        JLabel titulo = new JLabel("Escaneo de IPs en progreso");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(fg);

        texto1 = new JLabel("Pasando por la IP: ");
        texto1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        texto1.setForeground(fg);

        textoTiempo = new JLabel("Tiempo: -");
        textoTiempo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textoTiempo.setForeground(fg);

        textoCont = new JLabel("IPs escaneadas: 0");
        textoCont.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textoCont.setForeground(fg);

        panelTextos.add(titulo);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 8)));
        panelTextos.add(texto1);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTextos.add(textoTiempo);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 6)));
        panelTextos.add(textoCont);
        panelPrincipal.add(panelTextos, BorderLayout.NORTH);

        // Barra de progreso
        barra = new JProgressBar(0, 100);
        barra.setValue(0);
        barra.setStringPainted(true);
        barra.setForeground(barraFg);
        barra.setBackground(modoOscuro ? new Color(70, 70, 70) : new Color(235, 235, 235));
        barra.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barra.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        barra.setIndeterminate(cantIps <= 0);

        JPanel panelBarra = new JPanel(new BorderLayout());
        panelBarra.setBackground(bg);
        panelBarra.add(barra, BorderLayout.CENTER);
        panelPrincipal.add(panelBarra, BorderLayout.CENTER);

        // Botones
        botonResultados = new JButton("Ver resultados");
        botonResultados.setVisible(false);
        botonResultados.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botonResultados.setBackground(botonBg);
        botonResultados.setForeground(Color.WHITE);
        botonResultados.setFocusPainted(false);
        botonResultados.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonResultados.addActionListener(e -> {
            new VentanaResultados(arrayDatos, modoOscuro).setVisible(true);
            dispose();
        });

        botonDetener = new JButton("Detener escaneo");
        botonDetener.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botonDetener.setBackground(botonDetBg);
        botonDetener.setForeground(Color.WHITE);
        botonDetener.setFocusPainted(false);
        botonDetener.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonDetener.addActionListener(e -> detenerEscaneo());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        panelBotones.setBackground(bg);
        panelBotones.add(botonResultados);
        panelBotones.add(botonDetener);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        escaneoIps();
    }

    private void escaneoIps() {
        new Thread(() -> {
            ArrayList<String> listaIps = new ArrayList<>();
            int d = Di, c = Ci, b = Bi, a = Ai;
            while (true) {
                listaIps.add(String.format("%d.%d.%d.%d", d, c, b, a));
                if (d == Df && c == Cf && b == Bf && a == Af) break;
                a++; if (a > 255) { a = 0; b++; }
                if (b > 255) { b = 0; c++; }
                if (c > 255) { c = 0; d++; }
            }

            final int total = listaIps.size();
            int poolSize = Math.min(200, Math.max(4, Runtime.getRuntime().availableProcessors() * 10));
            executor = Executors.newFixedThreadPool(poolSize);
            CompletionService<ResultadoEscaneo> ecs = new ExecutorCompletionService<>(executor);
            AtomicInteger doneCount = new AtomicInteger(0);

            for (String ip : listaIps) {
                ecs.submit(() -> {
                    if (escaneoDetenido) return null;
                    ResultadoEscaneo r = new ResultadoEscaneo(ip, false, "", "-", -1, "", "");
                    Metodos.hacerPingOptimizado(ip, r, tiempoMax, cantPings);
                    Metodos.lookupReverseDnsCached(ip, r);
                    r.setFechaEscaneo(java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    return r;
                });
            }

            try {
                for (int i = 0; i < total; i++) {
                    if (escaneoDetenido) break;
                    Future<ResultadoEscaneo> f = ecs.take();
                    ResultadoEscaneo res = f.get();
                    if (res == null) continue;

                    arrayDatos.add(res);
                    int current = doneCount.incrementAndGet();

                    SwingUtilities.invokeLater(() -> {
                        texto1.setText("Escaneando la IP: " + res.getIp() + " ...");
                        textoTiempo.setText("Tiempo: " + (res.getTiempoRespuesta() == null ? "-" : res.getTiempoRespuesta()));
                        textoCont.setText("IPs escaneadas: " + current);
                        if (cantIps > 0) {
                            int valor = (int) Math.min(100, Math.round((current * 100.0) / cantIps));
                            barra.setIndeterminate(false);
                            barra.setValue(valor);
                        } else barra.setIndeterminate(true);
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
            finally { if (executor != null) executor.shutdownNow(); }

            SwingUtilities.invokeLater(() -> {
                barra.setIndeterminate(false);
                barra.setValue(escaneoDetenido ? 0 : 100);
                texto1.setText(escaneoDetenido ? "Escaneo detenido." : "Escaneo completo.");
                botonResultados.setVisible(true);
                textoCont.setText("IPs escaneadas: " + arrayDatos.size());
                textoTiempo.setText("Tiempo: -");
                if (escaneoDetenido) System.out.println("--- ESCANEO DETENIDO ---");
            });
        }).start();
    }

    private void detenerEscaneo() {
        escaneoDetenido = true;
        if (executor != null) executor.shutdownNow();
    }
}
