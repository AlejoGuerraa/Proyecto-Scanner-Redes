// VentanaPrincipal.java
package scanner.vista;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VentanaPrincipal extends JFrame {

    private boolean modoOscuro = false;
    private JButton btnEscaner, btnNetstat, btnTema;
    private JLabel titulo;

    public VentanaPrincipal() {
        setTitle("IPVision");
        setSize(420, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        
        setIconImage(new ImageIcon(getClass().getResource("/lupa.png")).getImage());

        // ---------- Panel principal ----------
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        add(panel, BorderLayout.CENTER);

        // ---------- Título ----------
        titulo = new JLabel("Seleccione una opción");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(46, 134, 222)));
        titulo.setOpaque(false);
        panel.add(titulo);
        panel.add(Box.createRigidArea(new Dimension(0, 22)));

        // ---------- Botones ----------
        btnEscaner = crearBoton("Escaneo de redes");
        btnEscaner.addActionListener(e -> {
            new VentanaEscaner(modoOscuro).setVisible(true);
            dispose();
        });

        btnNetstat = crearBoton("Netstat / Información de red");
        btnNetstat.addActionListener(e -> {
            new VentanaNetstat(modoOscuro).setVisible(true);
            dispose();
        });

        btnTema = crearBoton("Alternar tema oscuro");
        btnTema.addActionListener(e -> {
            modoOscuro = !modoOscuro;
            actualizarTema(panel);
        });

        panel.add(btnEscaner);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(btnNetstat);
        panel.add(Box.createRigidArea(new Dimension(0, 22)));
        panel.add(btnTema);

        actualizarTema(panel);
    }

    private JButton crearBoton(String texto) {
        RoundedButton boton = new RoundedButton(texto);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        boton.setAlignmentX(CENTER_ALIGNMENT);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover dinámico según modo actual
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(modoOscuro ? new Color(100, 100, 100) : new Color(65, 150, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(modoOscuro ? Color.DARK_GRAY : new Color(46, 134, 222));
            }
        });
        return boton;
    }

    private void actualizarTema(JPanel panel) {
        Color bg = modoOscuro ? new Color(45, 45, 45) : new Color(245, 247, 250);
        Color fg = modoOscuro ? Color.WHITE : Color.BLACK;
        Color borderColor = modoOscuro ? new Color(100, 100, 100) : new Color(46, 134, 222);

        getContentPane().setBackground(bg);
        panel.setBackground(bg);

        // actualizar título (incluye subrayado)
        titulo.setForeground(fg);
        titulo.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, borderColor));

        // actualizar componentes dentro del panel
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(fg);
            } else if (c instanceof JButton) {
                c.setForeground(Color.WHITE);
                c.setBackground(modoOscuro ? Color.DARK_GRAY : new Color(46, 134, 222));
                // repaint for rounded button
                c.repaint();
            } else if (c instanceof JPanel) {
                c.setBackground(bg);
            }
        }

        // update frame decorations (optional subtle effect)
        SwingUtilities.updateComponentTreeUI(this);
    }

    // Clase interna para botones con bordes redondeados y pintura personalizada
    private static class RoundedButton extends JButton {
        private static final int ARC = 18;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setBackground(new Color(46, 134, 222));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // fondo redondeado
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

            // sombra sutil abajo (solo en modo claro no para complicar)
            // (Si se quiere, se puede agregar según preferencias)

            g2.dispose();

            // dibujar texto y demás elementos de JButton
            super.paintComponent(g);
        }

        @Override
        public void setBorder(Border border) {
            // mantenemos padding interno pero evitamos el borde rectangular estándar
            if (border == null) {
                super.setBorder(null);
            } else {
                super.setBorder(border);
            }
        }
    }

    public boolean isModoOscuro() {
        return modoOscuro;
    }

    // Para pruebas manuales (opcional):
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}
