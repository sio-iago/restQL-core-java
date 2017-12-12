package restql.core.interop;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ideais on 29/12/16.
 */
public abstract class Encoder {

    private Map<String, Object> data = new HashMap<>();

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String[] path) {
        Object result = data;
        for (String aPath : path) {
            if (result != null) {
                result = ((Map<String, Object>) result).get(aPath);
            }
        }
        return (T) result;
    }

    public abstract String encode();

}
