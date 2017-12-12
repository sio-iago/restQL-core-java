package restql.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ideais on 12/12/16.
 */
public class RouteMap {

    private Map<String, String> mappings = new HashMap<>();

    public void put(String name, String url) {
        mappings.put(name, url);
    }

    public Map<String, String> toMap() {
        return mappings;
    }

}
