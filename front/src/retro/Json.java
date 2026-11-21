package retro;

import java.util.*;

/**(sin dependencias externas). */
public final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        Json p = new Json(text);
        p.ws();
        return p.value();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(String text) { return (Map<String, Object>) parse(text); }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> list(String text) { return (List<Map<String, Object>>) parse(text); }

    private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

    private Object value() {
        char c = s.charAt(i);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", i))  { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i))  { i += 4; return null; }
        int st = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        String n = s.substring(st, i);
        return (n.contains(".") || n.contains("e") || n.contains("E")) ? (Object) Double.valueOf(n) : (Object) Long.valueOf(n);
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; ws();
        if (s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws(); String k = string(); ws(); i++; ws(); // i++ salta el carácter ':' del JSON
            m.put(k, value()); ws();
            if (s.charAt(i++) == '}') return m;
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++; ws();
        if (s.charAt(i) == ']') { i++; return l; }
        while (true) {
            ws(); l.add(value()); ws();
            if (s.charAt(i++) == ']') return l;
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++; // comilla inicial
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                default -> sb.append(e); // \" \\ \/
            }
        }
    }

    // ---------- escritura usuario ----------
    public static String write(Object o) {
        StringBuilder sb = new StringBuilder();
        w(sb, o);
        return sb.toString();
    }

    private static void w(StringBuilder sb, Object o) {
        if (o == null) sb.append("null");
        else if (o instanceof Number || o instanceof Boolean) sb.append(o);
        else if (o instanceof Map<?, ?> m) {
            sb.append('{'); boolean first = true;
            for (var e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                w(sb, String.valueOf(e.getKey())); sb.append(':'); w(sb, e.getValue());
            }
            sb.append('}');
        } else if (o instanceof List<?> l) {
            sb.append('['); boolean first = true;
            for (Object x : l) { if (!first) sb.append(','); first = false; w(sb, x); }
            sb.append(']');
        } else {
            sb.append('"');
            for (char c : o.toString().toCharArray()) {
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
                }
            }
            sb.append('"');
        }
    }
}
