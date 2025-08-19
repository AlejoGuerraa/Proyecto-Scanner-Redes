package scanner.vista;

import scanner.modelo.ResultadoEscaneo;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.net.InetAddress;

public class VentanaCarga extends JFrame {

    private JProgressBar barra;
    private JLabel texto1;
    private JLabel textoCont;
    private JButton botonResultados;

    private int Di, Ci, Bi, Ai;
    private int Df, Cf, Bf, Af;
    private long cantIps;
    private int contador = 0;

    ArrayList<ResultadoEscaneo> arrayDatos = new ArrayList<>();

    // Variables temporales por IP
    String ipConsultada;
    String hostPing;
    String estaConectado;
    String tiempoRespuesta;
    String ttl;
    String hostNslookup;
    String servidorDNS;

    private int cantPings;
    private int tiempoMax;

    public VentanaCarga(long cantIps, String IpInicio, String IpFinal, int tiempoMax, int cantPings) {
        this.cantIps = cantIps;
        this.cantPings = cantPings;
        this.tiempoMax = tiempoMax;

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

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Scanner de red");
        setSize(500, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        texto1 = new JLabel("Pasando por la ip: ");
        textoCont = new JLabel("IP's escaneadas: ");
        panelTextos.add(texto1);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 5)));
        panelTextos.add(textoCont);
        add(panelTextos, BorderLayout.NORTH);

        JPanel panelBarra = new JPanel();
        panelBarra.setLayout(new BorderLayout());
        panelBarra.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        barra = new JProgressBar(0, 100);
        barra.setValue(0);
        barra.setStringPainted(true);
        panelBarra.add(barra, BorderLayout.CENTER);
        add(panelBarra, BorderLayout.CENTER);

        botonResultados = new JButton("Ver resultados");
        botonResultados.setVisible(false);
        botonResultados.addActionListener(e -> new VentanaResultados(arrayDatos).setVisible(true));
        JPanel panelBoton = new JPanel();
        panelBoton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelBoton.add(botonResultados);
        add(panelBoton, BorderLayout.SOUTH);

        escaneoIps();
    }

    private void hacerPing(String ip) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            boolean reachable = inet.isReachable(tiempoMax);
            estaConectado = reachable ? "Conectado" : "Desconectado";
            ipConsultada = ip;

            hostPing = inet.getHostName().equals(ip) ? "" : inet.getHostName();
            tiempoRespuesta = "tiempo<1m";
            ttl = reachable ? "128" : "-1";

        } catch (Exception e) {
            estaConectado = "Desconectado";
            tiempoRespuesta = "tiempo<1m";
            ttl = "-1";
            hostPing = "";
        }
    }

    private void hacerNslookup(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("nslookup", ip);
            Process proceso = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream(), "Cp850"));
            String linea;
            hostNslookup = "";
            servidorDNS = "";
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.toLowerCase().startsWith("servidor") || linea.toLowerCase().startsWith("server")) {
                    String[] parts = linea.split(":");
                    if (parts.length >= 2) servidorDNS = parts[1].trim();
                }
                if (linea.toLowerCase().startsWith("nombre") || linea.toLowerCase().startsWith("name")) {
                    String[] parts = linea.split(":");
                    if (parts.length >= 2) hostNslookup = parts[1].trim();
                }
            }
            proceso.waitFor();

            if (!hostNslookup.isEmpty()) {
                hostPing = hostNslookup;
            }

        } catch (Exception e) {
            hostNslookup = "";
            servidorDNS = "";
        }
    }

    private void escaneoIps() {
        new Thread(() -> {
            while (true) {
                contador++;
                double progreso = (contador * 100.0) / cantIps;
                barra.setValue((int) progreso);

                String ipActual = String.format("%s.%s.%s.%s", Di, Ci, Bi, Ai);
                texto1.setText("Pasando por la ip: " + ipActual);

                hacerPing(ipActual);
                hacerNslookup(ipActual);

                boolean conexionBool = estaConectado != null && !estaConectado.toLowerCase().contains("desconect");

                int ttlInt = -1;
                try { ttlInt = Integer.parseInt(ttl); } catch (Exception ignored) {}

                ResultadoEscaneo resultado = new ResultadoEscaneo(
                        ipConsultada != null ? ipConsultada : ipActual,
                        conexionBool,
                        hostPing != null ? hostPing : "",
                        tiempoRespuesta,
                        ttlInt,
                        hostNslookup != null ? hostNslookup : "",
                        servidorDNS != null ? servidorDNS : ""
                );

                arrayDatos.add(resultado);
                textoCont.setText("IP's escaneadas: " + contador);

                if (Di == Df && Ci == Cf && Bi == Bf && Ai == Af) break;

                Ai++;
                if (Ai > 255) { Ai = 0; Bi++; }
                if (Bi > 255) { Bi = 0; Ci++; }
                if (Ci > 255) { Ci = 0; Di++; }

                try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            texto1.setText("Escaneo completo.");
            barra.setValue(100);
            SwingUtilities.invokeLater(() -> botonResultados.setVisible(true));
        }).start();
    }
}
