package astratwin.scenario;

import java.util.List;

/**
 * MVP: cabin CO2 build-up. EDA was unreachable, so this is a synthetic ramp from 3 to 6.8 mmHg
 * through the limits in BACKEND §4 (BACKEND §5).
 */
public final class Co2Scenario implements Scenario {
    private final String symptomaticCrewId;

    public Co2Scenario(String symptomaticCrewId) {
        this.symptomaticCrewId = symptomaticCrewId;
    }

    @Override
    public String id() {
        return "co2";
    }

    @Override
    public String title() {
        return "Cabin CO2 build-up";
    }

    @Override
    public String priority() {
        return "MVP";
    }

    @Override
    public String source() {
        return "OCHMO-TB-004; ICES-2025-402; values are a synthetic ramp";
    }

    @Override
    public List<ScenarioStep> steps() {
        return List.of(
                ScenarioStep.at("08:00", "Nominal cabin")
                        .detail("Typical ISS cabin CO2 in OSDR EDA data is about 2,500–3,000 ppm, which is about "
                                + "1.9–2.3 mmHg at 14.7 psia. The values in this scenario are a synthetic ramp.")
                        .co2(2.1)
                        .build(),
                ScenarioStep.at("10:00", "CO2 rising").detail("1-hour average above the 3 mmHg limit.").co2(3.3).build(),
                ScenarioStep.at("11:00", "Headache reported")
                        .detail("A crew member reports a headache; headache is reported at 2.8–4.5 mmHg.")
                        .reading(symptomaticCrewId, "symptom_headache", 1, "flag", "periodic", "Self-reported symptom")
                        .co2(3.8)
                        .build(),
                ScenarioStep.at("12:00", "CO2 at 4.6 mmHg").detail("Above 4.5 mmHg.").co2(4.6).build(),
                ScenarioStep.at("13:00", "CO2 at contingency level")
                        .detail("At or above 6.8 mmHg, the NASA contingency proposal level (not yet a standard).")
                        .co2(6.9)
                        .build(),
                ScenarioStep.at("14:00", "CO2 removal restored")
                        .detail("CO2 is back below the limit and the headache is gone; the alert becomes inactive.")
                        .reading(symptomaticCrewId, "symptom_headache", 0, "flag", "periodic", "Self-reported symptom")
                        .co2(2.6)
                        .build());
    }
}
