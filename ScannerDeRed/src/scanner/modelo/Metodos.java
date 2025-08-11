package scanner.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Metodos {

    public static boolean validarIp(String ip) {
        String regex =
            "^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\\.){3}(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})$";
        return ip.matches(regex);
       
    }
    public void prueba() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp 65001 && ping -a 127.0.0.1");
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            // Leer la salida del proceso
            try (BufferedReader lector = new BufferedReader(
            		new InputStreamReader(proceso.getInputStream(), "Cp850"))) {
                
            	// Imprime lineas hasta que no haya mas
            	String linea;
                while ((linea = lector.readLine()) != null) {
                    System.out.println(linea);
                }
            }

            int codigoSalida = proceso.waitFor();
            System.out.println("Proceso terminado con código: " + codigoSalida);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}