package restql.core.interop;

import java.util.Map;

/**
 * Created by iago.osilva on 30/01/17.
 */
public abstract class Hook {
    private Map<String, Object> data;

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public abstract void execute();
}
