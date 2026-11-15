package astratwin.twin;

import astratwin.domain.CheckIn;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Forecast;
import astratwin.domain.Forecast.ForecastPoint;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Sleep debt (BACKEND §3.2): the scheduled 8.5 h per night minus the sleep logged in the
 * astronaut's own check-ins, summed over the last 7 check-ins. The projection counts the part of
 * the standard ISS sleep period (21:30–06:00 GMT, Jones 2022) that falls inside the horizon and
 * assumes it is slept at the astronaut's recent average. Built from check-ins, so it is
 * psychological data: only the astronaut sees it (BACKEND §6).
 */
public final class SleepDebtModel implements ForecastModel {
    static final double SCHEDULED_HOURS = 8.5;
    static final LocalTime SLEEP_START_GMT = LocalTime.of(21, 30);
    static final int WINDOW = 7;
    static final String SOURCE = "Your check-ins; Jones 2022, Sleep 45(3) zsac006 (ISS sleep period 21:30–06:00 GMT, "
            + "8.5 h); Barger 2014, Lancet Neurol 13:904 (ISS mean 6.09 h)";

    @Override
    public String metric() {
        return "sleep_debt";
    }

    @Override
    public DataKind kind() {
        return DataKind.PSYCH;
    }

    @Override
    public Optional<Forecast> forecast(TwinContext context, CrewMember member, int horizonHours) {
        List<CheckIn> mine = context.checkIns().stream().filter(c -> c.crewId().equals(member.id())).toList();
        if (mine.isEmpty()) {
            return Optional.empty();
        }
        List<CheckIn> recent = mine.subList(Math.max(0, mine.size() - WINDOW), mine.size());
        double debt = recent.stream().mapToDouble(c -> Math.max(0, SCHEDULED_HOURS - c.sleepHours())).sum();
        double average = recent.stream().mapToDouble(CheckIn::sleepHours).average().orElse(SCHEDULED_HOURS);
        double shortfall = Math.max(0, 1 - average / SCHEDULED_HOURS);
        Instant now = context.now();
        double scheduled = scheduledSleepHours(now, now.plus(Duration.ofHours(horizonHours)));
        double later = debt + scheduled * shortfall;
        List<ForecastPoint> points = List.of(new ForecastPoint("T+0", round(debt)),
                new ForecastPoint("T+" + horizonHours, round(later)));
        String evidence = "Scheduled 8.5 h minus the sleep you logged, summed over your last " + recent.size()
                + (recent.size() == 1 ? " check-in" : " check-ins") + ". Projection: " + fmt(scheduled)
                + " h of the standard ISS sleep period (21:30–06:00 GMT) fall in the next " + horizonHours
                + " h, slept at your recent average of " + fmt(average) + " h per night. ISS crews averaged 6.09 h "
                + "(Barger 2014). Private: only you see this.";
        return Optional.of(new Forecast(member.id(), horizonHours, metric(), "h", points, "modelled", evidence, SOURCE));
    }

    /** Hours of the scheduled sleep period (21:30–06:00 GMT every night) inside [from, to). */
    public static double scheduledSleepHours(Instant from, Instant to) {
        long seconds = 0;
        LocalDate last = to.atZone(ZoneOffset.UTC).toLocalDate();
        for (LocalDate day = from.atZone(ZoneOffset.UTC).toLocalDate().minusDays(1); !day.isAfter(last);
                day = day.plusDays(1)) {
            Instant start = day.atTime(SLEEP_START_GMT).toInstant(ZoneOffset.UTC);
            Instant end = start.plus(Duration.ofMinutes(Math.round(SCHEDULED_HOURS * 60)));
            Instant a = start.isAfter(from) ? start : from;
            Instant b = end.isBefore(to) ? end : to;
            if (b.isAfter(a)) {
                seconds += Duration.between(a, b).toSeconds();
            }
        }
        return seconds / 3600.0;
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
