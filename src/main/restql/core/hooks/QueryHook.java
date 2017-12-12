package restql.core.hooks;

import restql.core.interop.Hook;

/**
 * Created by iago.osilva on 31/01/17.
 */
public abstract class QueryHook extends Hook{

    public String getQuery() {
        return (String) this.getData().get("query");
    }

}
