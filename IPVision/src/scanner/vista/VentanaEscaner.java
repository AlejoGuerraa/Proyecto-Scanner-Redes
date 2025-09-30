package scanner.vista;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import scanner.modelo.Metodos;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaEscaner extends JFrame implements ActionListener {

    private JButton botonLimpiar, botonComenzar;
    private JTextField cajaIpInicio, cajaIpFinal, cajaEspMaximo, cajaPings;
    private JLabel estadoInicio, estadoFinal;
    private boolean modoOscuro; // Recibido desde VentanaPrincipal

    public VentanaEscaner(boolean modoOscuro) {
        this.modoOscuro = modoOscuro;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Scanner de red");
        setSize(550, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Icono de lupa
        setIconImage(new ImageIcon(getClass().getResource("/lupa.png")).getImage());

        // ---------- Título ----------
        JLabel titulo = new JLabel("IPVision - Scanner de Red");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        // ---------- Panel principal ----------
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(245, 247, 250));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(panelPrincipal, BorderLayout.CENTER);

        // ---------- Panel IPs ----------
        JPanel panelIPs = new JPanel(new GridBagLayout());
        panelIPs.setBackground(new Color(245, 247, 250));
        panelIPs.setBorder(BorderFactory.createTitledBorder("Rango de IP"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // IP inicial
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panelIPs.add(new JLabel("IP inicial:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cajaIpInicio = new JTextField(15);
        cajaIpInicio.setToolTipText("Ingrese la IP inicial del rango (ej: 192.168.1.1)");
        panelIPs.add(cajaIpInicio, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        estadoInicio = new JLabel();
        panelIPs.add(estadoInicio, gbc);

        // IP final
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelIPs.add(new JLabel("IP final:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cajaIpFinal = new JTextField(15);
        cajaIpFinal.setToolTipText("Ingrese la IP final del rango (ej: 192.168.1.10)");
        panelIPs.add(cajaIpFinal, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        estadoFinal = new JLabel();
        panelIPs.add(estadoFinal, gbc);

        panelPrincipal.add(panelIPs);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // ---------- Panel Parámetros ----------
        JPanel panelParametros = new JPanel(new GridBagLayout());
        panelParametros.setBackground(new Color(245, 247, 250));
        panelParametros.setBorder(BorderFactory.createTitledBorder("Parámetros de escaneo"));

        // Tiempo máximo
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelParametros.add(new JLabel("Tiempo máximo (ms):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cajaEspMaximo = new JTextField(15);
        cajaEspMaximo.setToolTipText("Tiempo máximo de espera por ping (min 10ms)");
        panelParametros.add(cajaEspMaximo, gbc);

        // Cantidad de pings
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelParametros.add(new JLabel("Cantidad de intentos:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cajaPings = new JTextField(15);
        cajaPings.setToolTipText("Número de intentos de ping por IP (max 10)");
        panelParametros.add(cajaPings, gbc);

        panelPrincipal.add(panelParametros);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // ---------- Panel Botones ----------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotones.setBackground(panelPrincipal.getBackground());

        // Botones Limpiar y Comenzar
        botonLimpiar = crearBoton("Limpiar");
        botonComenzar = crearBoton("Comenzar escaneo");

        panelBotones.add(botonLimpiar);
        panelBotones.add(botonComenzar);

        panelPrincipal.add(panelBotones);

        // ---------- Listeners ----------
        botonLimpiar.addActionListener(this);
        botonComenzar.addActionListener(this);
        configurarValidacionEnTiempoReal();

        actualizarTema(); // inicializar colores según modo
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(46, 134, 222));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(modoOscuro ? Color.GRAY : new Color(65, 150, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(modoOscuro ? Color.DARK_GRAY : new Color(46, 134, 222));
            }
        });
        return btn;
    }

    private void configurarValidacionEnTiempoReal() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validarIP(); }
            public void removeUpdate(DocumentEvent e) { validarIP(); }
            public void changedUpdate(DocumentEvent e) { validarIP(); }
        };
        cajaIpInicio.getDocument().addDocumentListener(dl);
        cajaIpFinal.getDocument().addDocumentListener(dl);
    }

    private void validarIP() {
        validarCampo(cajaIpInicio, estadoInicio);
        validarCampo(cajaIpFinal, estadoFinal);

        if (!cajaIpInicio.getText().isEmpty() && cajaIpInicio.getText().equals(cajaIpFinal.getText())) {
            estadoInicio.setText("IPs iguales");
            estadoInicio.setForeground(new Color(204, 170, 0));
            estadoFinal.setText("IPs iguales");
            estadoFinal.setForeground(new Color(204, 170, 0));
        }
    }

    private void validarCampo(JTextField campo, JLabel estado) {
        String ip = campo.getText();
        if (ip.isEmpty()) {
            estado.setText("");
        } else if (Metodos.validarIp(ip)) {
            estado.setText("IP válida");
            estado.setForeground(new Color(0, 128, 0));
        } else {
            estado.setText("IP inválida");
            estado.setForeground(Color.RED);
        }
    }

    private void actualizarTema() {
        Color bg = modoOscuro ? new Color(45, 45, 45) : new Color(245, 247, 250);
        Color fg = modoOscuro ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bg);
        for (Component c : getContentPane().getComponents()) {
            actualizarComponenteTema(c, bg, fg);
        }
        repaint();
    }

    private void actualizarComponenteTema(Component c, Color bg, Color fg) {
        if (c instanceof JComponent && ((JComponent)c).getBorder() instanceof TitledBorder) {
            ((TitledBorder)((JComponent)c).getBorder()).setTitleColor(fg);
        }

        if (c instanceof JPanel) {
            c.setBackground(bg);
            for (Component hijo : ((JPanel) c).getComponents()) {
                actualizarComponenteTema(hijo, bg, fg);
            }
        } else if (c instanceof JLabel) {
            c.setForeground(fg);
        } else if (c instanceof JButton) {
            c.setBackground(modoOscuro ? Color.DARK_GRAY : new Color(46, 134, 222));
            c.setForeground(Color.WHITE);
        } else if (c instanceof JTextField) {
            c.setBackground(modoOscuro ? new Color(70, 70, 70) : Color.WHITE);
            c.setForeground(fg);
            ((JTextField) c).setCaretColor(fg);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == botonComenzar) {
            String ipInicio = cajaIpInicio.getText();
            String ipFinal = cajaIpFinal.getText();

            if (!Metodos.validarIp(ipInicio) || !Metodos.validarIp(ipFinal)) {
                JOptionPane.showMessageDialog(this, "Revise las IPs ingresadas.");
                return;
            }

            if (ipInicio.equals(ipFinal)) {
                JOptionPane.showMessageDialog(this, "Las IPs no pueden ser iguales.");
                return;
            }

            long ipIniNum = Metodos.ipToLong(ipInicio);
            long ipFinNum = Metodos.ipToLong(ipFinal);

            if (ipFinNum < ipIniNum) {
                JOptionPane.showMessageDialog(this, "La IP final no puede ser menor que la IP inicial.");
                return;
            }

            int tiempoMax = 1000, cantPings = 3;

            if (!cajaEspMaximo.getText().isEmpty()) {
                if (!Metodos.validarNums(cajaEspMaximo.getText())) {
                    JOptionPane.showMessageDialog(this, "Tiempo máximo debe ser numérico.");
                    return;
                } else {
                    tiempoMax = Integer.parseInt(cajaEspMaximo.getText());
                    if (tiempoMax < 10) {
                        JOptionPane.showMessageDialog(this, "Tiempo máximo no puede ser menor a 10 ms.");
                        return;
                    }
                }
            }

            if (!cajaPings.getText().isEmpty()) {
                if (!Metodos.validarNums(cajaPings.getText())) {
                    JOptionPane.showMessageDialog(this, "Cantidad de pings debe ser numérica.");
                    return;
                } else {
                    cantPings = Integer.parseInt(cajaPings.getText());
                    if (cantPings > 10) {
                        JOptionPane.showMessageDialog(this, "Cantidad de pings no puede superar 10.");
                        return;
                    }
                }
            }

            long ipsScaneadas = (ipFinNum - ipIniNum) + 1;

            new VentanaCarga(ipsScaneadas, ipInicio, ipFinal, tiempoMax, cantPings, modoOscuro).setVisible(true);

        } else if (source == botonLimpiar) {
            cajaIpInicio.setText("");
            cajaIpFinal.setText("");
            cajaEspMaximo.setText("");
            cajaPings.setText("");
            estadoInicio.setText("");
            estadoFinal.setText("");
        }
    }

}
