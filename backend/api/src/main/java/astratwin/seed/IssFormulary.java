package astratwin.seed;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * ISS medical kit products the app may name in a recommendation (team decision #11). Every named drug
 * must be on this list, and every recommendation still waits for the CMO. Source: Diaz et al. 2024,
 * a list taken from a NASA ISS medical manual obtained via FOIA; open licence CC BY 4.0, credited
 * under "Space Agency Partner Data and Other References".
 */
public final class IssFormulary {
    public static final String SOURCE = "Diaz, Ives, Lazare, Buckland 2024, npj Microgravity 10:76 "
            + "(doi:10.1038/s41526-024-00414-3), CC BY 4.0: ISS medical kit list from a NASA manual (FOIA)";

    /** Product and strength as listed in the paper (examples verified in #11). */
    public record Product(String name, String strength) {
    }

    public static final List<Product> PRODUCTS = List.of(
            new Product("acetaminophen", "325 mg"), new Product("ibuprofen", "400 mg"),
            new Product("ondansetron", "8 mg"), new Product("promethazine", "25 mg"),
            new Product("melatonin", "3 mg"), new Product("zolpidem", "5 mg"),
            new Product("oxymetazoline", "0.05%"), new Product("epinephrine", "0.3 mg"),
            new Product("metoprolol", "50 mg"));

    private IssFormulary() {
    }

    public static Optional<Product> find(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return PRODUCTS.stream().filter(p -> p.name().equals(n)).findFirst();
    }
}
