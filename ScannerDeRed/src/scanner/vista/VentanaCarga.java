package scanner.vista;

import javax.swing.*;
import java.awt.*;

public class VentanaCarga extends JFrame {

    private JProgressBar barra;
    private JLabel texto1;
    private JLabel textoCont;

    public VentanaCarga() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Scanner de red : Tarea en proceso");
        setSize(500, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel para los textos alineados a la izquierda con margen
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10)); // margen izquierdo

        texto1 = new JLabel("Pasando por la ip: ");
        textoCont = new JLabel("IP's escaneadas: ");

        panelTextos.add(texto1);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 5))); // espacio entre textos
        panelTextos.add(textoCont);

        add(panelTextos, BorderLayout.NORTH);

        // Panel central con la barra
        JPanel lamina = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        barra = new JProgressBar(0, 100);
        barra.setValue(0);
        barra.setStringPainted(true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        lamina.add(barra, gbc);

        add(lamina, BorderLayout.CENTER);

        // Simulación de progreso con Timer (solo para ver cómo sube la barra)
        Timer timer = new Timer(100, e -> {
            int valor = barra.getValue();
            if (valor < 100) {
                barra.setValue(valor + 1);
                texto1.setText("Pasando por la ip: 192.168.1." + valor);
                textoCont.setText("IP's escaneadas: " + valor);
            } else {
                ((Timer) e.getSource()).stop();
                texto1.setText("Escaneo completo.");
            }
        });
        timer.start();
    }
}
