package astratwin.privacy;

import astratwin.domain.Viewer;

/** Who asks: a viewer type and, for SELF, which crew member. */
public record ViewRequest(Viewer viewer, String viewerCrewId) {

    public boolean isSelf(String crewId) {
        return viewer == Viewer.SELF && crewId.equals(viewerCrewId);
    }
}
