package scanner.vista;

import scanner.modelo.ResultadoEscaneo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class VentanaResultados extends JFrame {

    private JTable tabla;
    private DefaultTableModel modelo;
    private ArrayList<ResultadoEscaneo> resultados;

    public VentanaResultados(ArrayList<ResultadoEscaneo> resultados) {
        this.resultados = resultados;

        setTitle("Resultados del Escaneo");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel principal con padding
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        add(panelPrincipal, BorderLayout.CENTER);

        // Columnas
        String[] columnas = {"IP", "Conexión", "Host", "Tiempo", "TTL", "Host Server", "Servidor DNS"};

        // Modelo
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        cargarTabla();

        // Tabla
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(25);
        tabla.getTableHeader().setBackground(new Color(220, 220, 220));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setSelectionBackground(new Color(173, 216, 230));
        tabla.setGridColor(Color.LIGHT_GRAY);

        // Alternancia de color en filas
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        panelPrincipal.add(scrollTabla, BorderLayout.CENTER);

        // Panel inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelInferior.setBackground(Color.WHITE);

        // Combo de orden
        String[] opcionesOrden = {"IP Ascendente", "IP Descendente", "Conexión", "Tiempo"};
        JComboBox<String> comboOrden = new JComboBox<>(opcionesOrden);
        comboOrden.setFont(new Font("Arial", Font.PLAIN, 13));
        comboOrden.setPreferredSize(new Dimension(150, 25));
        comboOrden.addActionListener(e -> ordenarTabla(comboOrden.getSelectedIndex()));

        // Botón guardar con estilo
        JButton btnGuardar = new JButton("Guardar en TXT");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        btnGuardar.addActionListener(e -> guardarArchivo());

        // Efecto hover
        btnGuardar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnGuardar.setBackground(new Color(100, 149, 237)); }
            @Override
            public void mouseExited(MouseEvent e) { btnGuardar.setBackground(new Color(70, 130, 180)); }
        });

        panelInferior.add(comboOrden);
        panelInferior.add(btnGuardar);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (ResultadoEscaneo r : resultados) {
            modelo.addRow(new Object[]{
                    r.getIp(),
                    r.isConexion() ? "Conectado" : "Desconectado",
                    r.getHost(),
                    r.getTiempoRespuesta(),
                    r.getTtl(),
                    r.getHostServer(),
                    r.getServidorDNS()
            });
        }
    }

    private void ordenarTabla(int opcion) {
        switch (opcion) {
            case 0: resultados.sort(Comparator.comparing(ResultadoEscaneo::getIp)); break;
            case 1: resultados.sort(Comparator.comparing(ResultadoEscaneo::getIp).reversed()); break;
            case 2: resultados.sort(Comparator.comparing(ResultadoEscaneo::isConexion).reversed()); break;
            case 3: resultados.sort(Comparator.comparing(ResultadoEscaneo::getTiempoRespuesta)); break;
        }
        cargarTabla();
    }

    private void guardarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar resultados");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    writer.write(modelo.getColumnName(i) + "\t");
                }
                writer.newLine();

                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        writer.write(modelo.getValueAt(i, j).toString() + "\t");
                    }
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(this, "Archivo guardado correctamente.");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
