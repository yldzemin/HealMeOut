package astratwin.domain;

/** Where a real baseline comes from; shown next to every commercial-mission value (#30). */
public record MissionTag(
        String name, String kind, String start, String end, String orbit,
        String crew, String hostedBy, String studyId, String link) {
}
