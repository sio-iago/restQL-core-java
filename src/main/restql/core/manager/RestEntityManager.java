package restql.core.manager;

import restql.core.RestQL;
import restql.core.annotation.RestEntity;
import restql.core.annotation.RestEntityMapper;
import restql.core.annotation.RestEntityMeta;
import restql.core.config.ClassConfigRepository;
import restql.core.config.ConfigRepository;
import restql.core.query.QueryOptions;
import restql.core.response.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestEntityManager {

    private static final Logger logger = Logger.getLogger(RestEntityManager.class.getName());

    private RestQL restQL;
    private ConfigRepository configRepository;

    public RestEntityManager() {
        this.configRepository = new ClassConfigRepository();
        this.restQL = new RestQL(null, null);
    }

    public RestEntityManager(ConfigRepository configRepository) {
        this(configRepository, null);
    }

    public RestEntityManager(QueryOptions queryOptions) {
        this(null, queryOptions);
    }

    public RestEntityManager(ConfigRepository configRepository, QueryOptions queryOptions) {
        this.configRepository = configRepository;
        this.restQL = new RestQL(configRepository, queryOptions);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass) {
        return this.executeQuery(restEntityClass, null, null);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.executeQuery(restEntityClass, null, queryOptions);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        // Let's retrieve all rest entity meta information first
        Map<String, RestEntityMeta> restEntityMetaMap = RestEntityMapper.traverseRestEntity(restEntityClass, queryParams);

        // Now we populate our mappings config class using the default configRepository and the class mappings
        ClassConfigRepository queryConfigRespository = new ClassConfigRepository();

        if (this.configRepository != null) {
            logger.warning("Overriding default configured mappings. Consider using only entity mappings or remove them.");

            for (Map.Entry<String, String> configMappings : this.configRepository.getMappings().toMap().entrySet()) {
                queryConfigRespository.put(configMappings.getKey(), configMappings.getValue());
            }
        }

        for (RestEntityMeta restEntityMeta : restEntityMetaMap.values()) {
            if (restEntityMeta.getResourceMapping() != null) {
                queryConfigRespository.put(restEntityMeta.getEntityName(), restEntityMeta.getResourceMapping());
            }
        }

        String query = RestEntityMapper.getEntityQuery(restEntityMetaMap);
        QueryResponse queryResponse = restQL.executeQuery(query, queryConfigRespository.getMappings(), queryOptions);

        return queryResponse;
    }

    public <T> List<T> fetch(Class<T> restEntityClass) {
        return this.fetch(restEntityClass, null, null);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, Map<String, Object> queryParams) {
        return this.fetch(restEntityClass, queryParams, null);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.fetch(restEntityClass, null, queryOptions);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        QueryResponse queryResponse = this.executeQuery(restEntityClass, queryParams, queryOptions);
        RestEntityMeta restEntityMeta = RestEntityMapper.extractMetaFromEntity(restEntityClass, queryParams);

        return queryResponse.getList(restEntityMeta.getResponseLookupPath(), restEntityClass);
    }

    public <T> T fetchOne(Class<T> restEntityClass) {
        return this.fetchOne(restEntityClass);
    }

    public <T> T fetchOne(Class<T> restEntityClass, Map<String, Object> queryParams) {
        return this.fetchOne(restEntityClass, queryParams, null);
    }

    public <T> T fetchOne(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.fetchOne(restEntityClass, null, queryOptions);
    }

    public <T> T fetchOne(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        QueryResponse queryResponse = this.executeQuery(restEntityClass, queryParams, queryOptions);
        RestEntityMeta restEntityMeta = RestEntityMapper.extractMetaFromEntity(restEntityClass, queryParams);

        return queryResponse.get(restEntityMeta.getResponseLookupPath(), restEntityClass);
    }
}
