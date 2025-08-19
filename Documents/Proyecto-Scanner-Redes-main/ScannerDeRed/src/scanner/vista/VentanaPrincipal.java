package scanner.vista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import scanner.modelo.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaPrincipal extends JFrame implements ActionListener {

    private JButton botonLimpiar, botonComenzar;
    private JLabel txtIpInicio, txtIpFinal, txtEspMaximo, txtPings;
    private JLabel estadoInicio, estadoFinal;
    private JTextField cajaIpInicio, cajaIpFinal, cajaEspMaximo, cajaPings;

    public VentanaPrincipal() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Scanner de red");
        setSize(520, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel textoBienvenida = new JLabel("Bienvenido al Scanner de red. Ingrese sus datos:");
        textoBienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        textoBienvenida.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(textoBienvenida, BorderLayout.NORTH);

        JPanel lamina = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Botón Limpiar
        botonLimpiar = new JButton("Limpiar la pantalla");
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lamina.add(botonLimpiar, gbc);
        gbc.gridwidth = 1;

        // IP inicial
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        txtIpInicio = new JLabel("IP inicial:");
        lamina.add(txtIpInicio, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cajaIpInicio = new JTextField(15);
        lamina.add(cajaIpInicio, gbc);

        // Mensaje de validación debajo
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        estadoInicio = new JLabel("");
        estadoInicio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lamina.add(estadoInicio, gbc);

        // IP final
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        txtIpFinal = new JLabel("IP final:");
        lamina.add(txtIpFinal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cajaIpFinal = new JTextField(15);
        lamina.add(cajaIpFinal, gbc);

        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        estadoFinal = new JLabel("");
        estadoFinal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lamina.add(estadoFinal, gbc);

        // Espera máxima
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        txtEspMaximo = new JLabel("Tiempo de espera máximo (ms)");
        lamina.add(txtEspMaximo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cajaEspMaximo = new JTextField(15);
        lamina.add(cajaEspMaximo, gbc);

        // Cantidad de pings
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        txtPings = new JLabel("Cantidad de intentos (pings)");
        lamina.add(txtPings, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cajaPings = new JTextField(15);
        lamina.add(cajaPings, gbc);

        // Botón Comenzar
        botonComenzar = new JButton("Comenzar el escaneo");
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lamina.add(botonComenzar, gbc);

        // Listeners
        botonComenzar.addActionListener(this);
        botonLimpiar.addActionListener(this);

        add(lamina, BorderLayout.CENTER);

        // Activar validación en tiempo real
        configurarValidacionEnTiempoReal();
    }

    private void configurarValidacionEnTiempoReal() {
        cajaIpInicio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
        });
        cajaIpFinal.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validarIPEnTiempoReal(); }
        });
    }

    private static long ipToLong(String ip) {
        String[] p = ip.trim().split("\\.");
        long d = Long.parseLong(p[0]);
        long c = Long.parseLong(p[1]);
        long b = Long.parseLong(p[2]);
        long a = Long.parseLong(p[3]);
        return d*256L*256L*256L + c*256L*256L + b*256L + a;
    }

    private void validarIPEnTiempoReal() {
        String ipInicio = cajaIpInicio.getText();
        String ipFinal = cajaIpFinal.getText();

        if (ipInicio.isEmpty()) {
            estadoInicio.setText("");
        } else if (scanner.modelo.Metodos.validarIp(ipInicio)) {
            estadoInicio.setText("IP válida");
            estadoInicio.setForeground(new Color(0, 128, 0));
        } else {
            estadoInicio.setText("IP inválida");
            estadoInicio.setForeground(Color.RED);
        }

        if (ipFinal.isEmpty()) {
            estadoFinal.setText("");
        } else if (scanner.modelo.Metodos.validarIp(ipFinal)) {
            estadoFinal.setText("IP válida");
            estadoFinal.setForeground(new Color(0, 128, 0));
        } else {
            estadoFinal.setText("IP inválida");
            estadoFinal.setForeground(Color.RED);
        }

        if (!ipFinal.isEmpty() && !ipInicio.isEmpty()
            && scanner.modelo.Metodos.validarIp(ipInicio)
            && scanner.modelo.Metodos.validarIp(ipFinal)
            && ipFinal.equals(ipInicio)) {

            estadoFinal.setText("IPs iguales");
            estadoFinal.setForeground(new Color(204, 170, 0));
            estadoInicio.setText("IPs iguales");
            estadoInicio.setForeground(new Color(204, 170, 0));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == botonComenzar) {
            String IpInicio = cajaIpInicio.getText();
            String IpFinal = cajaIpFinal.getText();

            if (!scanner.modelo.Metodos.validarIp(IpInicio)) {
                JOptionPane.showMessageDialog(this, "La IP inicial no es válida.");
                return;
            }
            if (!scanner.modelo.Metodos.validarIp(IpFinal)) {
                JOptionPane.showMessageDialog(this, "La IP final no es válida.");
                return;
            }

            long ipInicioNum = ipToLong(IpInicio);
            long ipFinalNum  = ipToLong(IpFinal);

            if (ipInicioNum > ipFinalNum) {
                estadoInicio.setText("La IP mayor debe estar acá");
                estadoInicio.setForeground(new Color(204, 170, 0));
                estadoFinal.setText("La IP menor debe estar acá");
                estadoFinal.setForeground(new Color(204, 170, 0));
                return;
            }

            if (ipInicioNum == ipFinalNum) {
                estadoInicio.setText("IPs iguales");
                estadoInicio.setForeground(new Color(204, 170, 0));
                estadoFinal.setText("IPs iguales");
                estadoFinal.setForeground(new Color(204, 170, 0));
                return;
            }

            long IpsScaneadas = (ipFinalNum - ipInicioNum) + 1;

            int tiempoMaximo = 1000;
            int cantPings = 4;

            if (!cajaEspMaximo.getText().isEmpty()) {
                if (!scanner.modelo.Metodos.validarNums(cajaEspMaximo.getText())) {
                    JOptionPane.showMessageDialog(this, "El tiempo máximo se especifica solo con números");
                    return;
                } else {
                    tiempoMaximo = Integer.parseInt(cajaPings.getText());
                }
            }

            // Abrir nueva ventana
            new VentanaCarga(IpsScaneadas, IpInicio, IpFinal, tiempoMaximo, cantPings).setVisible(true);

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
