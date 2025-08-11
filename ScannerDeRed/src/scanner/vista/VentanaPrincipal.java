package scanner.vista;

import javax.swing.*;

import scanner.modelo.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaPrincipal extends JFrame implements ActionListener {

    private JButton botonLimpiar, botonComenzar;
    private JLabel txtIpInicio, txtIpFinal, txtEspMinimo, txtEspMaximo;
    private JTextField cajaIpInicio, cajaIpFinal, cajaEspMaximo, cajaEspMinimo;

    public VentanaPrincipal() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Scanner de red");
        setSize(500, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel textoBienvenida = new JLabel("Bienvenido al Scanner de red. Ingrese sus datos:");
        textoBienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        textoBienvenida.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(textoBienvenida, BorderLayout.NORTH);

        JPanel lamina = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Botón Limpiar (arriba de todo)
        botonLimpiar = new JButton("Limpiar la pantalla");
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lamina.add(botonLimpiar, gbc);
        gbc.gridwidth = 1; // restablecer

        // IP inicial
        gbc.gridy = 1;
        gbc.gridx = 0;
        txtIpInicio = new JLabel("IP inicial:");
        lamina.add(txtIpInicio, gbc);

        gbc.gridx = 1;
        cajaIpInicio = new JTextField(15);
        lamina.add(cajaIpInicio, gbc);

        // IP final
        gbc.gridy = 2;
        gbc.gridx = 0;
        txtIpFinal = new JLabel("IP final:");
        lamina.add(txtIpFinal, gbc);

        gbc.gridx = 1;
        cajaIpFinal = new JTextField(15);
        lamina.add(cajaIpFinal, gbc);

        // Espera mínima 
        gbc.gridy = 3;
        gbc.gridx = 0;
        txtEspMinimo = new JLabel("Tiempo de espera mínimo (segs)");
        lamina.add(txtEspMinimo, gbc);

        gbc.gridx = 1;
        cajaEspMinimo = new JTextField(15);
        lamina.add(cajaEspMinimo, gbc);

        // Espera máxima
        gbc.gridy = 4;
        gbc.gridx = 0;
        txtEspMaximo = new JLabel("Tiempo de espera máximo (segs)");
        lamina.add(txtEspMaximo, gbc);

        gbc.gridx = 1;
        cajaEspMaximo = new JTextField(15);
        lamina.add(cajaEspMaximo, gbc);

        // Botón Comenzar
        botonComenzar = new JButton("Comenzar el escaneo");
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lamina.add(botonComenzar, gbc);

        // Listeners
        botonComenzar.addActionListener(this);
        botonLimpiar.addActionListener(this);

        // Agregar panel central
        add(lamina, BorderLayout.CENTER);
        
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == botonComenzar) {
            new VentanaCarga().setVisible(true);

            String IpInicio = cajaIpInicio.getText();
            String IpFinal = cajaIpFinal.getText();
            String txTiempoMax = cajaEspMaximo.getText();
            String txTiempoMin = cajaEspMinimo.getText();

            if (!IpInicio.isEmpty() && !IpFinal.isEmpty()) { // Si los campos de ip tienen contenido
                if (!txTiempoMax.isEmpty() && !txTiempoMin.isEmpty()) { // Si los campos de tiempo tienen contenido
                    try {
                        int tiempoMin = Integer.parseInt(txTiempoMin);
                        int tiempoMax = Integer.parseInt(txTiempoMax);
                        
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Tiempo minimo y maximo deben ser números válidos.");
                        }
                    if (scanner.modelo.Metodos.validarIp(IpInicio) == false) {
                        System.out.println("La IP no es válida.");
                    } else {
                        System.out.println("La IP es inválida.");
                    }
                }
            else {
                JOptionPane.showMessageDialog(this, "Todos los campos deben estar completos.");
            }

        }
        else if (source == botonLimpiar) {
            // Vacía los contenedores de texto
            System.out.println(txtEspMaximo);
            cajaIpInicio.setText("");
            cajaIpFinal.setText("");
            cajaEspMinimo.setText("");
            cajaEspMaximo.setText("");
        	}

        }
    }
}
