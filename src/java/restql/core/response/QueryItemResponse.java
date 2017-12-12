package restql.core.response;

/**
 * Created by ideais on 20/12/16.
 */
public interface QueryItemResponse {

    public <T> T getResult(Class<T> clazz);

    public ResponseDetails getDetails();

}
