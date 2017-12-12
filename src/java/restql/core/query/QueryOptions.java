package restql.core.query;

import restql.core.hooks.AfterQueryHook;
import restql.core.hooks.AfterRequestHook;
import restql.core.hooks.QueryHook;
import restql.core.hooks.RequestHook;
import restql.core.interop.Hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ideais on 12/12/16.
 */
public class QueryOptions {

    private boolean debugging = false;

    private Integer globalTimeout = 5000;

    private Integer timeout = 1000;

    private List<Class> beforeRequestHooks;
    private List<Class> afterRequesthooks;
    private List<Class> beforeQueryHooks;
    private List<Class> afterQueryHooks;

    public QueryOptions() {
        this.beforeRequestHooks = new ArrayList<>();
        this.afterRequesthooks = new ArrayList<>();
        this.beforeQueryHooks = new ArrayList<>();
        this.afterQueryHooks = new ArrayList<>();
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public boolean isDebugging() {
        return this.debugging;
    }

    public Integer getGlobalTimeout() {
        return globalTimeout;
    }

    public void setGlobalTimeout(Integer globalTimeout) {
        this.globalTimeout = globalTimeout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /* Binding hooks */
    public <T> void setBeforeRequestHook(Class<T> hook) {
        this.beforeRequestHooks.add(hook);
    }

    public <T> void setAfterRequestHook(Class<T> hook) {
        this.afterRequesthooks.add(hook);
    }

    public <T> void setBeforeQueryHook(Class<T> hook) {
        this.beforeQueryHooks.add(hook);
    }

    public <T> void setAfterQuerytHook(Class<T> hook) {
        this.afterQueryHooks.add(hook);
    }


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("debugging", debugging);
        map.put("timeout", timeout);
        map.put("global-timeout", globalTimeout);

        Map<String, List> hooks = new HashMap<>();
        hooks.put("before-request", this.beforeRequestHooks);
        hooks.put("after-request", this.afterRequesthooks);
        hooks.put("before-query", this.beforeQueryHooks);
        hooks.put("after-query", this.afterQueryHooks);

        map.put("java-hooks", hooks);
        return map;
    }

}
