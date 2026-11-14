package astratwin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runs every test; exits with 1 if one fails. Argument: path to data/cache. */
public final class TestRunner {
    interface Test {
        void run() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        Path cache = Path.of(args.length > 0 ? args[0] : "../../data/cache");
        Map<String, Test> tests = new LinkedHashMap<>();
        JsonTest.register(tests);
        RulesTest.register(tests);
        PrivacyTest.register(tests, cache);
        ScenarioTest.register(tests, cache);
        StreamTest.register(tests, cache);
        SupportTest.register(tests, cache);

        List<String> failed = new ArrayList<>();
        for (Map.Entry<String, Test> t : tests.entrySet()) {
            try {
                t.getValue().run();
                System.out.println("PASS  " + t.getKey());
            } catch (Throwable e) {
                failed.add(t.getKey());
                System.out.println("FAIL  " + t.getKey() + "\n      " + e);
            }
        }
        System.out.println((tests.size() - failed.size()) + "/" + tests.size() + " passed");
        if (!failed.isEmpty()) {
            System.exit(1);
        }
    }
}
