package scanner.modelo;

import scanner.modelo.ResultadoEscaneo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Metodos: mantiene los métodos originales y agrega versiones optimizadas
 * para ping y reverse DNS con cache.
 */
public class Metodos {

    // ---------------- (Tus métodos originales) ----------------

    /** Valida que la cadena ip tenga formato correcto de IPv4 */
    public static boolean validarIp(String ip) {
        String regex =
            "^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\\.){3}(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})$";
        return ip.matches(regex);
    }

    /** Valida que la cadena num pueda convertirse a entero */
    public static boolean validarNums(String num) {
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /** Convierte una IP en formato "x.x.x.x" a un número long para ordenamiento */
    public static long ipToLong(String ip) {
        String[] p = ip.trim().split("\\.");
        return Long.parseLong(p[0]) * 256L * 256L * 256L +
               Long.parseLong(p[1]) * 256L * 256L +
               Long.parseLong(p[2]) * 256L +
               Long.parseLong(p[3]);
    }

    // ---------------- Método de prueba (mantengo) ----------------

    /** Método de prueba que ejecuta un ping a 127.0.0.1 y muestra la salida */
    public void prueba() {
        try {
            // Configura el comando para cmd en Windows y cambia la página de código
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp 65001 && ping -a 127.0.0.1");
            pb.redirectErrorStream(true); // Redirige errores al mismo flujo de salida
            Process proceso = pb.start(); // Inicia el proceso

            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), "Cp850"))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    System.out.println(linea); // Muestra cada línea de salida
                }
            }

            int codigoSalida = proceso.waitFor(); // Espera que termine el proceso
            System.out.println("Proceso terminado con código: " + codigoSalida);

        } catch (Exception e) {
            e.printStackTrace(); // Muestra errores si falla la ejecución
        }
    }

    // ---------------- Métodos de red originales ----------------

    /** Realiza un ping a la IP y llena el ResultadoEscaneo (tu version original) */
    public static void hacerPing(String ip, ResultadoEscaneo res, int tiempoMax) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            boolean reachable = inet.isReachable(tiempoMax); // Ping con timeout
            res.setMensajeError(null);
            res.setConexion(reachable);
            res.setTiempoRespuesta("tiempo<1m"); // Placeholder, siempre <1m
            res.setTtl(reachable ? 128 : -1); // TTL fijo o -1 si no responde
            String host = inet.getHostName();
            res.setHost(host != null && !host.equals(ip) ? host : ""); // Host solo si es diferente
        } catch (Exception e) {
            // Si falla, llena con valores por defecto
            res.setConexion(false);
            res.setTiempoRespuesta("tiempo<1m");
            res.setTtl(-1);
            res.setHost("");
            res.setMensajeError(e.getMessage());
        }
    }

    /** Realiza nslookup a la IP y llena el ResultadoEscaneo (tu version original) */
    public static void hacerNslookup(String ip, ResultadoEscaneo res) {
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("nslookup", ip);
            Process proceso = pb.start();
            reader = new BufferedReader(new InputStreamReader(proceso.getInputStream(), "Cp850"));

            String linea;
            String hostNslookup = "";
            String servidorDNS = "";
            while ((linea = reader.readLine()) != null) {
                String l = linea.trim().toLowerCase();
                if (l.startsWith("servidor") || l.startsWith("server")) {
                    String[] parts = linea.split(":");
                    if (parts.length >= 2) servidorDNS = parts[1].trim();
                }
                if (l.startsWith("nombre") || l.startsWith("name")) {
                    String[] parts = linea.split(":");
                    if (parts.length >= 2) hostNslookup = parts[1].trim();
                }
            }
            proceso.waitFor();

            if (!hostNslookup.isEmpty()) {
                res.setHost(hostNslookup);
                res.setHostServer(hostNslookup);
            } else {
                res.setHostServer("");
            }
            res.setServidorDNS(servidorDNS != null ? servidorDNS : "");
        } catch (Exception e) {
            // En caso de error deja vacío
            res.setHostServer("");
            res.setServidorDNS("");
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        }
    }

    // ---------------- Helpers para exportaciones (mantengo) ----------------

    /** Retorna "-" si la cadena es null o vacía */
    public static String dashOr(String s) {
        if (s == null) return "-";
        String t = s.trim();
        return t.isEmpty() ? "-" : t;
    }

    /** Convierte string de tiempo a número long para ordenamiento */
    public static long parseTiempoToLong(String tiempo) {
        if (tiempo == null) return Long.MAX_VALUE;
        String digits = tiempo.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return Long.MAX_VALUE;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    /** Escapa caracteres especiales de CSV y agrega comillas si es necesario */
    public static String escapeCsv(String value, char sep) {
        if (value == null) return "";
        boolean needQuotes = value.indexOf(sep) >= 0 || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (needQuotes) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    /** Genera una fila de texto con ancho fijo para exportación TXT */
    public static String fixedRow(String[] cols, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            String cell = cols[i] == null ? "-" : cols[i];
            if (cell.length() > widths[i]) {
                cell = cell.substring(0, Math.max(0, widths[i] - 1)) + "…";
            }
            line.append(String.format("%-" + widths[i] + "s", cell));
            if (i < cols.length - 1) line.append("  ");
        }
        return line.toString();
    }

    /** Repite un carácter n veces */
    public static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(ch);
        return sb.toString();
    }

    /** Devuelve un array con la misma cadena repetida n veces */
    public static String[] repeatString(String s, int n) {
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) arr[i] = s;
        return arr;
    }

    // ---------------- Nuevas optimizaciones (no eliminan nada) ----------------

    // Cache concurrente para reverse-DNS
    private static final Map<String, String> dnsCache = new ConcurrentHashMap<>();

    /**
     * Ping multiplataforma optimizado:
     * - Ejecuta el comando nativo "ping -n 1" (Windows) o "ping -c 1" (Unix)
     * - Controla el timeout usando Process.waitFor(timeout, TimeUnit.MILLISECONDS)
     * - Repite hasta 'attempts' veces (por si hay paquetes perdidos)
     *
     * Este enfoque evita depender de flags que cambian entre distros/macOS.
     */
    public static void hacerPingOptimizado(String ip, ResultadoEscaneo res, int tiempoMaxMs, int attempts) {
        boolean reachable = false;
        String tiempoStr = "-"; // por defecto "-" (indica no disponible / no responde)
        int ttl = -1;
        String lastError = null;

        final String os = System.getProperty("os.name").toLowerCase();
        final boolean isWindows = os.contains("win");
        java.nio.charset.Charset charsetToUse = isWindows ? java.nio.charset.Charset.forName("Cp850")
                                                          : java.nio.charset.Charset.forName("UTF-8");

        // Patterns para capturar time/tiempo y ttl (case insensitive)
        Pattern timePattern = Pattern.compile("(?i)(?:time|tiempo)\\s*[=<>]?\\s*([0-9]+)");
        Pattern timeLessOnePattern = Pattern.compile("(?i)(?:time|tiempo)\\s*[=<>]?\\s*<\\s*1");
        Pattern ttlPattern = Pattern.compile("(?i)ttl\\s*[=:\\s]?\\s*([0-9]+)");

        for (int a = 0; a < Math.max(1, attempts) && !reachable; a++) {
            Process p = null;
            BufferedReader br = null;
            try {
                String[] cmd;
                if (isWindows) {
                    // Windows: usar cmd para asegurar parsing correcto
                    cmd = new String[]{"cmd.exe", "/c", "ping", "-n", "1", ip};
                } else {
                    // Unix-like (Linux/macOS)
                    cmd = new String[]{"ping", "-c", "1", ip};
                }

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                p = pb.start();

                // Espera con timeout; si no termina, destruye el proceso
                boolean finished = p.waitFor(tiempoMaxMs + 500, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (!finished) {
                    try { p.destroyForcibly(); } catch (Exception ignored) {}
                    lastError = "timeout";
                    continue;
                }

                br = new BufferedReader(new InputStreamReader(p.getInputStream(), charsetToUse));
                String line;
                while ((line = br.readLine()) != null) {
                    String l = line.trim();

                    // ttl
                    Matcher mTtl = ttlPattern.matcher(l);
                    if (mTtl.find()) {
                        try { ttl = Integer.parseInt(mTtl.group(1)); } catch (Exception ignored) {}
                        // marcar reachable si TTL viene junto con respuesta
                        reachable = true;
                    }

                    // time exacto (ej: time=123ms OR tiempo=123ms)
                    Matcher mTime = timePattern.matcher(l);
                    if (mTime.find()) {
                        String numt = mTime.group(1);
                        if (numt != null && !numt.isEmpty()) {
                            tiempoStr = numt + "ms";
                            reachable = true;
                        }
                    } else {
                        // case: "time<1ms" o "tiempo<1ms"
                        Matcher mLt = timeLessOnePattern.matcher(l);
                        if (mLt.find()) {
                            tiempoStr = "0ms"; // tratar <1ms como 0ms para poder ordenar
                            reachable = true;
                        }
                    }
                }

                if (reachable) {
                    // Intentar extraer host (sin nslookup)
                    try {
                        InetAddress inet = InetAddress.getByName(ip);
                        String host = inet.getHostName();
                        res.setHost(host != null && !host.equals(ip) ? host : "");
                    } catch (Exception ignored) {
                        res.setHost("");
                    }
                    // si todavia no teniamos tiempo, dejarlo en "-" o 0ms ya seteado
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                lastError = "interrupted";
                break;
            } catch (IOException ioe) {
                lastError = ioe.getMessage();
            } finally {
                try { if (br != null) br.close(); } catch (Exception ignored) {}
                if (p != null && p.isAlive()) {
                    try { p.destroyForcibly(); } catch (Exception ignored) {}
                }
            }

        // Llenar resultado
        res.setConexion(reachable);
        res.setTiempoRespuesta(reachable ? tiempoStr : "-");
        res.setTtl(reachable ? ttl : -1);
        res.setMensajeError(lastError);
        }
    


        // Intentar obtener host si responde (no llamar a nslookup para performance)
        if (reachable) {
            try {
                InetAddress inet = InetAddress.getByName(ip);
                String host = inet.getHostName();
                res.setHost(host != null && !host.equals(ip) ? host : "");
            } catch (Exception ignored) {
                res.setHost("");
            }
        } else {
            res.setHost("");
        }
    }

    /**
     * Lookup reverse-DNS usando InetAddress con cache.
     * Rellena hostServer y servidorDNS (este ultimo queda vacío, si necesitás servidor DNS real seguí usando nslookup).
     */
    public static void lookupReverseDnsCached(String ip, ResultadoEscaneo res) {
        try {
            if (dnsCache.containsKey(ip)) {
                String cached = dnsCache.get(ip);
                res.setHostServer(cached);
                res.setServidorDNS("");
                if (!cached.equals("") && !cached.equals(ip)) res.setHost(cached);
                return;
            }

            try {
                InetAddress inet = InetAddress.getByName(ip);
                String canonical = inet.getCanonicalHostName();
                if (canonical == null) canonical = "";
                dnsCache.put(ip, canonical);
                res.setHostServer(canonical);
                res.setServidorDNS("");
                if (!canonical.equals("") && !canonical.equals(ip)) res.setHost(canonical);
            } catch (Exception e) {
                dnsCache.put(ip, "");
                res.setHostServer("");
                res.setServidorDNS("");
            }
        } catch (Exception ignored) {
            res.setHostServer("");
            res.setServidorDNS("");
        }
    }

}
