package guru.springframework.sfgrestbrewery;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
//import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
//import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootApplication
public class SfgReactiveBreweryApplication {
	
	
	public static void main(String[] args) {
		SpringApplication.run(SfgReactiveBreweryApplication.class, args);
	}
	@Bean
	ConnectionFactoryInitializer initializer (ConnectionFactory connectionFactory) {
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(
				new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
		return initializer;
	}
}
