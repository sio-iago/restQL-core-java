package restql.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import restql.core.exception.RestQLException;

/**
 * Created by ideais on 20/12/16.
 */
public class ArrayQueryItemResponse implements QueryItemResponse {

    private final JsonNode details;
    private final JsonNode result;
    private final ObjectMapper mapper;

    public ArrayQueryItemResponse(JsonNode details, JsonNode result) {
        this.details = details;
        this.result = result;
        this.mapper = new ObjectMapper();
    }


    @Override
    public <T> T getResult(Class<T> clazz) {
        try {
            return mapper.treeToValue(result, clazz);
        } catch (JsonProcessingException e) {
            throw new RestQLException(e);
        }
    }

    @Override
    public ResponseDetails getDetails() {
        return ResponseDetails.fromJsonNode(details);
    }
}
