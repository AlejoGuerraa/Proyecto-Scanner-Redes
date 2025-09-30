package scanner.modelo;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase que representa el resultado de escanear una IP
 */
public class ResultadoEscaneo {
    private String ip;                  // IP escaneada
    private boolean conexion;           // true si responde al ping, false si no
    private String host;                // Nombre del host (si se pudo obtener)
    private String tiempoRespuesta;     // Tiempo de respuesta como String ("tiempo<1m")
    private int ttl;                    // TTL de la respuesta
    private String hostServer;          // Host server desde nslookup
    private String servidorDNS;         // Servidor DNS obtenido en nslookup
    private String mensajeError;        // Mensaje de error si ocurre
    private Date fechaEscaneo;          // Fecha y hora del escaneo
    private List<Integer> puertosAbiertos; // Lista de puertos abiertos (para escaneo de puertos)

    // ---------------- Constructores ----------------

    // Constructor principal
    public ResultadoEscaneo(String ip, boolean conexion, String host, String tiempoRespuesta,
                            int ttl, String hostServer, String servidorDNS) {
        this.ip = ip;
        this.conexion = conexion;
        this.host = host;
        this.tiempoRespuesta = tiempoRespuesta;
        this.ttl = ttl;
        this.hostServer = hostServer;
        this.servidorDNS = servidorDNS;
        this.mensajeError = null;
        this.fechaEscaneo = new Date();
        this.puertosAbiertos = new ArrayList<>();
    }

    // Constructor vacío (útil para crear resultados en tiempo de escaneo múltiple)
    public ResultadoEscaneo() {
        this.ip = "";
        this.conexion = false;
        this.host = "";
        this.tiempoRespuesta = "";
        this.ttl = -1;
        this.hostServer = "";
        this.servidorDNS = "";
        this.mensajeError = null;
        this.fechaEscaneo = new Date();
        this.puertosAbiertos = new ArrayList<>();
    }

    // ---------------- Métodos utilitarios ----------------

    public long getTiempoRespuestaLong() {
        if (tiempoRespuesta == null || tiempoRespuesta.trim().isEmpty()) return Long.MAX_VALUE;
        String digits = tiempoRespuesta.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return Long.MAX_VALUE;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    // ---------------- Getters y Setters ----------------

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public boolean isConexion() { return conexion; }
    public void setConexion(boolean conexion) { this.conexion = conexion; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getTiempoRespuesta() { return tiempoRespuesta; }
    public void setTiempoRespuesta(String tiempoRespuesta) { this.tiempoRespuesta = tiempoRespuesta; }

    public int getTtl() { return ttl; }
    public void setTtl(int ttl) { this.ttl = ttl; }

    public String getHostServer() { return hostServer; }
    public void setHostServer(String hostServer) { this.hostServer = hostServer; }

    public String getServidorDNS() { return servidorDNS; }
    public void setServidorDNS(String servidorDNS) { this.servidorDNS = servidorDNS; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public Date getFechaEscaneo() { return fechaEscaneo; }
    public void setFechaEscaneo(Date fechaEscaneo) { this.fechaEscaneo = fechaEscaneo; }

    public List<Integer> getPuertosAbiertos() { return puertosAbiertos; }
    public void setPuertosAbiertos(List<Integer> puertosAbiertos) { this.puertosAbiertos = puertosAbiertos; }

    public void addPuertoAbierto(int puerto) {
        if (puertosAbiertos == null) puertosAbiertos = new ArrayList<>();
        puertosAbiertos.add(puerto);
    }
}
