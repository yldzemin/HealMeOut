package astratwin.privacy;

import astratwin.domain.Alert;
import astratwin.domain.Environment;
import astratwin.domain.Level;
import astratwin.domain.Viewer;
import astratwin.scenario.ScenarioState;
import java.util.List;

/** Everything one viewer sees at once; the demo console and the frontend render this. */
public record StationView(
        Viewer viewer, String viewerCrewId, Level stationLevel, List<CrewCard> crew, List<Alert> alerts,
        Environment cabin, Environment dose, ScenarioState scenario, String disclaimer) {
}
