package scanner.vista;

import scanner.modelo.ResultadoEscaneo;
import scanner.modelo.Metodos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class VentanaResultados extends JFrame {

    private final ArrayList<ResultadoEscaneo> resultadosOriginal;
    private ArrayList<ResultadoEscaneo> visibles;

    private JTable tabla;
    private JLabel lblResumen;
    private JComboBox cbOrden;
    private JComboBox cbFiltro;
    private JTextField tfBuscar;

    public VentanaResultados(ArrayList<ResultadoEscaneo> resultados, boolean modoOscuro) {
        this.resultadosOriginal = resultados;
        this.visibles = new ArrayList<>(resultadosOriginal);

        setTitle("Resultados del escaneo");
        setSize(950, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setIconImage(new ImageIcon(getClass().getResource("/lupa.png")).getImage());

        // Colores
        Color bg = modoOscuro ? new Color(45, 45, 45) : Color.WHITE;
        Color fg = modoOscuro ? Color.WHITE : Color.BLACK;
        Color tablaBg = modoOscuro ? new Color(60, 60, 60) : Color.WHITE;
        Color tablaFg = modoOscuro ? Color.WHITE : Color.BLACK;
        Color selBg = modoOscuro ? new Color(100, 180, 255) : new Color(46, 134, 222);
        Color botonBg = modoOscuro ? new Color(80, 80, 80) : UIManager.getColor("Button.background");
        Color botonFg = modoOscuro ? Color.WHITE : Color.BLACK;

        // ---------- Toolbar superior ----------
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(8, 8, 8, 8));
        toolbar.setBackground(bg);

        cbOrden = new JComboBox(new String[]{
                "IP (menor → mayor)",
                "IP (mayor → menor)",
                "Tiempo respuesta (menor → mayor)",
                "Tiempo respuesta (mayor → menor)"
        });
        cbOrden.setPreferredSize(new Dimension(280, 28));

        cbFiltro = new JComboBox(new String[]{"Ambos", "Activos", "Inactivos"});
        cbFiltro.setPreferredSize(new Dimension(140, 28));

        JButton btnTxt = new JButton("Guardar TXT");
        JButton btnCsv = new JButton("Guardar CSV");
        JButton btnLog = new JButton("Guardar LOG");

        // Botones estilo oscuro
        btnTxt.setBackground(botonBg);
        btnTxt.setForeground(botonFg);
        btnCsv.setBackground(botonBg);
        btnCsv.setForeground(botonFg);
        btnLog.setBackground(botonBg);
        btnLog.setForeground(botonFg);

        JLabel lblOrden = new JLabel(" Orden: ");
        lblOrden.setForeground(fg);
        JLabel lblMostrar = new JLabel(" Mostrar: ");
        lblMostrar.setForeground(fg);

        toolbar.add(lblOrden);
        toolbar.add(cbOrden);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(lblMostrar);
        toolbar.add(cbFiltro);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(btnTxt);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnCsv);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnLog);

        add(toolbar, BorderLayout.NORTH);

        // ---------- Tabla ----------
        tabla = new JTable(new ModeloTabla());
        tabla.setAutoCreateRowSorter(false);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);
        tabla.setBackground(tablaBg);
        tabla.setForeground(tablaFg);
        tabla.setSelectionBackground(selBg);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(tablaBg);
        tabla.getTableHeader().setForeground(tablaFg);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(tablaBg);
        add(scroll, BorderLayout.CENTER);

        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    int row = tabla.getSelectedRow();
                    if (row >= 0 && row < visibles.size()) mostrarDetalle(visibles.get(row));
                }
            }
        });

        // ---------- Resumen + Buscador en sur ----------
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        panelSur.setBorder(new EmptyBorder(6, 10, 10, 10));
        panelSur.setBackground(bg);

        lblResumen = new JLabel("", SwingConstants.CENTER);
        lblResumen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResumen.setForeground(fg);
        panelSur.add(lblResumen);

        tfBuscar = new JTextField();
        tfBuscar.setToolTipText("Escribí un número o texto para filtrar por IP (ej: 20)");
        tfBuscar.setPreferredSize(new Dimension(120, 28));
        panelSur.add(new JLabel(" Buscar IP: "){{
            setForeground(fg);
        }});
        panelSur.add(tfBuscar);

        JButton btnLimpiarBusqueda = new JButton("Limpiar");
        btnLimpiarBusqueda.setBackground(botonBg);
        btnLimpiarBusqueda.setForeground(botonFg);
        panelSur.add(btnLimpiarBusqueda);

        add(panelSur, BorderLayout.SOUTH);

        // Buscador reactivo
        tfBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltroYOrden(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltroYOrden(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltroYOrden(); }
        });

        btnLimpiarBusqueda.addActionListener(ev -> {
            tfBuscar.setText("");
            aplicarFiltroYOrden();
            tfBuscar.requestFocusInWindow();
        });

        // Eventos de orden y filtro
        ActionListener actListener = e -> aplicarFiltroYOrden();
        cbOrden.addActionListener(actListener);
        cbFiltro.addActionListener(actListener);

        // Botones exportación
        btnTxt.addActionListener(e -> guardarComoTxt());
        btnCsv.addActionListener(e -> guardarComoCsv());
        btnLog.addActionListener(e -> guardarComoLog(false));

        aplicarFiltroYOrden();
    }

    // ---------------- Filtros y orden -----------------
    private void aplicarFiltroYOrden() {
        String filtro = (String) cbFiltro.getSelectedItem();
        String busqueda = tfBuscar.getText().trim();

        ArrayList<ResultadoEscaneo> filtrados = new ArrayList<>();
        for (ResultadoEscaneo r : resultadosOriginal) {
            if ("Activos".equals(filtro) && !r.isConexion()) continue;
            if ("Inactivos".equals(filtro) && r.isConexion()) continue;
            if (!busqueda.isEmpty() && !r.getIp().contains(busqueda)) continue;
            filtrados.add(r);
        }

        String orden = (String) cbOrden.getSelectedItem();
        if ("IP (menor → mayor)".equals(orden))
            filtrados.sort(Comparator.comparingLong(a -> Metodos.ipToLong(a.getIp())));
        else if ("IP (mayor → menor)".equals(orden))
            filtrados.sort((a, b) -> Long.compare(Metodos.ipToLong(b.getIp()), Metodos.ipToLong(a.getIp())));
        else if ("Tiempo respuesta (menor → mayor)".equals(orden))
            filtrados.sort(Comparator.comparingLong(a -> Metodos.parseTiempoToLong(a.getTiempoRespuesta())));
        else if ("Tiempo respuesta (mayor → menor)".equals(orden))
            filtrados.sort((a, b) -> Long.compare(Metodos.parseTiempoToLong(b.getTiempoRespuesta()),
                    Metodos.parseTiempoToLong(a.getTiempoRespuesta())));

        visibles = filtrados;
        ((AbstractTableModel) tabla.getModel()).fireTableDataChanged();
        actualizarResumen();
    }

    private void actualizarResumen() {
        int totales = resultadosOriginal.size();
        int mostrando = visibles.size();
        int activos = 0;
        for (ResultadoEscaneo r : visibles) if (r.isConexion()) activos++;
        int inactivos = mostrando - activos;
        lblResumen.setText("Mostrando " + mostrando + " de " + totales +
                " — Activos: " + activos + " — Inactivos: " + inactivos);
    }

    private void mostrarDetalle(ResultadoEscaneo r) {
        String ttlStr = (r.getTtl() <= 0) ? "-" : String.valueOf(r.getTtl());
        String msg =
                "IP: " + Metodos.dashOr(r.getIp()) + "\n" +
                "Conexión: " + (r.isConexion() ? "Conectado" : "Desconectado") + "\n" +
                "Host: " + Metodos.dashOr(r.getHost()) + "\n" +
                "Tiempo de respuesta: " + Metodos.dashOr(r.getTiempoRespuesta()) + "\n" +
                "TTL: " + ttlStr + "\n" +
                "Host Server: " + Metodos.dashOr(r.getHostServer()) + "\n" +
                "Servidor DNS: " + Metodos.dashOr(r.getServidorDNS()) +
                (r.getMensajeError() != null && !r.getMensajeError().trim().isEmpty() ? "\n\nNota: " + r.getMensajeError() : "");
        JOptionPane.showMessageDialog(this, msg, "Detalle de IP", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----------------- Exportación (mantenidos originales) -----------------
 // ----------------- Exportación -----------------
    private void guardarComoTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como TXT");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo de texto", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".txt"))
            archivo = new File(archivo.getAbsolutePath() + ".txt");

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int totales = resultadosOriginal.size();
        int activos = 0;
        for (ResultadoEscaneo r : resultadosOriginal) if (r.isConexion()) activos++;
        int inactivos = totales - activos;

        String[] headers = {"IP", "Conexión", "Host", "Tiempo", "TTL", "Host Server", "Servidor DNS"};
        int[] widths = {15, 13, 28, 16, 5, 22, 22};
        StringBuilder sb = new StringBuilder();
        sb.append("Informe de resultados - Scanner de red\n");
        sb.append("Fecha: ").append(fecha).append("\n");
        sb.append(Metodos.repeat('-', 60)).append("\n");
        sb.append("Total: ").append(totales)
                .append(" Respondieron: ").append(activos)
                .append(" No respondieron: ").append(inactivos).append("\n");
        sb.append(Metodos.repeat('-', 60)).append("\n\n");
        sb.append(Metodos.fixedRow(headers, widths)).append("\n");
        sb.append(Metodos.fixedRow(Metodos.repeatString("=", headers.length), widths)).append("\n");

        for (ResultadoEscaneo r : visibles) {
            String ttlStr = (r.getTtl() <= 0) ? "-" : String.valueOf(r.getTtl());
            String[] row = {
                    Metodos.dashOr(r.getIp()),
                    r.isConexion() ? "Conectado" : "Desconectado",
                    Metodos.dashOr(r.getHost()),
                    Metodos.dashOr(r.getTiempoRespuesta()),
                    ttlStr,
                    Metodos.dashOr(r.getHostServer()),
                    Metodos.dashOr(r.getServidorDNS())
            };
            sb.append(Metodos.fixedRow(row, widths)).append("\n");
        }

        try (Writer w = new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
            JOptionPane.showMessageDialog(this, "TXT guardado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar TXT: " + ex.getMessage());
        }
    }

    private void guardarComoCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv"))
            archivo = new File(archivo.getAbsolutePath() + ".csv");

        final char SEP = ';';
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

            bw.write('\ufeff'); // BOM
            String[] cols = {"IP","Conexión","Host","Tiempo","TTL","Host Server","Servidor DNS"};
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) bw.write(SEP);
                bw.write(Metodos.escapeCsv(cols[i], SEP));
            }
            bw.write("\n");

            for (ResultadoEscaneo r : visibles) {
                String ttlStr = (r.getTtl() <= 0) ? "-" : String.valueOf(r.getTtl());
                String[] row = {
                        Metodos.dashOr(r.getIp()),
                        r.isConexion() ? "Conectado" : "Desconectado",
                        Metodos.dashOr(r.getHost()),
                        Metodos.dashOr(r.getTiempoRespuesta()),
                        ttlStr,
                        Metodos.dashOr(r.getHostServer()),
                        Metodos.dashOr(r.getServidorDNS())
                };
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) bw.write(SEP);
                    bw.write(Metodos.escapeCsv(row[i], SEP));
                }
                bw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "CSV guardado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar CSV: " + ex.getMessage());
        }
    }

    private void guardarComoLog(boolean escaneoDetenido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como LOG");
        chooser.setFileFilter(new FileNameExtensionFilter("LOG", "log"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".log"))
            archivo = new File(archivo.getAbsolutePath() + ".log");

        String fechaGeneral = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

            bw.write("Registro de resultados - Scanner de red\n");
            bw.write("Fecha: " + fechaGeneral + "\n\n");

            for (ResultadoEscaneo r : visibles) {
                String horaEscaneo = new SimpleDateFormat("HH:mm:ss").format(r.getFechaEscaneo());
                String ttlStr = (r.getTtl() <= 0) ? "-" : String.valueOf(r.getTtl());

                bw.write("Hora: " + horaEscaneo + "\n");
                bw.write("IP: " + Metodos.dashOr(r.getIp()) + "\n");
                bw.write("Conexión: " + (r.isConexion() ? "Conectado" : "Desconectado") + "\n");
                bw.write("Host: " + Metodos.dashOr(r.getHost()) + "\n");
                bw.write("Tiempo: " + Metodos.dashOr(r.getTiempoRespuesta()) + "\n");
                bw.write("TTL: " + ttlStr + "\n");
                bw.write("Host Server: " + Metodos.dashOr(r.getHostServer()) + "\n");
                bw.write("Servidor DNS: " + Metodos.dashOr(r.getServidorDNS()) + "\n");
                if (r.getMensajeError() != null) bw.write("Nota: " + r.getMensajeError() + "\n");
                bw.write("--------------------------------------------------\n");
            }

            if (escaneoDetenido) {
                bw.write("\nNota: El usuario detuvo la ejecución del escaneo.\n");
            }

            JOptionPane.showMessageDialog(this, "LOG guardado correctamente.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar LOG: " + ex.getMessage());
        }
    }





    // ----------------- Modelo de tabla -----------------
    private class ModeloTabla extends AbstractTableModel {
        private final String[] columnas = {"IP","Conexión","Host","Tiempo","TTL","Host Server","Servidor DNS"};
        public int getRowCount() { return visibles.size(); }
        public int getColumnCount() { return columnas.length; }
        public String getColumnName(int col) { return columnas[col]; }
        public Object getValueAt(int row, int col) {
            ResultadoEscaneo r = visibles.get(row);
            switch (col) {
                case 0: return r.getIp();
                case 1: return r.isConexion() ? "Conectado" : "Desconectado";
                case 2: return r.getHost();
                case 3: return r.getTiempoRespuesta();
                case 4: return r.getTtl() <= 0 ? "-" : r.getTtl();
                case 5: return r.getHostServer();
                case 6: return r.getServidorDNS();
                default: return "";
            }
        }
        public boolean isCellEditable(int row, int col) { return false; }
    }
}
