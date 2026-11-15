package astratwin.support;

import astratwin.json.Json;
import astratwin.json.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Local LLM through Ollama's HTTP API ({@code /api/chat}), using only the JDK HTTP client.
 * Nothing leaves the machine: the default URL is {@code http://127.0.0.1:11434}.
 */
public final class OllamaResponder implements SupportResponder {
    private static final Duration AVAILABILITY_CACHE = Duration.ofSeconds(15);
    private static final int HISTORY = 4;
    /** Fixed seed: the same message gets the same reply (reproducible demos and evaluation). */
    static final int SEED = 42;

    private final URI base;
    private final String model;
    private final Duration timeout;
    private final HttpClient http;
    private Instant checkedAt = Instant.EPOCH;
    private boolean lastAvailable;

    public OllamaResponder(URI base, String model, Duration timeout) {
        this.base = base;
        this.model = model;
        this.timeout = timeout;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @Override
    public String name() {
        return "local-llm:" + model;
    }

    /** True if Ollama answers and has the model; cached for 15 s. */
    @Override
    public synchronized boolean available() {
        if (Duration.between(checkedAt, Instant.now()).compareTo(AVAILABILITY_CACHE) < 0) {
            return lastAvailable;
        }
        checkedAt = Instant.now();
        try {
            HttpResponse<String> res = http.send(HttpRequest.newBuilder(base.resolve("/api/tags"))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            lastAvailable = res.statusCode() == 200 && hasModel(res.body());
        } catch (IOException | RuntimeException e) {
            lastAvailable = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lastAvailable = false;
        }
        return lastAvailable;
    }

    @Override
    public Optional<SupportReply> reply(SupportRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SupportPrompt.SYSTEM));
        messages.add(Map.of("role", "user", "content", SupportPrompt.EXAMPLE_USER));
        messages.add(Map.of("role", "assistant", "content", SupportPrompt.EXAMPLE_REPLY));
        List<SupportMessage> history = request.history();
        for (SupportMessage m : history.subList(Math.max(0, history.size() - HISTORY), history.size())) {
            messages.add(Map.of("role", m.author().equals("crew") ? "user" : "assistant", "content", m.text()));
        }
        SupportPrompt.hint(request.text()).ifPresent(h -> messages.add(Map.of("role", "system", "content", h)));
        messages.add(Map.of("role", "user", "content", request.text()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("keep_alive", "30m");
        body.put("messages", messages);
        body.put("options", Map.of("temperature", 0.4, "top_p", 0.9, "num_predict", 160, "num_ctx", 4096,
                "repeat_penalty", 1.1, "seed", SEED + request.attempt()));
        try {
            HttpResponse<String> res = http.send(HttpRequest.newBuilder(base.resolve("/api/chat"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(Json.write(body), StandardCharsets.UTF_8))
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() != 200) {
                return Optional.empty();
            }
            Object message = JsonParser.parseObject(res.body()).get("message");
            if (message instanceof Map<?, ?> m && m.get("content") instanceof String content && !content.isBlank()) {
                return Optional.of(new SupportReply(content.strip(), name(), List.of()));
            }
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private boolean hasModel(String tagsJson) {
        Object models = JsonParser.parseObject(tagsJson).get("models");
        if (!(models instanceof List<?> list)) {
            return false;
        }
        for (Object o : list) {
            if (o instanceof Map<?, ?> m && m.get("name") instanceof String n
                    && (n.equals(model) || n.startsWith(model + ":"))) {
                return true;
            }
        }
        return false;
    }
}
