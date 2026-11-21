package retro;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.*;

/** (JDK). Renderizar las vistas y consumir los microservicios por HTTP. */
public final class App {
    private static final Path STATIC = Path.of(System.getProperty("static.dir", "static")).toAbsolutePath().normalize();
    private static final Pattern P_ITEM = Pattern.compile("^/items/(\\d+)(/editar|/cumplida|/eliminar)?$");
    private static final Pattern P_SPRINT_DEL = Pattern.compile("^/sprints/(\\d+)/eliminar$");

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", App::handle);
        server.start();
        System.out.println("Front Retrospectivas en http://localhost:" + port);
    }

    private static void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod(), path = ex.getRequestURI().getPath();
        try {
            if (method.equals("GET") && (path.startsWith("/public/") || path.startsWith("/resources/"))) { estatico(ex, path); return; }
            Map<String, List<String>> f = method.equals("POST") ? form(ex) : Map.of();
            Matcher m;

            if (method.equals("GET") && path.equals("/")) redirect(ex, "/retrospectivas");
            else if (method.equals("GET") && path.equals("/retrospectivas")) {
                var sprints = Api.sprints();
                Map<Long, List<Map<String, Object>>> items = new HashMap<>();
                for (var s : sprints) { long id = ((Number) s.get("id")).longValue(); items.put(id, Api.itemsDeSprint(id)); }
                html(ex, 200, Html.retrospectivas(sprints, items));
            }
            else if (method.equals("GET") && path.equals("/retrospectivas/nueva")) {
                var ultimo = Api.ultimoSprint();
                var acciones = ultimo == null ? List.<Map<String, Object>>of() : Api.accionesDeSprint(((Number) ultimo.get("id")).longValue());
                html(ex, 200, Html.formulario(ultimo, acciones));
            }
            else if (method.equals("POST") && path.equals("/retrospectivas")) { registrar(f); redirect(ex, "/retrospectivas"); }
            else if (method.equals("POST") && path.equals("/items")) {
                if (!one(f, "descripcion").isBlank()) Api.crearItem(itemBody(f, true));
                redirect(ex, "/retrospectivas");
            }
            else if ((m = P_SPRINT_DEL.matcher(path)).matches() && method.equals("POST")) { Api.eliminarSprint(Long.parseLong(m.group(1))); redirect(ex, "/retrospectivas"); }
            else if ((m = P_ITEM.matcher(path)).matches()) {
                long id = Long.parseLong(m.group(1)); String sub = m.group(2);
                if (method.equals("GET") && "/editar".equals(sub)) {
                    var it = Api.item(id);
                    if (it == null) redirect(ex, "/retrospectivas"); else html(ex, 200, Html.editar(it));
                }
                else if (method.equals("POST") && sub == null) { Api.actualizarItem(id, itemBody(f, false)); redirect(ex, "/retrospectivas"); }
                else if (method.equals("POST") && "/cumplida".equals(sub)) { Api.cumplida(id, "1".equals(one(f, "cumplida"))); redirect(ex, "/retrospectivas"); }
                else if (method.equals("POST") && "/eliminar".equals(sub)) { Api.eliminarItem(id); redirect(ex, "/retrospectivas"); }
                else html(ex, 404, Html.error("Ruta no encontrada"));
            }
            else html(ex, 404, Html.error("Ruta no encontrada"));
        } catch (Exception e) {
            e.printStackTrace();
            html(ex, 502, Html.error(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /** Caso de uso principal (igual que registrarRetrospectivaCompleta del monolito). */
    private static void registrar(Map<String, List<String>> f) throws Exception {
        // 1. Evaluar acciones del sprint anterior (lote) -> microservicio acciones
        List<Map<String, Object>> lote = new ArrayList<>();
        for (var e : f.entrySet()) {
            Matcher m = Pattern.compile("^acciones_previas\\[(\\d+)]$").matcher(e.getKey());
            if (m.matches()) lote.add(Map.of("id", Long.parseLong(m.group(1)), "cumplida", "1".equals(e.getValue().get(0))));
        }
        Api.evaluarLote(lote);

        // 2. Crear el sprint -> microservicio sprints
        Map<String, Object> sprint = Api.crearSprint(Map.of("nombre", one(f, "nombre"), "fecha_inicio", one(f, "fecha_inicio"), "fecha_fin", one(f, "fecha_fin")));
        long sprintId = ((Number) sprint.get("id")).longValue();

        // 3. Crear cada ítem no vacío -> microservicio items
        List<String> desc = f.getOrDefault("descripciones[]", List.of()), cats = f.getOrDefault("categorias[]", List.of()), fechas = f.getOrDefault("fechas_revision[]", List.of());
        for (int i = 0; i < desc.size(); i++) {
            if (desc.get(i).isBlank()) continue;
            Map<String, Object> it = new LinkedHashMap<>();
            it.put("sprint_id", sprintId);
            it.put("categoria", i < cats.size() ? cats.get(i) : "otro");
            it.put("descripcion", desc.get(i).trim());
            it.put("fecha_revision", i < fechas.size() && !fechas.get(i).isBlank() ? fechas.get(i) : null);
            Api.crearItem(it);
        }
    }

    private static Map<String, Object> itemBody(Map<String, List<String>> f, boolean conSprint) {
        Map<String, Object> b = new LinkedHashMap<>();
        if (conSprint) b.put("sprint_id", Long.parseLong(one(f, "sprint_id")));
        b.put("categoria", one(f, "categoria").isBlank() ? "otro" : one(f, "categoria"));
        b.put("descripcion", one(f, "descripcion").trim());
        b.put("fecha_revision", one(f, "fecha_revision").isBlank() ? null : one(f, "fecha_revision"));
        if (f.containsKey("cumplida")) b.put("cumplida", "1".equals(one(f, "cumplida")));
        return b;
    }

    // ---------- utilidades HTTP ----------
    private static String one(Map<String, List<String>> f, String k) { var l = f.get(k); return l == null || l.isEmpty() ? "" : l.get(0); }

    private static Map<String, List<String>> form(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> m = new LinkedHashMap<>();
        if (body.isBlank()) return m;
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            String k = URLDecoder.decode(i < 0 ? pair : pair.substring(0, i), StandardCharsets.UTF_8);
            String v = i < 0 ? "" : URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8);
            m.computeIfAbsent(k, x -> new ArrayList<>()).add(v);
        }
        return m;
    }

    private static void html(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private static void redirect(HttpExchange ex, String to) throws IOException {
        ex.getResponseHeaders().set("Location", to);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static void estatico(HttpExchange ex, String path) throws IOException {
        Path file = STATIC.resolve(path.substring(1)).normalize();
        if (!file.startsWith(STATIC) || !Files.isRegularFile(file)) { ex.sendResponseHeaders(404, -1); ex.close(); return; }
        String n = file.getFileName().toString().toLowerCase();
        String type = n.endsWith(".css") ? "text/css" : n.endsWith(".jpg") ? "image/jpeg" : n.endsWith(".gif") ? "image/gif" : n.endsWith(".png") ? "image/png"
                : n.endsWith(".ttf") ? "font/ttf" : n.endsWith(".otf") ? "font/otf" : "application/octet-stream";
        byte[] b = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
