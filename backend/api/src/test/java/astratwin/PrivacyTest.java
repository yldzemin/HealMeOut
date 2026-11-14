package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.DataKind;
import astratwin.domain.Viewer;
import astratwin.privacy.CrewCard;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import java.nio.file.Path;
import java.util.Map;

/** BACKEND §8: "psych data never reaches the Crew Deck". */
final class PrivacyTest {
    private PrivacyTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("privacy: psychological check-ins never reach the Crew Deck, emergency or CMO view", () -> {
            Station station = Station.load(cache);
            for (Viewer viewer : new Viewer[] {Viewer.CREW_DECK, Viewer.EMERGENCY, Viewer.CMO}) {
                StationView view = station.view(new ViewRequest(viewer, null));
                for (CrewCard card : view.crew()) {
                    Check.isTrue(card.checkIns() == null, viewer + " sees check-ins of " + card.id());
                }
                for (Alert a : view.alerts()) {
                    Check.isTrue(a.category() != DataKind.PSYCH, viewer + " sees a psych alert");
                }
            }
        });
        tests.put("privacy: SELF sees only its own check-ins", () -> {
            StationView view = Station.load(cache).view(new ViewRequest(Viewer.SELF, "C3"));
            for (CrewCard card : view.crew()) {
                Check.equal(card.id().equals("C3"), card.checkIns() != null, "check-ins of " + card.id());
            }
        });
        tests.put("privacy: Crew Deck sees no vitals; medication only if shared (C3)", () -> {
            StationView view = Station.load(cache).view(new ViewRequest(Viewer.CREW_DECK, null));
            for (CrewCard card : view.crew()) {
                Check.isTrue(card.vitals() == null, "Crew Deck sees vitals of " + card.id());
                Check.equal(card.id().equals("C3"), card.medical() != null, "medication of " + card.id());
            }
        });
        tests.put("privacy: emergency broadcast reaches the Crew Deck, vitals alerts do not", () -> {
            Station station = Station.load(cache);
            station.startScenario("chest-pain");
            station.addReading("C2", "glucose", 50, "mg/dL");
            StationView deck = station.view(new ViewRequest(Viewer.CREW_DECK, null));
            Check.isTrue(deck.alerts().stream().anyMatch(a -> a.category() == DataKind.EMERGENCY), "emergency missing");
            Check.isTrue(deck.alerts().stream().noneMatch(a -> a.category() == DataKind.VITALS), "vitals alert leaked");
            StationView cmo = station.view(new ViewRequest(Viewer.CMO, null));
            Check.isTrue(cmo.alerts().stream().anyMatch(a -> a.id().equals("glucose:C2")), "CMO must see glucose alert");
        });
    }
}
