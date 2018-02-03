package restql.core.annotation;

public class RestEntityMappingException extends RuntimeException {

    public RestEntityMappingException(String entityName) {
        super(String.format("Could not parse entity [%s]", entityName));
    }

    public RestEntityMappingException(String entityName, String cause) {
        super(String.format("Could not parse entity [%s]. Cause: %s", entityName, cause));
    }
}
