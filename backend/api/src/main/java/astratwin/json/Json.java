package astratwin.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal JSON writer with no dependencies.
 *
 * <p>Records are written as objects whose keys are the record component names, so the domain
 * records must use the field names of TECH_OVERVIEW §4. Null components are left out.
 */
public final class Json {
    private Json() {
    }

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(out, value);
        return out.toString();
    }

    private static void write(StringBuilder out, Object value) {
        switch (value) {
            case null -> out.append("null");
            case String s -> string(out, s);
            case Double d -> number(out, d);
            case Float f -> number(out, f.doubleValue());
            case Number n -> out.append(n);
            case Boolean b -> out.append(b);
            case Character c -> string(out, c.toString());
            case Enum<?> e -> string(out, e.toString());
            case Instant i -> string(out, i.toString());
            case Optional<?> o -> write(out, o.orElse(null));
            case Map<?, ?> m -> map(out, m);
            case Iterable<?> it -> array(out, it);
            case Record r -> record(out, r);
            default -> string(out, value.toString());
        }
    }

    private static void number(StringBuilder out, double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            out.append("null");
        } else if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            out.append((long) d);
        } else {
            out.append(d);
        }
    }

    private static void map(StringBuilder out, Map<?, ?> map) {
        out.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            if (!first) {
                out.append(',');
            }
            first = false;
            string(out, String.valueOf(e.getKey()));
            out.append(':');
            write(out, e.getValue());
        }
        out.append('}');
    }

    private static void array(StringBuilder out, Iterable<?> items) {
        out.append('[');
        boolean first = true;
        for (Object item : items) {
            if (!first) {
                out.append(',');
            }
            first = false;
            write(out, item);
        }
        out.append(']');
    }

    private static void record(StringBuilder out, Record record) {
        out.append('{');
        boolean first = true;
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            Object value;
            try {
                value = component.getAccessor().invoke(record);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("cannot read " + component.getName(), e);
            }
            if (value == null) {
                continue;
            }
            if (!first) {
                out.append(',');
            }
            first = false;
            string(out, component.getName());
            out.append(':');
            write(out, value);
        }
        out.append('}');
    }

    private static void string(StringBuilder out, String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
