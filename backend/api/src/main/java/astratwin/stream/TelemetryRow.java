package astratwin.stream;

import astratwin.domain.Reading;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One line of the telemetry CSV. Columns follow the {@link Reading} entity (TECH_OVERVIEW §4.2):
 * {@value #HEADER}.
 */
public record TelemetryRow(
        Instant timestamp, String crewId, String metric, double value, String unit, String provenance,
        String measuredHow) {

    public static final String HEADER = "timestamp,crewId,metric,value,unit,provenance,measuredHow";

    public String toCsv() {
        return String.join(",", timestamp.toString(), field(crewId), field(metric), number(value), field(unit),
                field(provenance), field(measuredHow));
    }

    /** Parses one data line (not the header). Fields may be quoted with double quotes. */
    public static TelemetryRow parse(String line) {
        List<String> f = split(line);
        if (f.size() != 7) {
            throw new IllegalArgumentException("expected 7 CSV fields, got " + f.size() + ": " + line);
        }
        try {
            return new TelemetryRow(Instant.parse(f.get(0)), f.get(1), f.get(2), Double.parseDouble(f.get(3)),
                    f.get(4), f.get(5), f.get(6));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("bad telemetry line: " + line, e);
        }
    }

    public Reading toReading(String note) {
        return new Reading(crewId, metric, value, unit, timestamp, provenance, measuredHow, note);
    }

    public TelemetryRow at(Instant newTimestamp) {
        return new TelemetryRow(newTimestamp, crewId, metric, value, unit, provenance, measuredHow);
    }

    private static String number(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return Long.toString((long) v);
        }
        return String.format(Locale.ROOT, "%.2f", v).replaceAll("0+$", "");
    }

    private static String field(String s) {
        if (s == null) {
            return "";
        }
        if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && s.indexOf('\n') < 0) {
            return s;
        }
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
