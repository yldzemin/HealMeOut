package astratwin.cache;

import astratwin.domain.Baseline;
import astratwin.domain.ConditionProbability;
import astratwin.domain.Environment;
import astratwin.domain.MissionTag;
import astratwin.json.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Reads the cache files written by backend/ingest (envelope: BACKEND §1.1). */
public final class CacheLoader {
    private CacheLoader() {
    }

    /** Walks up from {@code start} until it finds {@code data/cache}. */
    public static Path findCacheDir(Path start) {
        for (Path p = start.toAbsolutePath(); p != null; p = p.getParent()) {
            Path candidate = p.resolve("data").resolve("cache");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("data/cache not found above " + start.toAbsolutePath());
    }

    public static CacheData load(Path dir) throws IOException {
        Map<String, Object> baselines = envelope(dir.resolve("baselines.json"));
        Map<String, Object> dose = envelope(dir.resolve("iss_dose.json"));
        Map<String, Object> imm = envelope(dir.resolve("imm_leo.json"));
        return new CacheData(
                items(baselines).stream().map(CacheLoader::baseline).toList(),
                items(dose).stream().map(CacheLoader::environment).toList(),
                items(imm).stream().map(m -> new ConditionProbability(str(m, "condition"), num(m, "probability"))).toList(),
                str(baselines, "source"), str(dose, "source"), str(imm, "source"));
    }

    private static Map<String, Object> envelope(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException(file + " is missing; run backend/ingest first");
        }
        return JsonParser.parseObject(Files.readString(file));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(Map<String, Object> envelope) {
        return (List<Map<String, Object>>) envelope.get("items");
    }

    @SuppressWarnings("unchecked")
    private static Baseline baseline(Map<String, Object> m) {
        Map<String, Object> t = (Map<String, Object>) m.get("mission");
        MissionTag mission = t == null ? null : new MissionTag(str(t, "name"), str(t, "kind"), str(t, "start"),
                str(t, "end"), str(t, "orbit"), str(t, "crew"), str(t, "hostedBy"), str(t, "studyId"), str(t, "link"));
        return new Baseline(str(m, "crewId"), str(m, "source"), str(m, "analyte"), str(m, "timepoint"),
                num(m, "value"), str(m, "unit"), optNum(m, "refLow"), optNum(m, "refHigh"), mission);
    }

    private static Environment environment(Map<String, Object> m) {
        return new Environment(optNum(m, "co2_mmHg"), optNum(m, "temp_C"), optNum(m, "rh_pct"),
                optNum(m, "doseRate_uGy_h"), optNum(m, "doseEqRate_uSv_h"), str(m, "instrument"),
                Instant.parse(str(m, "timestamp")), str(m, "provenance"));
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : v.toString();
    }

    private static double num(Map<String, Object> m, String key) {
        Double v = optNum(m, key);
        if (v == null) {
            throw new IllegalArgumentException("missing number '" + key + "' in " + m);
        }
        return v;
    }

    private static Double optNum(Map<String, Object> m, String key) {
        return m.get(key) instanceof Double d ? d : null;
    }
}
