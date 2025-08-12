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
    private JLabel txtIpInicio, txtIpFinal, txtEspMaximo;
    private JLabel estadoInicio, estadoFinal;
    private JTextField cajaIpInicio, cajaIpFinal, cajaEspMaximo;

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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Botón Limpiar
        botonLimpiar = new JButton("Limpiar la pantalla");
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lamina.add(botonLimpiar, gbc);
        gbc.gridwidth = 1;

        // IP inicial
        gbc.gridy = 1;
        gbc.gridx = 0;
        txtIpInicio = new JLabel("IP inicial:");
        lamina.add(txtIpInicio, gbc);

        gbc.gridx = 1;
        cajaIpInicio = new JTextField(15);
        lamina.add(cajaIpInicio, gbc);

        // Mensaje de validación debajo
        gbc.gridy = 2;
        gbc.gridx = 1;
        estadoInicio = new JLabel("");
        estadoInicio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lamina.add(estadoInicio, gbc);

        // IP final
        gbc.gridy = 3;
        gbc.gridx = 0;
        txtIpFinal = new JLabel("IP final:");
        lamina.add(txtIpFinal, gbc);

        gbc.gridx = 1;
        cajaIpFinal = new JTextField(15);
        lamina.add(cajaIpFinal, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 1;
        estadoFinal = new JLabel("");
        estadoFinal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lamina.add(estadoFinal, gbc);
        
        // Espera máxima
        gbc.gridy = 5;
        gbc.gridx = 0;
        txtEspMaximo = new JLabel("Tiempo de espera máximo (segs)");
        lamina.add(txtEspMaximo, gbc);

        gbc.gridx = 1;
        cajaEspMaximo = new JTextField(15);
        lamina.add(cajaEspMaximo, gbc);

        // Botón Comenzar
        botonComenzar = new JButton("Comenzar el escaneo");
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lamina.add(botonComenzar, gbc);

        // Listeners
        botonComenzar.addActionListener(this);
        botonLimpiar.addActionListener(this);

        // Agregar panel central
        add(lamina, BorderLayout.CENTER);

        // Activar validación en tiempo real
        configurarValidacionEnTiempoReal();
    }

    private void configurarValidacionEnTiempoReal() {
        cajaIpInicio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
        });
        cajaIpFinal.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validarIPEnTiempoReal();
            }
        });
    }

    private void validarIPEnTiempoReal() {
        String ipInicio = cajaIpInicio.getText();
        String ipFinal = cajaIpFinal.getText();
        
        if (ipInicio.isEmpty()) {
        	estadoInicio.setText("");
        }
        else if (scanner.modelo.Metodos.validarIp(ipInicio)) {
        	estadoInicio.setText("IP válida");
        	estadoInicio.setForeground(new Color(0, 128, 0)); // Verde
        } else {
        	estadoInicio.setText("IP inválida");
        	estadoInicio.setForeground(Color.RED);
        }
        
        
        if (ipFinal.isEmpty()){
        	estadoFinal.setText("");
        }
        else if(scanner.modelo.Metodos.validarIp(ipFinal)) {
        	estadoFinal.setText("IP válida");
        	estadoFinal.setForeground(new Color(0,128,0));
        } else{
        	estadoFinal.setText("IP inválida");
        	estadoFinal.setForeground(Color.RED);
        }
        if (!ipFinal.isEmpty() && !ipInicio.isEmpty()) {
            if (scanner.modelo.Metodos.validarIp(ipInicio) && scanner.modelo.Metodos.validarIp(ipFinal)) {
	        	if (ipFinal.equals(ipInicio)) {
		        	estadoFinal.setText("IPs iguales");
		        	estadoFinal.setForeground(new Color(204, 170, 0));
		        	
		        	estadoInicio.setText("IPs iguales");
		        	estadoInicio.setForeground(new Color(204, 170, 0));}
            }
	     }
  }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == botonComenzar) {

            String IpInicio = cajaIpInicio.getText();
            String IpFinal = cajaIpFinal.getText();
            String txTiempoMax = cajaEspMaximo.getText();

            if (!IpInicio.isEmpty() && !IpFinal.isEmpty() ) {
                try {
                    int tiempoMax = Integer.parseInt(txTiempoMax);

                    if (!scanner.modelo.Metodos.validarIp(IpInicio)) {
                        JOptionPane.showMessageDialog(this, "La IP inicial no es válida.");
                    } else {
                    	
                    	// Guarda los digitos de cada IP
                    	
                      	String[] listaIpFinal = (IpFinal.split("\\."));         	
                    	int Di = Integer.parseInt(listaIpFinal[0]);
                    	int Ci = Integer.parseInt(listaIpFinal[1]);
                    	int Bi = Integer.parseInt(listaIpFinal[2]); 
                    	int Ai = Integer.parseInt(listaIpFinal[3]);
                    	
                    	String[] listaIpInicial = (IpInicio.split("\\."));
                    	int Df = Integer.parseInt(listaIpInicial[0]);
                    	int Cf = Integer.parseInt(listaIpFinal[1]);
                    	int Bf = Integer.parseInt(listaIpFinal[2]); 
                    	int Af = Integer.parseInt(listaIpFinal[3]);
     
                    	for (int i = 0; i < 5; i++) {
                    	    System.out.println(i);
                    	}
                    	
              
                        new VentanaCarga().setVisible(true);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "El tiempo máximo debe ser un número válido.");
                }
            }

        } else if (source == botonLimpiar) {
            cajaIpInicio.setText("");
            cajaIpFinal.setText("");
            cajaEspMaximo.setText("");
            estadoInicio.setText("");
            estadoFinal.setText("");
        }
    }
}
