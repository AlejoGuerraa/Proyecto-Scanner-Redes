package scanner.main;

import scanner.vista.VentanaPrincipal;
import scanner.modelo.Metodos;

public class Main {
    public static void main(String[] args) {
        // Mostrar ventana principal
        VentanaPrincipal venPrincipal = new VentanaPrincipal();
        venPrincipal.setVisible(true);
        
        Metodos m = new Metodos();
        //m.prueba();
    }
}
