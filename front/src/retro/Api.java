package retro;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Cliente HTTP hacia los 3 microservicios*/
public final class Api {
    private static final String SPRINTS  = env("SPRINTS_URL",  "http://127.0.0.1:8081");
    private static final String ITEMS    = env("ITEMS_URL",    "http://127.0.0.1:8082");
    private static final String ACCIONES = env("ACCIONES_URL", "http://127.0.0.1:8083");

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private static String env(String k, String d) { String v = System.getenv(k); return v == null || v.isBlank() ? d : v; }

    private record Res(int status, String body) {}

    private static Res call(String method, String url, Object jsonBody) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).header("Accept", "application/json");
        if (jsonBody == null) b.method(method, HttpRequest.BodyPublishers.noBody());
        else b.header("Content-Type", "application/json")
              .method(method, HttpRequest.BodyPublishers.ofString(Json.write(jsonBody), StandardCharsets.UTF_8));
        var r = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Res(r.statusCode(), r.body());
    }

    private static List<Map<String, Object>> getList(String url) throws Exception {
        Res r = call("GET", url, null);
        if (r.status == 204 || r.body.isBlank()) return List.of();
        if (r.status >= 400) throw new Exception("HTTP " + r.status + " en " + url);
        return Json.list(r.body);
    }

    private static Map<String, Object> getOne(String url) throws Exception {
        Res r = call("GET", url, null);
        if (r.status == 204 || r.status == 404 || r.body.isBlank()) return null;
        if (r.status >= 400) throw new Exception("HTTP " + r.status + " en " + url);
        return Json.obj(r.body);
    }

    private static void ok(Res r, String what) throws Exception {
        if (r.status >= 400) throw new Exception(what + " falló (HTTP " + r.status + ")");
    }

    public static List<Map<String, Object>> sprints() throws Exception { return getList(SPRINTS + "/sprints"); }
    public static Map<String, Object> ultimoSprint() throws Exception { return getOne(SPRINTS + "/sprints/ultimo"); }
    public static Map<String, Object> crearSprint(Map<String, Object> d) throws Exception {
        Res r = call("POST", SPRINTS + "/sprints", d); ok(r, "Crear sprint"); return Json.obj(r.body);
    }
    public static void eliminarSprint(long id) throws Exception { call("DELETE", SPRINTS + "/sprints/" + id, null); }

    public static List<Map<String, Object>> itemsDeSprint(long sprintId) throws Exception { return getList(ITEMS + "/items/sprint/" + sprintId); }
    public static Map<String, Object> item(long id) throws Exception { return getOne(ITEMS + "/items/" + id); }
    public static void crearItem(Map<String, Object> d) throws Exception { ok(call("POST", ITEMS + "/items", d), "Crear ítem"); }
    public static void actualizarItem(long id, Map<String, Object> d) throws Exception { ok(call("PUT", ITEMS + "/items/" + id, d), "Editar ítem"); }
    public static void eliminarItem(long id) throws Exception { call("DELETE", ITEMS + "/items/" + id, null); }

    public static List<Map<String, Object>> accionesDeSprint(long sprintId) throws Exception { return getList(ACCIONES + "/acciones/sprint/" + sprintId); }
    public static void cumplida(long id, boolean v) throws Exception {
        ok(call("PUT", ACCIONES + "/acciones/" + id + "/cumplida", Map.of("cumplida", v)), "Actualizar cumplimiento");
    }
    public static void evaluarLote(List<Map<String, Object>> acciones) throws Exception {
        if (!acciones.isEmpty()) ok(call("PUT", ACCIONES + "/acciones/evaluar-lote", Map.of("acciones", acciones)), "Evaluar acciones");
    }
}
