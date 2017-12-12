package restql.core.hooks;

import restql.core.response.QueryResponse;

/**
 * Created by iago.osilva on 30/01/17.
 */
public abstract class AfterRequestHook extends RequestHook {

    public QueryResponse getResponseBody() {
        Object response = this.getData().get("body");

        return new QueryResponse((String) response.toString());
    }

    public Long getReponseTime() {
        Object responseTime = this.getData().get("response-time");
        return (responseTime != null ? ((Number) responseTime).longValue() : null);
    }

    public Long getTime() {
        Object time = this.getData().get("time");
        return (time != null ? ((Number) time).longValue() : null);
    }

    public Long getResponseStatusCode() {
        return ((Number) this.getData().get("status")).longValue();
    }

    public Boolean isError() {
        return this.getData().containsKey("errordetail");
    }

}
