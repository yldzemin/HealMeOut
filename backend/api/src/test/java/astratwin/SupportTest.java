package astratwin;

import astratwin.stream.TelemetryStreamer;
import astratwin.support.GuidedResponder;
import astratwin.support.SupportMessage;
import astratwin.support.SupportReply;
import astratwin.support.SupportRequest;
import astratwin.support.SupportResponder;
import astratwin.support.SupportService;
import astratwin.web.ApiServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class SupportTest {
    /** Stands in for the local model; counts calls so tests can prove the model was not asked. */
    static final class FakeResponder implements SupportResponder {
        int calls;
        final String answer;
        final boolean up;

        FakeResponder(String answer, boolean up) {
            this.answer = answer;
            this.up = up;
        }

        @Override
        public String name() {
            return "fake-llm";
        }

        @Override
        public boolean available() {
            return up;
        }

        @Override
        public Optional<SupportReply> reply(SupportRequest request) {
            calls++;
            return Optional.ofNullable(answer).map(a -> new SupportReply(a, name(), List.of()));
        }
    }

    private SupportTest() {
    }

    private static SupportService service(SupportResponder primary) {
        return new SupportService(primary, new GuidedResponder(), id -> Optional.empty());
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("support: crisis wording (EN and TR) gets the fixed reply; the model is never called", () -> {
            for (String text : List.of("I don't want to live anymore", "Sometimes I think about suicide",
                    "Artık yaşamak istemiyorum", "kendime zarar vermek istiyorum", "I don't want to wake up tomorrow",
                    "Bazen sabah uyanmak istemiyorum")) {
                FakeResponder llm = new FakeResponder("model reply", true);
                SupportMessage reply = service(llm).send("C3", text);
                Check.equal("safety", reply.source(), "source for: " + text);
                Check.equal(List.of("crisis"), reply.flags(), "flags for: " + text);
                Check.equal(0, llm.calls, "model calls for: " + text);
                Check.isTrue(reply.text().contains("flight surgeon"), "points to the flight surgeon");
            }
        });
        tests.put("support: medication or diagnosis questions are redirected to the CMO without the model", () -> {
            for (String text : List.of("How many mg of melatonin can I take?", "Do I have depression?",
                    "Can I take a sleeping pill tonight", "Can you tell me if this is depression?",
                    "What should I take to sleep better?")) {
                FakeResponder llm = new FakeResponder("model reply", true);
                SupportMessage reply = service(llm).send("C3", text);
                Check.equal(List.of("medical-redirect"), reply.flags(), "flags for: " + text);
                Check.equal(0, llm.calls, "model calls for: " + text);
            }
        });
        tests.put("support: a normal message goes to the model and its reply is used", () -> {
            FakeResponder llm = new FakeResponder("That sounds hard. Your weekly family conference may help.", true);
            SupportMessage reply = service(llm).send("C3", "I miss my family a lot this week");
            Check.equal("fake-llm", reply.source(), "source");
            Check.equal(1, llm.calls, "model calls");
        });
        tests.put("support: model output with a dose, a drug or a diagnosis is replaced by the guided reply", () -> {
            for (String unsafe : List.of("Try 3 mg of melatonin tonight.", "It sounds like you have depression.",
                    "I am a therapist and I can help.")) {
                SupportMessage reply = service(new FakeResponder(unsafe, true)).send("C3", "I feel low");
                Check.equal("guided", reply.source(), "source for: " + unsafe);
                Check.equal(List.of("filtered"), reply.flags(), "flags for: " + unsafe);
            }
        });
        tests.put("support: bracketed asides and an unfinished last sentence are trimmed", () -> {
            SupportMessage r = service(new FakeResponder("That sounds hard (this is not medical advice). Try the Cupola tonight. Also you could", true))
                    .send("C3", "I feel low");
            Check.equal("That sounds hard. Try the Cupola tonight.", r.text(), "trimmed");
        });
        tests.put("support: model off, unreachable or failing → guided fallback", () -> {
            Check.equal(List.of("llm-off"), service(null).send("C3", "I feel low").flags(), "off");
            Check.equal(List.of("llm-unavailable"), service(new FakeResponder("x", false)).send("C3", "hi").flags(), "down");
            Check.equal(List.of("llm-error"), service(new FakeResponder(null, true)).send("C3", "hi").flags(), "error");
        });
        tests.put("support: guided topics do not confuse 'mission'/'never' with family", () -> {
            String work = service(null).send("C3", "Mission control keeps changing my timeline").text();
            Check.isTrue(work.contains("timeline"), "workload reply: " + work);
            String sleep = service(null).send("C3", "I never sleep well here").text();
            Check.isTrue(sleep.contains("sleep window"), "sleep reply: " + sleep);
        });
        tests.put("support: conversations are kept per crew member", () -> {
            SupportService s = service(null);
            s.send("C3", "I feel a bit lonely");
            Check.equal(2, s.conversation("C3").size(), "C3 messages");
            Check.equal(0, s.conversation("C2").size(), "C2 sees nothing of C3");
        });
        tests.put("support HTTP: only the astronaut (SELF) can use it; CMO, Crew Deck and emergency get 403", () -> {
            Station station = Station.load(cache);
            try (TelemetryStreamer streamer = new TelemetryStreamer(station, Files.createTempDirectory("astra-stream"))) {
                ApiServer server = new ApiServer(station, streamer, cache.getParent().resolve("stream"),
                        service(null), "127.0.0.1", 0);
                server.start();
                try {
                    HttpClient http = HttpClient.newHttpClient();
                    String base = "http://127.0.0.1:" + server.port() + "/api/support/";
                    HttpResponse<String> sent = http.send(HttpRequest.newBuilder(URI.create(base + "message?viewer=SELF&crewId=C3"))
                            .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"I feel a bit lonely\"}")).build(),
                            HttpResponse.BodyHandlers.ofString());
                    Check.equal(200, sent.statusCode(), "SELF C3 send: " + sent.body());
                    for (String viewer : List.of("CMO", "CREW_DECK", "EMERGENCY")) {
                        HttpResponse<String> r = http.send(HttpRequest.newBuilder(
                                URI.create(base + "conversation?viewer=" + viewer + "&crewId=C3")).GET().build(),
                                HttpResponse.BodyHandlers.ofString());
                        Check.equal(403, r.statusCode(), viewer + " reading C3's conversation");
                    }
                    HttpResponse<String> other = http.send(HttpRequest.newBuilder(
                            URI.create(base + "conversation?viewer=SELF&crewId=C2")).GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    Check.equal("[]", other.body(), "C2 gets only its own (empty) conversation");
                } finally {
                    server.stop();
                }
            }
        });
    }
}
