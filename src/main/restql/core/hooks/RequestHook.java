package restql.core.hooks;

import restql.core.interop.Hook;

import java.util.Collections;
import java.util.Map;

/**
 * Created by iago.osilva on 31/01/17.
 */
public abstract class RequestHook extends Hook {
    
    public String getUrl() {
        return (String) this.getData().get("url");
    }

    public Long getTimeout() {
        return ((Number) this.getData().get("timeout")).longValue();
    }

    public Map<String, String> getQueryParameters() {
        Object queryParams = this.getData().get("query-params");
        return (queryParams != null ? (Map<String, String> )queryParams : null);
    }

    public Map<String, String> getHeaders() {
        Object headers = this.getData().get("headers");
        return (headers != null ? Collections.synchronizedMap((Map<String, String>) headers) : null);
    }
}
