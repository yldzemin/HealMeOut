package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.json.Json;
import astratwin.json.JsonParser;
import java.time.Instant;
import java.util.List;
import java.util.Map;

final class JsonTest {
    private JsonTest() {
    }

    static void register(Map<String, TestRunner.Test> tests) {
        tests.put("json: record keys match fields, nulls are left out, status is lower case", () -> {
            Alert a = Alert.open("x", "C1", Level.L2, "T \"q\"", null, "r", "e", "s", DataKind.VITALS,
                    Instant.parse("2026-11-14T00:00:00Z"));
            String json = Json.write(a);
            Check.isTrue(json.startsWith("{\"id\":\"x\",\"crewId\":\"C1\",\"level\":\"L2\",\"title\":\"T \\\"q\\\"\""), json);
            Check.isTrue(!json.contains("explanation"), "null field must be left out: " + json);
            Check.isTrue(json.contains("\"status\":\"open\""), json);
            Check.isTrue(json.contains("\"active\":true"), json);
        });
        tests.put("json: parse and write round trip", () -> {
            String text = "{\"a\":[1,2.5,-3e2],\"b\":{\"c\":\"\\u00e9\\n\"},\"d\":null,\"e\":true}";
            Map<String, Object> m = JsonParser.parseObject(text);
            Check.equal(List.of(1.0, 2.5, -300.0), m.get("a"), "numbers");
            Check.equal("é\n", ((Map<?, ?>) m.get("b")).get("c"), "escapes");
            Check.equal("{\"a\":[1,2.5,-300],\"b\":{\"c\":\"é\\n\"},\"e\":true}", Json.write(m), "round trip");
        });
    }
}
