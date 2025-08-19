package scanner.modelo;

public class ResultadoEscaneo {
    private String ip;
    private boolean conexion;
    private String host;
    private String tiempoRespuesta; // String para "tiempo<1m"
    private int ttl;
    private String hostServer;
    private String servidorDNS;

    public ResultadoEscaneo(String ip, boolean conexion, String host, String tiempoRespuesta, int ttl, String hostServer, String servidorDNS) {
        this.ip = ip;
        this.conexion = conexion;
        this.host = host;
        this.tiempoRespuesta = tiempoRespuesta;
        this.ttl = ttl;
        this.hostServer = hostServer;
        this.servidorDNS = servidorDNS;
    }

    public String getIp() { return ip; }
    public boolean isConexion() { return conexion; }
    public String getHost() { return host; }
    public String getTiempoRespuesta() { return tiempoRespuesta; }
    public int getTtl() { return ttl; }
    public String getHostServer() { return hostServer; }
    public String getServidorDNS() { return servidorDNS; }
}
