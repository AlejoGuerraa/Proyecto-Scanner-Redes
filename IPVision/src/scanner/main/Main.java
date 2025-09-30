package scanner.main;

import scanner.vista.VentanaPrincipal;
import scanner.modelo.Metodos;

public class Main {
    public static void main(String[] args) {
        // Crear una instancia de la ventana principal de la aplicación
        VentanaPrincipal venPrincipal = new VentanaPrincipal();
        
        // Mostrar la ventana principal en pantalla
        venPrincipal.setVisible(true);
        
        // Crear instancia de la clase Metodos (para poder usar métodos no estáticos)
        Metodos m = new Metodos();
        
    }
}
