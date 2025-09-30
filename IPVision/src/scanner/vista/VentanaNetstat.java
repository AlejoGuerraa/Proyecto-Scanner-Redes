// src/scanner/vista/VentanaNetstat.java
package scanner.vista;

import scanner.modelo.Metodos;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VentanaNetstat: ejecuta netstat con flags -ano, -s, -e (evitando -b) y muestra resultados.
 * Mejoras UI: SplitPane con tabla + pestañas de Estadísticas (tabla 2 columnas) y Resumen,
 * búsqueda, render por estado, popup copiar.
 */
public class VentanaNetstat extends JFrame {

    private static class NetEntry {
        String localIp;
        String localPort;
        String remoteIp;
        String remotePort;
        String process;
        String state;
    }

    private final boolean modoOscuro;
    private final JTable tabla;
    private final DefaultTableModel modelo;
    private final JProgressBar progressBar;
    private final JButton btnEjecutar;
    private final JLabel lblStatus;

    private final List<String> lastSummary = Collections.synchronizedList(new ArrayList<String>());
    private final List<String> lastErrors = Collections.synchronizedList(new ArrayList<String>());

    // Lista de comandos a ejecutar
    private final List<String[]> comandos = new ArrayList<String[]>();

    // Componentes adicionales
    private final JTextField txtFilter = new JTextField(24);
    private TableRowSorter<DefaultTableModel> sorter;

    // Pestañas / stats table
    private final JTabbedPane rightTabs;
    private final JTable statsTable;
    private DefaultTableModel statsTableModel;
    private final JTextArea resumenArea;

    public VentanaNetstat(boolean modoOscuro) {
        this.modoOscuro = modoOscuro;
        setTitle("IPVision - Información de Red");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setIconImage(new ImageIcon(getClass().getResource("/lupa.png")).getImage());

        // --- Panel principal ---
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel);

        JLabel titulo = new JLabel("Información de red (netstat)");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo, BorderLayout.NORTH);

        String[] columnas = {"Local IP", "L.Port", "Remote IP", "R.Port", "Programa/Proceso", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(18); // más chiquita para ver más filas
        tabla.setFont(tabla.getFont().deriveFont(12f));
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Sorter & Filter
        sorter = new TableRowSorter<DefaultTableModel>(modelo);
        tabla.setRowSorter(sorter);

        // Render personalizado por estado y filas alternadas
        final DefaultTableCellRenderer defaultRender = new DefaultTableCellRenderer();
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color alt1 = modoOscuro ? new Color(54, 57, 63) : Color.WHITE;
            private final Color alt2 = modoOscuro ? new Color(48, 50, 54) : new Color(250, 250, 250);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = defaultRender.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                String estado = "";
                try {
                    Object s = table.getModel().getValueAt(modelRow, 5);
                    estado = s == null ? "" : s.toString().toUpperCase();
                } catch (Exception ignored) {}
                if (isSelected) {
                    c.setBackground(modoOscuro ? new Color(70, 130, 180) : new Color(184, 207, 229));
                } else {
                    c.setBackground((row % 2 == 0) ? alt1 : alt2);
                    // colorear según estado
                    if (estado != null) {
                        if (estado.contains("ESTABLISHED")) c.setBackground(new Color(200, 255, 200)); // verde claro
                        else if (estado.contains("LISTEN")) c.setBackground(new Color(200, 225, 255)); // azul claro
                        else if (estado.contains("TIME_WAIT")) c.setBackground(new Color(230, 230, 230)); // gris
                        else if (estado.contains("CLOSE_WAIT")) c.setBackground(new Color(255, 235, 190)); // naranja claro
                        else if (estado.startsWith("SYN") || estado.contains("SYN_SENT") || estado.contains("SYN_RECV"))
                            c.setBackground(new Color(255, 250, 200)); // amarillo claro
                    }
                }
                return c;
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);

        // Panel derecho: pestañas (Estadísticas / Resumen)
        // Estadísticas -> tabla 2 columnas
        statsTableModel = new DefaultTableModel(new String[] {"Clave", "Valor"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        statsTable = new JTable(statsTableModel);
        statsTable.setFillsViewportHeight(true);
        statsTable.setRowHeight(18);
        statsTable.setFont(statsTable.getFont().deriveFont(12f));
        JScrollPane scrollStats = new JScrollPane(statsTable);

        resumenArea = new JTextArea();
        resumenArea.setEditable(false);
        resumenArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollResumen = new JScrollPane(resumenArea);

        rightTabs = new JTabbedPane();
        rightTabs.addTab("Estadísticas", scrollStats);
        rightTabs.addTab("Resumen", scrollResumen);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, rightTabs);
        split.setResizeWeight(0.72);
        panel.add(split, BorderLayout.CENTER);

        // South: controles, búsqueda y barra de progreso
        JPanel south = new JPanel(new BorderLayout(6, 6));
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        btnEjecutar = new JButton("Ejecutar netstat");
        controles.add(btnEjecutar);

        JButton btnTxt = new JButton("Guardar TXT");
        JButton btnCsv = new JButton("Guardar CSV");
        JButton btnLog = new JButton("Guardar LOG");
        controles.add(btnTxt); controles.add(btnCsv); controles.add(btnLog);

        // Búsqueda en tiempo real
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        txtFilter.setToolTipText("Filtrar (IP, puerto, proceso, estado) — expresiones literales.");
        buscador.add(new JLabel("Buscar:"));
        buscador.add(txtFilter);

        JPanel topSouth = new JPanel(new BorderLayout());
        topSouth.add(controles, BorderLayout.WEST);
        topSouth.add(buscador, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        lblStatus = new JLabel("Listo");
        lblStatus.setHorizontalAlignment(SwingConstants.RIGHT);

        south.add(topSouth, BorderLayout.NORTH);
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(progressBar, BorderLayout.CENTER);
        bottom.add(lblStatus, BorderLayout.EAST);
        south.add(bottom, BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);

        // Listeners
        btnEjecutar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ejecutarNetstatCompleto(); }
        });
        btnTxt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guardarComoTxt(); }
        });
        btnCsv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guardarComoCsv(); }
        });
        btnLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guardarComoLog(); }
        });

        // Filtro en tiempo real
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            private void aplicar() {
                String text = txtFilter.getText();
                if (text == null || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    try {
                        String q = Pattern.quote(text.trim());
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + q));
                    } catch (Exception ex) {
                        sorter.setRowFilter(null);
                    }
                }
            }
            public void insertUpdate(DocumentEvent e) { aplicar(); }
            public void removeUpdate(DocumentEvent e) { aplicar(); }
            public void changedUpdate(DocumentEvent e) { aplicar(); }
        });

        // Popup copiar fila
        final JPopupMenu popup = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copiar fila");
        popup.add(copyItem);
        copyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int row = tabla.getSelectedRow();
                if (row >= 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int c = 0; c < tabla.getColumnCount(); c++) {
                        if (c > 0) sb.append(" | ");
                        Object v = tabla.getValueAt(row, c);
                        sb.append(v == null ? "-" : v.toString());
                    }
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
                }
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) show(e); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) show(e); }
            private void show(MouseEvent e) {
                int r = tabla.rowAtPoint(e.getPoint());
                if (r >= 0) tabla.setRowSelectionInterval(r, r);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        actualizarTema(panel);

        // --- Comandos por defecto (evitando -b) ---
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            comandos.add(new String[]{"cmd.exe", "/c", "netstat", "-ano"});
            comandos.add(new String[]{"cmd.exe", "/c", "netstat", "-s"});
            comandos.add(new String[]{"cmd.exe", "/c", "netstat", "-e"});
        } else {
            comandos.add(new String[]{"sh", "-c", "netstat -anp 2>/dev/null || ss -anp"});
            comandos.add(new String[]{"sh", "-c", "netstat -s 2>/dev/null || ss -s"});
            comandos.add(new String[]{"sh", "-c", "netstat -e 2>/dev/null || true"});
        }

        // ajustar columnas por defecto
        TableColumnModel cm = tabla.getColumnModel();
        if (cm.getColumnCount() >= 6) {
            cm.getColumn(0).setPreferredWidth(140); // local ip
            cm.getColumn(1).setPreferredWidth(60);  // local port
            cm.getColumn(2).setPreferredWidth(180); // remote ip
            cm.getColumn(3).setPreferredWidth(60);  // remote port
            cm.getColumn(4).setPreferredWidth(260); // process
            cm.getColumn(5).setPreferredWidth(110); // state
        }
    }

    // Tema
    private void actualizarTema(Container container) {
        Color bg = modoOscuro ? new Color(45, 45, 45) : new Color(245, 247, 250);
        Color fg = modoOscuro ? Color.WHITE : Color.BLACK;
        container.setBackground(bg);
        tabla.setBackground(modoOscuro ? new Color(60, 60, 60) : Color.WHITE);
        tabla.setForeground(fg);
        tabla.getTableHeader().setBackground(bg);
        tabla.getTableHeader().setForeground(fg);
        statsTable.setBackground(modoOscuro ? new Color(40, 40, 40) : Color.WHITE);
        statsTable.setForeground(fg);
        resumenArea.setBackground(modoOscuro ? new Color(30, 30, 30) : Color.WHITE);
        resumenArea.setForeground(fg);
        progressBar.setBackground(bg);
        progressBar.setForeground(modoOscuro ? Color.CYAN : new Color(46, 134, 222));
    }

    // ---------------- Ejecutar netstat ----------------
    private void ejecutarNetstatCompleto() {
        btnEjecutar.setEnabled(false);
        modelo.setRowCount(0);
        statsTableModel.setRowCount(0);
        resumenArea.setText("");
        progressBar.setValue(0);
        lblStatus.setText("Iniciando...");
        lastErrors.clear();
        lastSummary.clear();

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private final Map<String, NetEntry> map = new LinkedHashMap<String, NetEntry>();

            @Override
            protected Void doInBackground() {
                int total = comandos.size();
                for (int i = 0; i < total; i++) {
                    final String[] cmd = comandos.get(i);
                    publish((i * 100) / total);

                    // Actualizar label (en EDT)
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { lblStatus.setText("Ejecutando: " + String.join(" ", cmd)); }
                    });

                    List<String> salida;
                    try {
                        salida = Metodos.ejecutarComando(cmd, 60_000);
                    } catch (InterruptedException ie) {
                        lastErrors.add("Interrumpido: " + String.join(" ", cmd));
                        Thread.currentThread().interrupt();
                        break;
                    } catch (IOException ioe) {
                        lastErrors.add("Error ejecutar: " + String.join(" ", cmd) + " -> " + ioe.getMessage());
                        continue;
                    }

                    String joined = String.join(" ", cmd).toLowerCase();
                    if (joined.contains("-ano") || joined.contains("-anp") || joined.contains("ss -anp") || joined.contains("netstat -an")) {
                        parseConnections(salida, map);
                    } else if (joined.contains("-s") || joined.contains("ss -s") || joined.contains("netstat -s")) {
                        parseStatistics(salida, lastSummary);
                    } else if (joined.contains("-e") || joined.contains("netstat -e")) {
                        parseStatistics(salida, lastSummary);
                    } else {
                        parseConnections(salida, map);
                    }

                    publish(((i + 1) * 100) / total);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                // Inicializar pidMap como final desde el principio
                final Map<String, String> pidMap;
                Map<String, String> tempMap;
                try {
                    tempMap = Metodos.obtenerProcesosPorPID();
                } catch (Throwable ignored) {
                    tempMap = new HashMap<String, String>();
                }
                pidMap = tempMap; // ahora pidMap es final y efectivamente final

                // Copiar map a final para usarlo dentro del Runnable
                final Map<String, NetEntry> finalMap = new LinkedHashMap<String, NetEntry>(map);

                // Actualizar UI en EDT
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        modelo.setRowCount(0);
                        for (NetEntry e : finalMap.values()) {
                            String proc = (e.process == null) ? "-" : e.process;

                            // Si solo tiene PID y está en pidMap, mostrar nombre del proceso
                            if (proc.matches("^\\d+$") && pidMap.containsKey(proc)) {
                                proc = proc + " (" + pidMap.get(proc) + ")";
                            } else if (proc.contains("/")) {
                                String left = proc.split("/")[0];
                                if (left.matches("^\\d+$") && pidMap.containsKey(left)) {
                                    proc = left + " (" + pidMap.get(left) + ")";
                                }
                            }

                            modelo.addRow(new Object[]{
                                    e.localIp == null ? "-" : e.localIp,
                                    e.localPort == null ? "-" : e.localPort,
                                    e.remoteIp == null ? "-" : e.remoteIp,
                                    e.remotePort == null ? "-" : e.remotePort,
                                    proc,
                                    e.state == null ? "-" : e.state
                            });
                        }

                        // Estadísticas: convertir lastSummary a pares clave/valor y poblar statsTableModel
                        statsTableModel.setRowCount(0);
                        synchronized (lastSummary) {
                            for (String line : lastSummary) {
                                Map.Entry<String, String> kv = splitStatLine(line);
                                if (kv != null) statsTableModel.addRow(new Object[]{kv.getKey(), kv.getValue()});
                                else statsTableModel.addRow(new Object[]{line, ""});
                            }
                        }

                        // Resumen: primeras líneas + errores
                        StringBuilder sb = new StringBuilder();
                        synchronized (lastSummary) {
                            int max = Math.min(200, lastSummary.size());
                            for (int i = 0; i < max; i++) sb.append(lastSummary.get(i)).append("\n");
                        }
                        synchronized (lastErrors) {
                            if (!lastErrors.isEmpty()) {
                                sb.append("\n---- Errores ----\n");
                                for (String err : lastErrors) sb.append(err).append("\n");
                            }
                        }
                        resumenArea.setText(sb.toString());
                        resumenArea.setCaretPosition(0);

                        progressBar.setValue(100);
                        lblStatus.setText("Finalizado");
                        btnEjecutar.setEnabled(true);
                    }
                });
            }



        };
        worker.execute();
    }

    // ----------------- Métodos de parseo -----------------
    private void parseConnections(List<String> salida, Map<String, NetEntry> map) {
        Pattern protoPattern = Pattern.compile("^(tcp|udp)", Pattern.CASE_INSENSITIVE);
        for (String raw : salida) {
            String linea = raw == null ? "" : raw.trim();
            if (linea.isEmpty()) continue;
            String lower = linea.toLowerCase();
            if (lower.startsWith("proto") || lower.startsWith("protocol") || lower.contains("local address")
                    || lower.contains("estado") || lower.contains("state") || lower.contains("netstat")) continue;

            String[] tokens = linea.split("\\s+");
            if (tokens.length < 2) continue;

            int idxLocal = -1;
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].contains(":") && !tokens[i].contains("::")) { idxLocal = i; break; }
            }
            if (idxLocal < 0 || idxLocal + 1 >= tokens.length) {
                Matcher pm = protoPattern.matcher(tokens[0]);
                if (!pm.find() || tokens.length < 4) continue;
                idxLocal = 1;
            }

            String localTok = tokens[idxLocal];
            String remoteTok = tokens.length > idxLocal + 1 ? tokens[idxLocal + 1] : "-";
            String stateTok = tokens.length > idxLocal + 2 ? tokens[idxLocal + 2] : "-";
            String possiblePid = tokens.length > idxLocal + 3 ? tokens[idxLocal + 3] : null;

            String[] local = splitAddr(localTok);
            String[] remote = splitAddr(remoteTok);
            if (local == null) continue;

            NetEntry e = new NetEntry();
            e.localIp = normalizeIp(local[0]);
            e.localPort = local[1] == null ? "-" : local[1];
            if (remote != null) {
                e.remoteIp = normalizeIp(remote[0]);
                e.remotePort = remote[1] == null ? "-" : remote[1];
            } else {
                e.remoteIp = "-";
                e.remotePort = "-";
            }
            e.state = stateTok == null ? "-" : stateTok;
            if (possiblePid != null) e.process = possiblePid;
            else e.process = "-";

            String key = e.localIp + ":" + e.localPort + ">" + e.remoteIp + ":" + e.remotePort;
            map.putIfAbsent(key, e);
        }
    }

    private void parseStatistics(List<String> salida, List<String> summary) {
        for (String raw : salida) {
            String linea = raw == null ? "" : raw.trim();
            if (linea.isEmpty()) continue;
            if (linea.length() > 0 && linea.length() < 1000) summary.add(linea);
        }
    }

    // Helpers de parseo
    private String[] splitAddr(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.startsWith("[") && token.contains("]:")) {
            int idx = token.lastIndexOf("]:");
            String ip = token.substring(1, idx);
            String port = token.substring(idx + 2);
            return new String[]{ip, port};
        }
        int lastColon = token.lastIndexOf(':');
        if (lastColon <= 0) return new String[]{token, null};
        String ip = token.substring(0, lastColon);
        String port = token.substring(lastColon + 1);
        return new String[]{ip, port};
    }

    private String normalizeIp(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length() - 1);
        return s;
    }

    /**
     * splitStatLine: intenta convertir una línea de estadísticas en (clave, valor)
     * - Si tiene '=', separa por la primera '='.
     * - Si termina con números, separa por la porción final que comienza con dígitos.
     * - Si no pudo, devuelve null.
     */
    private Map.Entry<String,String> splitStatLine(String line) {
        if (line == null) return null;
        String l = line.trim();
        if (l.isEmpty()) return null;
        // 1) contiene '=' ?
        int ix = l.indexOf('=');
        if (ix > 0) {
            String k = l.substring(0, ix).trim();
            String v = l.substring(ix + 1).trim();
            return new AbstractMap.SimpleEntry<String,String>(k, v);
        }
        // 2) intentar separar por último bloque que comienza con dígito
        Pattern p = Pattern.compile("^(.*?)[\\s\\t]+(\\d[\\d\\s,]+.*)$");
        Matcher m = p.matcher(l);
        if (m.find()) {
            String k = m.group(1).trim();
            String v = m.group(2).trim();
            return new AbstractMap.SimpleEntry<String,String>(k, v);
        }
        // 3) si tiene solo dos columnas separadas por múltiples espacios (ej: "Bytes 435185100 62526168")
        Pattern p2 = Pattern.compile("^(\\D[\\w\\s\\-\\.]+?)\\s{2,}(.+)$");
        Matcher m2 = p2.matcher(l);
        if (m2.find()) {
            return new AbstractMap.SimpleEntry<String,String>(m2.group(1).trim(), m2.group(2).trim());
        }
        return null;
    }

    // ----------------- Exportación -----------------
    private void guardarComoTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como TXT");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo de texto", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".txt")) archivo = new File(archivo.getAbsolutePath() + ".txt");
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            bw.write("Informe Netstat - Fecha: " + fecha + "\n\n");
            bw.write(String.format("%-16s %-8s %-16s %-10s %-24s %-16s\n",
                    "Local IP","L.Port","Remote IP","R.Port","Proceso","Estado"));
            bw.write(new String(new char[140]).replace("\0","-") + "\n");
            for (int r = 0; r < modelo.getRowCount(); r++) {
                Object a = modelo.getValueAt(r,0), b = modelo.getValueAt(r,1), c = modelo.getValueAt(r,2);
                Object d = modelo.getValueAt(r,3), e = modelo.getValueAt(r,4), f = modelo.getValueAt(r,5);
                bw.write(String.format("%-16s %-8s %-16s %-10s %-24s %-16s\n",
                        a==null?"-":a.toString(),
                        b==null?"-":b.toString(),
                        c==null?"-":c.toString(),
                        d==null?"-":d.toString(),
                        e==null?"-":e.toString(),
                        f==null?"-":f.toString()
                ));
            }
            // estadísticas en formato 2 columnas (si hay)
            bw.write("\n---- ESTADÍSTICAS ----\n");
            if (statsTableModel.getRowCount() > 0) {
                for (int r = 0; r < statsTableModel.getRowCount(); r++) {
                    String k = statsTableModel.getValueAt(r,0) == null ? "" : statsTableModel.getValueAt(r,0).toString();
                    String v = statsTableModel.getValueAt(r,1) == null ? "" : statsTableModel.getValueAt(r,1).toString();
                    bw.write(String.format("%-50s : %s\n", k, v));
                }
            } else {
                synchronized (lastSummary) {
                    if (lastSummary.isEmpty()) bw.write("Sin estadísticas recogidas.\n");
                    else for (String s : lastSummary) bw.write(s + "\n");
                }
            }

            synchronized (lastErrors) {
                if (!lastErrors.isEmpty()) {
                    bw.write("\n---- ERRORES ----\n");
                    for (String err : lastErrors) bw.write(err + "\n");
                }
            }
            JOptionPane.showMessageDialog(this, "TXT guardado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar TXT: " + ex.getMessage());
        }
    }

    private void guardarComoCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como CSV");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) archivo = new File(archivo.getAbsolutePath() + ".csv");
        final char SEP = ';';
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            String[] cols = {"Local IP","Local Port","Remote IP","Remote Port","Programa/Proceso","Estado"};
            for (int i = 0; i < cols.length; i++) {
                if (i>0) bw.write(SEP);
                bw.write(cols[i]);
            }
            bw.write("\n");
            for (int r = 0; r < modelo.getRowCount(); r++) {
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) bw.write(SEP);
                    Object v = modelo.getValueAt(r, c);
                    String s = v == null ? "" : v.toString().replace("\"", "\"\"");
                    if (s.indexOf(SEP) >= 0 || s.contains("\"") || s.contains("\n")) s = "\"" + s + "\"";
                    bw.write(s);
                }
                bw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "CSV guardado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar CSV: " + ex.getMessage());
        }
    }

    private void guardarComoLog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar como LOG");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("LOG", "log"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".log")) archivo = new File(archivo.getAbsolutePath() + ".log");
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            bw.write("LOG de Netstat - Fecha: " + fecha + "\n\n");
            bw.write("TABLA:\n");
            for (int r = 0; r < modelo.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) row.append(" | ");
                    Object v = modelo.getValueAt(r,c);
                    row.append(v==null?"-":v.toString());
                }
                bw.write(row.toString() + "\n");
            }

            bw.write("\n---- ESTADÍSTICAS (Clave : Valor) ----\n");
            if (statsTableModel.getRowCount() > 0) {
                for (int r = 0; r < statsTableModel.getRowCount(); r++) {
                    String k = statsTableModel.getValueAt(r,0) == null ? "" : statsTableModel.getValueAt(r,0).toString();
                    String v = statsTableModel.getValueAt(r,1) == null ? "" : statsTableModel.getValueAt(r,1).toString();
                    bw.write(k + " : " + v + "\n");
                }
            } else {
                synchronized (lastSummary) {
                    if (lastSummary.isEmpty()) bw.write("Sin estadísticas recogidas.\n");
                    else for (String s : lastSummary) bw.write(s + "\n");
                }
            }

            bw.write("\nERRORES:\n");
            synchronized (lastErrors) {
                if (lastErrors.isEmpty()) bw.write("Ningún error registrado.\n");
                else {
                    for (String err : lastErrors) bw.write(err + "\n");
                }
            }
            JOptionPane.showMessageDialog(this, "LOG guardado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar LOG: " + ex.getMessage());
        }
    }
}
