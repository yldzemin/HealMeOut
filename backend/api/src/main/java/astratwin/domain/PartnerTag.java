package astratwin.domain;

/**
 * Where partner-agency data comes from and why we use it (team decision #13): shown next to every
 * value from a partner instrument that a NASA archive hosts.
 */
public record PartnerTag(String instrument, String agency, String location, String hostedBy, String link, String why) {
}
