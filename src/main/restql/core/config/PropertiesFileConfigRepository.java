package restql.core.config;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesFileConfigRepository implements ConfigRepository {

	/**
	 * API Mappings
	 */
	private RouteMap mappings;
	
	/**
	 * Creates the class and gets mappings from Properties File
	 * 
	 * @param filename {@link String}
	 * 
	 * @throws IOException :(
	 */
	public PropertiesFileConfigRepository(String filename) throws IOException {
		Properties fileProperties = new Properties();
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		fileProperties.load(classLoader.getResourceAsStream(filename));
		
		this.mapProperties(fileProperties);
	}

	/**
	 * Creates the class from a {@link Properties} class.
	 *
	 * @param properties {@link Properties}
	 */
	public PropertiesFileConfigRepository(Properties properties) {
        this.mapProperties(properties);
	}

	/**
	 * Maps the properties to the mappings.
	 *
	 * @param props {@link Properties}
	 */
	private void mapProperties(Properties props) {
        mappings = new RouteMap();

        for(Entry<Object, Object> property : props.entrySet()) {
            mappings.put((String) property.getKey(), (String) property.getValue());
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RouteMap getMappings() {
		// TODO Auto-generated method stub
		return this.mappings;
	}

}
