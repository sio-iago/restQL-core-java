package restql.core.hooks;

import restql.core.response.QueryResponse;

import java.util.Map;

/**
 * Created by iago.osilva on 31/01/17.
 */
public abstract class AfterQueryHook extends QueryHook {

    public QueryResponse getResult() {
        return new QueryResponse((Map) this.getData().get("result"));
    }
}
