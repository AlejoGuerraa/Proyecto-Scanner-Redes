// src/scanner/modelo/Metodos.java
package scanner.modelo;

import scanner.modelo.ResultadoEscaneo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Metodos: mantiene los métodos originales y agrega versiones optimizadas
 * para ping y reverse DNS con cache, y utilidades de ejecución de comandos.
 *
 * Nota: ejecutarComando ahora lee la salida en un hilo separado para evitar bloqueos
 * cuando la salida es grande o el proceso tarda.
 */
public class Metodos {

    // ---------------- (Tus métodos originales) ----------------

	    /**
	     * Obtiene un map PID -> Nombre de proceso usando tasklist.
	     * Solo Windows.
	     */
	    public static Map<String, String> obtenerProcesosPorPID() {
	        Map<String, String> pidMap = new HashMap<>();
	        String os = System.getProperty("os.name").toLowerCase();
	        if (!os.contains("win")) return pidMap;

	        try {
	            Process p = new ProcessBuilder("cmd.exe", "/c", "tasklist /fo csv /nh").start();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                // CSV: "Nombre","PID","Memoria","..."
	                String[] tokens = line.split("\",\"");
	                if (tokens.length >= 2) {
	                    String nombre = tokens[0].replace("\"","").trim();
	                    String pid = tokens[1].replace("\"","").trim();
	                    pidMap.put(pid, nombre);
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return pidMap;
	    }



    public static boolean validarIp(String ip) {
        String regex =
            "^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\\.){3}(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})$";
        return ip.matches(regex);
    }

    public static boolean validarNums(String num) {
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static long ipToLong(String ip) {
        String[] p = ip.trim().split("\\.");
        return Long.parseLong(p[0]) * 256L * 256L * 256L +
               Long.parseLong(p[1]) * 256L * 256L +
               Long.parseLong(p[2]) * 256L +
               Long.parseLong(p[3]);
    }

    public void prueba() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp 65001 && ping -a 127.0.0.1");
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), "Cp850"))) {
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

    public static void hacerPing(String ip, ResultadoEscaneo res, int tiempoMax) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            boolean reachable = inet.isReachable(tiempoMax);
            res.setMensajeError(null);
            res.setConexion(reachable);
            res.setTiempoRespuesta("tiempo<1m");
            res.setTtl(reachable ? 128 : -1);
            String host = inet.getHostName();
            res.setHost(host != null && !host.equals(ip) ? host : "");
        } catch (Exception e) {
            res.setConexion(false);
            res.setTiempoRespuesta("tiempo<1m");
            res.setTtl(-1);
            res.setHost("");
            res.setMensajeError(e.getMessage());
        }
    }

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
            res.setHostServer("");
            res.setServidorDNS("");
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        }
    }

    public static String dashOr(String s) {
        if (s == null) return "-";
        String t = s.trim();
        return t.isEmpty() ? "-" : t;
    }

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

    public static String escapeCsv(String value, char sep) {
        if (value == null) return "";
        boolean needQuotes = value.indexOf(sep) >= 0 || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (needQuotes) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

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

    public static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(ch);
        return sb.toString();
    }

    public static String[] repeatString(String s, int n) {
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) arr[i] = s;
        return arr;
    }

    // ---------------- Nuevas optimizaciones ----------------

    private static final Map<String, String> dnsCache = new ConcurrentHashMap<>();

    public static void hacerPingOptimizado(String ip, ResultadoEscaneo res, int tiempoMaxMs, int attempts) {
        boolean reachable = false;
        String tiempoStr = "-";
        int ttl = -1;
        String lastError = null;

        final String os = System.getProperty("os.name").toLowerCase();
        final boolean isWindows = os.contains("win");
        Charset charsetToUse = isWindows ? Charset.forName("Cp850") : Charset.forName("UTF-8");

        Pattern timePattern = Pattern.compile("(?i)(?:time|tiempo)\\s*[=<>]?\\s*([0-9]+)");
        Pattern timeLessOnePattern = Pattern.compile("(?i)(?:time|tiempo)\\s*[=<>]?\\s*<\\s*1");
        Pattern ttlPattern = Pattern.compile("(?i)ttl\\s*[=:\\s]?\\s*([0-9]+)");

        for (int a = 0; a < Math.max(1, attempts) && !reachable; a++) {
            Process p = null;
            BufferedReader br = null;
            try {
                String[] cmd = isWindows ? new String[]{"cmd.exe", "/c", "ping", "-n", "1", ip}
                                         : new String[]{"ping", "-c", "1", ip};

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                p = pb.start();

                boolean finished = p.waitFor(tiempoMaxMs + 500, TimeUnit.MILLISECONDS);
                if (!finished) {
                    try { p.destroyForcibly(); } catch (Exception ignored) {}
                    lastError = "timeout";
                    continue;
                }

                br = new BufferedReader(new InputStreamReader(p.getInputStream(), charsetToUse));
                String line;
                while ((line = br.readLine()) != null) {
                    String l = line.trim();

                    Matcher mTtl = ttlPattern.matcher(l);
                    if (mTtl.find()) {
                        try { ttl = Integer.parseInt(mTtl.group(1)); } catch (Exception ignored) {}
                        reachable = true;
                    }

                    Matcher mTime = timePattern.matcher(l);
                    if (mTime.find()) {
                        String numt = mTime.group(1);
                        if (numt != null && !numt.isEmpty()) {
                            tiempoStr = numt + "ms";
                            reachable = true;
                        }
                    } else {
                        Matcher mLt = timeLessOnePattern.matcher(l);
                        if (mLt.find()) {
                            tiempoStr = "0ms";
                            reachable = true;
                        }
                    }
                }

                if (reachable) {
                    try {
                        InetAddress inet = InetAddress.getByName(ip);
                        String host = inet.getHostName();
                        res.setHost(host != null && !host.equals(ip) ? host : "");
                    } catch (Exception ignored) {
                        res.setHost("");
                    }
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

            res.setConexion(reachable);
            res.setTiempoRespuesta(reachable ? tiempoStr : "-");
            res.setTtl(reachable ? ttl : -1);
            res.setMensajeError(lastError);
        }

        if (!res.isConexion()) res.setHost("");
    }

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

    // ---------------- Nueva utilidad: ejecutar comandos ----------------

    /**
     * Ejecuta un comando del sistema con timeout y retorna la salida en una lista de líneas.
     * Implementado para evitar bloqueos: la lectura de salida se hace en un thread separado.
     *
     * @param cmd arreglo con comando + args
     * @param timeoutMs tiempo máximo en ms para la ejecución
     * @return lista de líneas de salida
     * @throws IOException si el comando excede tiempo o falla
     * @throws InterruptedException si se interrumpe la espera
     */
    public static List<String> ejecutarComando(String[] cmd, long timeoutMs)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        final Process proceso = pb.start();

        final String os = System.getProperty("os.name").toLowerCase();
        final Charset cs = os.contains("win") ? Charset.forName("Cp850") : Charset.forName("UTF-8");

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<List<String>> future = exec.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> lines = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(proceso.getInputStream(), cs))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        lines.add(linea);
                    }
                } catch (IOException ioe) {
                    // rethrow to be handled by caller
                    throw ioe;
                }
                return lines;
            }
        });

        try {
            boolean finished = proceso.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                try { proceso.destroyForcibly(); } catch (Exception ignored) {}
                future.cancel(true);
                throw new IOException("Tiempo de espera excedido para el comando: " + String.join(" ", cmd));
            }
            // Si terminó, obtener la lista. Damos un pequeño timeout para que el lector termine.
            try {
                List<String> salida = future.get(2, TimeUnit.SECONDS);
                return salida;
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof IOException) throw (IOException) cause;
                throw new IOException("Error al leer salida del comando: " + cause.getMessage(), cause);
            } catch (TimeoutException te) {
                // lector no terminó en 2s: cancelamos y retornamos lo que haya
                future.cancel(true);
                return new ArrayList<>();
            }
        } finally {
            exec.shutdownNow();
        }
    }

    // ---------------- Métodos de escaneo de red ----------------

    /**
     * Escanea un puerto TCP en la IP especificada.
     */
    public static boolean escanearPuertoTCP(String ip, int puerto, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(ip, puerto), timeoutMs);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Escanea una IP (ping) y devuelve true si responde.
     */
    public static boolean escanearIP(String ip, int timeoutMs) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            return inet.isReachable(timeoutMs);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Escanea un rango de IPs y devuelve la lista de IPs activas.
     * ipStart e ipEnd deben estar en formato "192.168.1.10"
     */
    public static List<String> escanearRangoIPs(String ipStart, String ipEnd, int timeoutMs) {
        List<String> activas = new ArrayList<>();
        long start = ipToLong(ipStart);
        long end = ipToLong(ipEnd);
        for (long i = start; i <= end; i++) {
            String ip = longToIp(i);
            if (escanearIP(ip, timeoutMs)) {
                activas.add(ip);
            }
        }
        return activas;
    }

    /**
     * Escanea múltiples puertos en una IP y devuelve los puertos abiertos.
     */
    public static List<Integer> escanearPuertos(String ip, int[] puertos, int timeoutMs) {
        List<Integer> abiertos = new ArrayList<>();
        for (int p : puertos) {
            if (escanearPuertoTCP(ip, p, timeoutMs)) {
                abiertos.add(p);
            }
        }
        return abiertos;
    }
    
    

    /**
     * Convierte long a IP en formato string.
     */
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }

    /**
     * Escaneo combinado de rango de IPs y puertos.
     * Devuelve lista de resultados por IP y puertos abiertos.
     */
    public static List<ResultadoEscaneo> escanearRed(String ipStart, String ipEnd, int[] puertos, int timeoutMs) {
        List<ResultadoEscaneo> resultados = new ArrayList<>();
        List<String> ipsActivas = escanearRangoIPs(ipStart, ipEnd, timeoutMs);
        for (String ip : ipsActivas) {
            ResultadoEscaneo res = new ResultadoEscaneo();
            res.setIp(ip);
            res.setConexion(true);
            res.setPuertosAbiertos(escanearPuertos(ip, puertos, timeoutMs));
            lookupReverseDnsCached(ip, res); // nombre de host opcional
            resultados.add(res);
        }
        return resultados;
    }

}
