package restql.core.config;

public interface ConfigRepository {
	
	/**
	 * Retrieves the APIs mapped on a given configuration strategy
	 * 
	 * @return {@link RouteMap}
	 */
	public RouteMap getMappings();
	
}
