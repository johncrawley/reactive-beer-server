package guru.springframework.sfgrestbrewery.web.functional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BeerRouterConfig {

	public final static String BEER_V2_URL = "/api/v2/beer";
	public final static String BEER_V2_URL_ID_TEMPLATE = "/api/v2/beer/{beerId}";
	public final static String BEER_V2_URL_UPC = "/api/v2/beerUpc/";
	public final static String BEER_V2_URL_UPC_TEMPLATE = BEER_V2_URL_UPC + "{beerUpc}";
	

	@Bean
	public RouterFunction<ServerResponse> beerRoutesV2(BeerHandlerV2 handler){
		
		RequestPredicate acceptJson = accept(APPLICATION_JSON);
		
		return route()
				.GET(BEER_V2_URL_ID_TEMPLATE, acceptJson, handler::getBeerById)
				.GET(BEER_V2_URL_UPC_TEMPLATE, acceptJson, handler::getBeerByUpc)
				.POST(BEER_V2_URL, acceptJson, handler::saveNewBeer)
				.PUT(BEER_V2_URL_ID_TEMPLATE, acceptJson, handler::updateBeer)
				.DELETE(BEER_V2_URL_ID_TEMPLATE, acceptJson, handler::deleteBeer)
						
				.build();
	}


}
