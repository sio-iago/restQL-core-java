package restql.core.config;

import java.util.Map;
import java.util.Map.Entry;


public class ClassConfigRepository implements ConfigRepository {

	/**
	 * API Mappings
	 */
	private RouteMap mappings;
	
	/**
	 * Creates a new configuration class.
	 */
	public ClassConfigRepository(){
		mappings = new RouteMap();
	}
	
	/**
	 * Creates a new configuration class with a given configuration map.
	 * 
	 * @param configurationMap {@link Map} of (String, String)
	 */
	public ClassConfigRepository(Map<String, String> configurationMap) {
		this();
		
		for(Entry<String, String> confParam : configurationMap.entrySet()) {
			this.mappings.put(confParam.getKey(), confParam.getValue());
		}
	}
	
	/**
	 * Puts a new (name,url) pair on the configuration map. 
	 * 
	 * @param name The resource name
	 * @param url The resource url
	 */
	public void put(String name, String url) {
		this.mappings.put(name, url);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RouteMap getMappings() {
		return this.mappings;
	}

}
