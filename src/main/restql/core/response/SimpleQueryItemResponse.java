package restql.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import restql.core.exception.RestQLException;

/**
 * Created by ideais on 15/12/16.
 */
public class SimpleQueryItemResponse implements QueryItemResponse{

    private final ObjectMapper mapper;
    private final JsonNode data;

    public SimpleQueryItemResponse(JsonNode data) {
        this.data = data;
        this.mapper = new ObjectMapper();
    }

    @Override
    public <T> T getResult(Class<T> clazz) {
        try {
            return mapper.treeToValue(data.get("result"), clazz);
        } catch (JsonProcessingException e) {
            throw new RestQLException(e);
        }
    }

    @Override
    public ResponseDetails getDetails() {
        return ResponseDetails.fromJsonNode(data.get("details"));
    }

}
