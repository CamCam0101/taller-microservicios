package retro;

import java.util.*;

/** Vistas HTML */
public final class Html {
    private Html() {}

    public static String esc(Object o) {
        if (o == null) return "";
        return o.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String fecha(Object o) { return o == null ? "" : o.toString().substring(0, Math.min(10, o.toString().length())); }
    private static long id(Map<String, Object> m) { return ((Number) m.get("id")).longValue(); }

    private static String layout(String title, String body, String style) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link rel="stylesheet" href="/public/css/tarjetas.css">
            </head>
            <body>
                <div class="contenedor"%s>
            %s
                </div>
            </body>
            </html>
            """.formatted(esc(title), style, body);
    }

    private static final String[][] CATS = {
        {"logro", "Logro"}, {"impedimento", "Impedimento"}, {"accion", "Acción"}, {"comentario", "Comentario"}, {"otro", "Otro"}};

    private static String opciones(String sel, boolean largo) {
        String[] largos = {"Logro (Lo que funcionó bien)", "Impedimento (A mejorar)", "Acción (Compromiso próximo sprint)", "Comentario", "Otro"};
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < CATS.length; k++)
            sb.append("<option value=\"").append(CATS[k][0]).append("\"").append(CATS[k][0].equals(sel) ? " selected" : "")
              .append(">").append(largo ? largos[k] : CATS[k][1]).append("</option>");
        return sb.toString();
    }

    private static String formPost(String action, String hidden, String btnClass, String label, String confirm) {
        return "<form method=\"post\" action=\"" + action + "\" style=\"display:inline\""
            + (confirm != null ? " onsubmit=\"return confirm('" + confirm + "');\"" : "") + ">" + hidden
            + "<button type=\"submit\" class=\"btn " + btnClass + " btn-sm\">" + label + "</button></form>";
    }

    // ================= Pantalla principal =================
    public static String retrospectivas(List<Map<String, Object>> sprints, Map<Long, List<Map<String, Object>>> itemsPorSprint) {
        StringBuilder b = new StringBuilder("""
            <div class="hud-bar"><span>♥ ♥ ♥ SCRUM SYSTEM</span><span>[■■■] ONLINE</span></div>
            <header class="header-app">
                <p class="subtitulo-pixel">❖ GESTIÓN ÁGIL Y MEJORA CONTINUA ❖</p>
                <h1>Registro de Retrospectivas<br> Scrum</h1>
                <p class="desc-header">Aquí controlaremos el historial de sprints, logros, impedimentos y seguimiento de compromisos</p>
                <div class="estrellas-deco">✦ &nbsp; ✧ &nbsp; ✦</div>
            """);
        if (!sprints.isEmpty())
            b.append("<div class=\"grupo-botones-inicio\"><a href=\"/retrospectivas/nueva\" class=\"btn btn-primario\">+ Nueva Retrospectiva</a></div>");
        b.append("</header>");

        if (sprints.isEmpty()) {
            b.append("""
                <div class="form-box estado-vacio">
                    <h3>Aún no hay retrospectivas registradas</h3>
                    <p>Crea la primera retrospectiva de tu equipo Scrum para empezar a documentar mejoras.</p>
                    <a href="/retrospectivas/nueva" class="btn btn-primario">+ Crear retrospectiva</a>
                </div>""");
            return layout("Registro de Retrospectivas Ágiles", b.toString(), "");
        }

        for (var sp : sprints) {
            long sid = id(sp);
            var items = itemsPorSprint.getOrDefault(sid, List.of());
            b.append("<section class=\"sprint-card\"><div class=\"sprint-header\"><div><h2>").append(esc(sp.get("nombre"))).append("</h2>")
             .append("<div class=\"sprint-fechas\">Del <strong>").append(esc(fecha(sp.get("fecha_inicio")))).append("</strong> al <strong>")
             .append(esc(fecha(sp.get("fecha_fin")))).append("</strong></div></div>")
             .append(formPost("/sprints/" + sid + "/eliminar", "", "btn-peligro", "Eliminar Sprint", "¿Seguro que deseas eliminar todo este Sprint y sus registros?"))
             .append("</div>");

            if (items.isEmpty()) b.append("<p style=\"color: #718096; margin-bottom: 15px;\">Este sprint aún no tiene ítems registrados.</p>");
            else {
                b.append("<div class=\"grid-items\">");
                for (var it : items) {
                    long iid = id(it);
                    String cat = String.valueOf(it.get("categoria"));
                    boolean cumplida = Boolean.TRUE.equals(it.get("cumplida"));
                    b.append("<article class=\"item-card cat-").append(esc(cat)).append("\"><div><span class=\"badge badge-").append(esc(cat)).append("\">")
                     .append(esc(cat)).append("</span><p class=\"item-desc\">").append(esc(it.get("descripcion")).replace("\n", "<br>")).append("</p></div><div>");
                    if (it.get("fecha_revision") != null)
                        b.append("<div class=\"item-meta\">Revisión: <strong>").append(esc(fecha(it.get("fecha_revision")))).append("</strong></div>");
                    if (cat.equals("accion"))
                        b.append("<div class=\"item-meta\">Estado: ").append(cumplida ? "<span class=\"estado-cumplida\">Cumplida</span>"
                                : "<span class=\"estado-pendiente\">Pendiente / No cumplida</span>").append("</div>");
                    b.append("<div class=\"item-acciones\">");
                    if (cat.equals("accion"))
                        b.append(formPost("/items/" + iid + "/cumplida", "<input type=\"hidden\" name=\"cumplida\" value=\"" + (cumplida ? 0 : 1) + "\">",
                                cumplida ? "btn-secundario" : "btn-exito", cumplida ? "Marcar Pendiente" : "Marcar Cumplida", null));
                    b.append("<a href=\"/items/").append(iid).append("/editar\" class=\"btn btn-primario btn-sm\">Editar</a>")
                     .append(formPost("/items/" + iid + "/eliminar", "", "btn-peligro", "Borrar", "¿Desea eliminar este registro?"))
                     .append("</div></div></article>");
                }
                b.append("</div>");
            }
            b.append("<form action=\"/items\" method=\"POST\" class=\"form-rapido\"><input type=\"hidden\" name=\"sprint_id\" value=\"").append(sid).append("\">")
             .append("<select name=\"categoria\" class=\"form-control\" style=\"max-width: 170px;\" required>").append(opciones("logro", false)).append("</select>")
             .append("<input type=\"text\" name=\"descripcion\" class=\"form-control\" style=\"flex: 1; min-width: 220px;\" placeholder=\"Agregar otro logro, impedimento o acción a este sprint...\" required>")
             .append("<input type=\"date\" name=\"fecha_revision\" class=\"form-control\" style=\"max-width: 160px;\" title=\"Fecha opcional de revisión\">")
             .append("<button type=\"submit\" class=\"btn btn-primario btn-sm\">+ Agregar</button></form></section>");
        }
        return layout("Registro de Retrospectivas Ágiles", b.toString(), "");
    }

    // ================= Formulario =================
    public static String formulario(Map<String, Object> sprintAnterior, List<Map<String, Object>> acciones) {
        StringBuilder b = new StringBuilder("""
            <header class="header-app"><div><h1>Nueva Reunión de Retrospectiva</h1>
            <p>Revisa compromisos anteriores y documenta los puntos del nuevo sprint</p></div>
            <a href="/retrospectivas" class="btn btn-secundario">Volver al historial</a></header>
            <form action="/retrospectivas" method="POST">""");
        if (sprintAnterior != null && !acciones.isEmpty()) {
            b.append("<div class=\"caja-revision\"><h3>Seguimiento: Acciones acordadas en ").append(esc(sprintAnterior.get("nombre"))).append("</h3>")
             .append("<p>Antes de iniciar la nueva retrospectiva, evalúa si el equipo cumplió los compromisos anteriores:</p>");
            for (var a : acciones) {
                long aid = id(a);
                boolean c = Boolean.TRUE.equals(a.get("cumplida"));
                b.append("<div class=\"accion-previa-item\"><div><strong>").append(esc(a.get("descripcion"))).append("</strong>");
                if (a.get("fecha_revision") != null)
                    b.append(" <span style=\"font-size: 0.8rem; color: #718096;\">(Revisión: ").append(esc(fecha(a.get("fecha_revision")))).append(")</span>");
                b.append("</div><div><label style=\"margin-right: 12px; cursor: pointer;\"><input type=\"radio\" name=\"acciones_previas[").append(aid)
                 .append("]\" value=\"1\"").append(c ? " checked" : "").append("> Cumplida</label><label style=\"cursor: pointer;\"><input type=\"radio\" name=\"acciones_previas[")
                 .append(aid).append("]\" value=\"0\"").append(c ? "" : " checked").append("> No cumplida</label></div></div>");
            }
            b.append("</div>");
        }
        b.append("""
            <div class="form-box"><h3 style="margin-bottom: 15px;">1. Información del Sprint</h3><div class="fila-grid">
              <div class="form-grupo"><label for="nombre">Nombre del Sprint *</label>
                <input type="text" id="nombre" name="nombre" class="form-control" placeholder="Ej: Sprint 1 - Módulo de Usuarios" required></div>
              <div class="form-grupo"><label for="fecha_inicio">Fecha de Inicio *</label>
                <input type="date" id="fecha_inicio" name="fecha_inicio" class="form-control" required></div>
              <div class="form-grupo"><label for="fecha_fin">Fecha de Fin *</label>
                <input type="date" id="fecha_fin" name="fecha_fin" class="form-control" required></div>
            </div></div>
            <div class="form-box">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                <h3>2. Puntos de la Retrospectiva (Logros, Impedimentos, Acciones)</h3>
                <button type="button" class="btn btn-exito btn-sm" onclick="agregarFilaItem()">+ Agregar otro punto</button></div>
              <div id="contenedor-items">
            """);
        String[][] filas = {{"logro", "Describe el punto discutido..."}, {"impedimento", "Describe el impedimento encontrado..."}, {"accion", "Describe la acción o acuerdo asumido..."}};
        for (int k = 0; k < filas.length; k++)
            b.append(fila(filas[k][0], filas[k][1], k == 0));
        b.append("""
              </div>
              <div style="margin-top: 20px; text-align: right;"><button type="submit" class="btn btn-primario">Guardar Retrospectiva</button></div>
            </div></form>
            <script>
            function agregarFilaItem() {
                const t = document.createElement('div');
                t.innerHTML = `%s`;
                document.getElementById('contenedor-items').appendChild(t.firstElementChild);
            }
            </script>""".formatted(fila("logro", "Escribe otro punto...", false).replace("`", "")));
        return layout("Nueva Retrospectiva Scrum", b.toString(), "");
    }

    private static String fila(String cat, String placeholder, boolean requerido) {
        return "<div class=\"fila-grid item-row\" style=\"margin-bottom: 12px; align-items: end;\">"
            + "<div class=\"form-grupo\" style=\"margin-bottom: 0;\"><select name=\"categorias[]\" class=\"form-control\">" + opciones(cat, true) + "</select></div>"
            + "<div class=\"form-grupo\" style=\"margin-bottom: 0; grid-column: span 2;\"><input type=\"text\" name=\"descripciones[]\" class=\"form-control\" placeholder=\""
            + placeholder + "\"" + (requerido ? " required" : "") + "></div>"
            + "<div class=\"form-grupo\" style=\"margin-bottom: 0;\"><input type=\"date\" name=\"fechas_revision[]\" class=\"form-control\" title=\"Fecha revisión (opcional)\"></div></div>";
    }

    // ================= Editar ítem =================
    public static String editar(Map<String, Object> it) {
        String cat = String.valueOf(it.get("categoria"));
        boolean c = Boolean.TRUE.equals(it.get("cumplida"));
        String extra = !cat.equals("accion") ? "" : """
            <div class="form-grupo"><label for="cumplida">Estado de cumplimiento (solo acciones)</label>
            <select id="cumplida" name="cumplida" class="form-control">
              <option value="0"%s>Pendiente / No cumplida</option><option value="1"%s>Cumplida</option></select></div>
            """.formatted(c ? "" : " selected", c ? " selected" : "");
        String body = """
            <header class="header-app"><div><h1>Editar Registro</h1><p>Actualiza la información del punto seleccionado</p></div>
            <a href="/retrospectivas" class="btn btn-secundario">Cancelar</a></header>
            <form action="/items/%d" method="POST" class="form-box">
              <div class="form-grupo"><label for="categoria">Categoría</label>
                <select id="categoria" name="categoria" class="form-control" required>%s</select></div>
              <div class="form-grupo"><label for="descripcion">Descripción</label>
                <textarea id="descripcion" name="descripcion" rows="4" class="form-control" required>%s</textarea></div>
              <div class="form-grupo"><label for="fecha_revision">Fecha de Revisión (opcional)</label>
                <input type="date" id="fecha_revision" name="fecha_revision" class="form-control" value="%s"></div>
              %s
              <div style="text-align: right; margin-top: 20px;"><button type="submit" class="btn btn-primario">Guardar Cambios</button></div>
            </form>""".formatted(id(it), opciones(cat, false), esc(it.get("descripcion")), esc(fecha(it.get("fecha_revision"))), extra);
        return layout("Editar Registro de Retrospectiva", body, " style=\"max-width: 650px;\"");
    }

    public static String error(String mensaje) {
        return layout("Error", "<div class=\"form-box estado-vacio\"><h3>No se pudo completar la operación</h3><p>" + esc(mensaje)
            + "</p><p>Verifica que los 3 microservicios estén encendidos (8081, 8082, 8083).</p><a href=\"/retrospectivas\" class=\"btn btn-primario\">Reintentar</a></div>", "");
    }
}
