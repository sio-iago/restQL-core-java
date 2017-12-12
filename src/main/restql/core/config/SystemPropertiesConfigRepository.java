package restql.core.config;

import java.util.Map.Entry;
import java.util.Properties;

public class SystemPropertiesConfigRepository implements ConfigRepository{
	
	/**
	 * API Mappings
	 */
	private RouteMap mappings;
	
	/**
	 * Creates the class and gets mappings from System Properties
	 */
	public SystemPropertiesConfigRepository() {
		Properties systemProperties = System.getProperties();
		
		mappings = new RouteMap();
		
		for(Entry<Object, Object> property : systemProperties.entrySet()) {
			mappings.put((String) property.getKey(), (String) property.getValue());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RouteMap getMappings() {
		return this.mappings;
	}
	
}
