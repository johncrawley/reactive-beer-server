package guru.springframework.sfgrestbrewery.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {
	
	public static final String BASE_URL = "http://localhost:8080";
	public static final String BEER_V2_PATH = "api/v2/beer";
	WebClient webClient;
	private CountDownLatch countDownLatch;
	
	@BeforeEach
	void setup() {
		webClient = WebClient.builder()
				.baseUrl(BASE_URL)
				.clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
				.build();
	}
	
	@Test
	void getBeerById() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		Mono<BeerDto> beerDtoMono = webClient.get().uri(BEER_V2_PATH + "/" + 1)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(BeerDto.class);
		
		
		beerDtoMono.subscribe(beer -> {
			assertThat(beer).isNotNull();
			assertThat(beer.getBeerName()).isNotNull();
			countDownLatch.countDown();
		});
		assertCountDown();
	}
	
	
	@Test
	void getBeerByIdNotFound() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		int badId = -1000;
		Mono<BeerDto> beerDtoMono = webClient.get().uri(BEER_V2_PATH + "/" + badId )
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(BeerDto.class);
		
		beerDtoMono.subscribe(beer -> {
		}, throwable -> {
			countDownLatch.countDown();
		});
		assertCountDown();
	}
	
	
	@Test
	void getBeerByUpc() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		String validUpc = BeerLoader.BEER_10_UPC;
		Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_V2_URL_UPC + validUpc )
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(BeerDto.class);
		
		beerDtoMono.subscribe(beer -> {
			assertThat(beer).isNotNull();
			assertThat(beer.getBeerName()).isNotNull();
			countDownLatch.countDown();
		});
		assertCountDown();
	}
	
	
	@Test
	void getBeerByUpcNotFound() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		String badUpc = "-123123123";
		Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_V2_URL_UPC + badUpc )
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(BeerDto.class);
		beerDtoMono.subscribe(beer -> {
		}, throwable -> {
			if(throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")){	
				WebClientResponseException ex = (WebClientResponseException) throwable;
				if(ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				countDownLatch.countDown();
			}
		}
		});
		assertCountDown();
	}
	
	
	@Test
	void saveBeer() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
    	BeerDto beerDto = BeerDto.builder()
    			.beerName("Test Beer")
    			.upc("1234555")
    			.beerStyle("PALE_ALE")
    			.price(new BigDecimal("8.99"))
    			.build();	
		
		String badUpc = "-123123123";
		Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri(BeerRouterConfig.BEER_V2_URL )
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(beerDto))
				.retrieve()
				.toBodilessEntity();
		
		beerResponseMono.subscribe(response -> {
			assertThat(response.getStatusCode().is2xxSuccessful());
			countDownLatch.countDown();
		});
		assertCountDown();
	}
	
	
	@Test
	void saveBeerBadRequest() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
    	BeerDto beerDto = BeerDto.builder()
    			.beerName("Test Beer")
    			.upc("1234555")
    			.build();	
		
		String badUpc = "-123123123";
		Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri(BeerRouterConfig.BEER_V2_URL )
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(beerDto))
				.retrieve()
				.toBodilessEntity();
		
		beerResponseMono.subscribe(response -> {}, throwable -> {
			if(throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest")){
				
				countDownLatch.countDown();
			}
		});
		assertCountDown();
	}
	
	
	@Test
	void updateBeer() throws InterruptedException {
		countDownLatch = new CountDownLatch(2);
		
		final Integer beerId = 1;
		final String UPDATED_BEER_NAME = "UpdatedBeer";
    	BeerDto updatedBeerDto = BeerDto.builder()
    			.beerName(UPDATED_BEER_NAME)
    			.upc("1234555")
    			.beerStyle("PALE_ALE")
    			.price(new BigDecimal("12.99"))
    			.build();	
		
		Mono<ResponseEntity<Void>> beerResponseMono = webClient.put().uri(BeerRouterConfig.BEER_V2_URL + "/" + beerId)
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(updatedBeerDto))
				.retrieve()
				.toBodilessEntity();
		
		beerResponseMono.subscribe(responseEntity -> {
			assertThat(responseEntity.getStatusCode().is2xxSuccessful());
			countDownLatch.countDown();
		});
		
		countDownLatch.await(500, TimeUnit.MILLISECONDS); //wait for the update request to process
				
		webClient.get().uri(BeerRouterConfig.BEER_V2_URL + "/" + beerId)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.bodyToMono(BeerDto.class)
			.subscribe( beerDto -> {
				assertThat(beerDto).isNotNull();
				assertThat(beerDto.getBeerName()).isNotNull();
				assertThat(beerDto.getBeerName()).isEqualTo(UPDATED_BEER_NAME);
				countDownLatch.countDown();
			});
		assertCountDown();
	}
	
	
	@Test
	void updateBeerNotFound() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		
		final Integer beerId = -111;
		final String UPDATED_BEER_NAME = "UpdatedBeer";
    	BeerDto updatedBeerDto = BeerDto.builder()
    			.beerName(UPDATED_BEER_NAME)
    			.upc("1234555")
    			.beerStyle("PALE_ALE")
    			.price(new BigDecimal("12.99"))
    			.build();	
		
		Mono<ResponseEntity<Void>> beerResponseMono = webClient.put().uri(BeerRouterConfig.BEER_V2_URL + "/" + beerId)
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(updatedBeerDto))
				.retrieve()
				.toBodilessEntity();
		
		beerResponseMono.subscribe(responseEntity -> {
			assertThat(responseEntity.getStatusCode().is2xxSuccessful());
			
		}, throwable -> {
			countDownLatch.countDown();
		});
		
		assertCountDown();
	}
	
	
	@Test
	void deleteBeer() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		int beerId = 1;
		webClient.delete().uri(BeerRouterConfig.BEER_V2_URL + "/" + beerId)
		.retrieve()
		.toBodilessEntity()
		.flatMap(responseEntity -> {
			countDownLatch.countDown();
			return webClient.get().uri(BeerRouterConfig.BEER_V2_URL + "/" + beerId)
					.retrieve()
					.bodyToMono(BeerDto.class); })
		.subscribe(beerDto-> {}, throwable -> {
			countDownLatch.countDown();
		});
		assertCountDown();
	}
	
	
	@Test
	void deleteBeerNotFound() {

		int badId = -100;
				
		assertThrows(WebClientResponseException.NotFound.class, () -> {
			webClient.delete().uri(BeerRouterConfig.BEER_V2_URL + "/" + badId)
			.retrieve()
			.toBodilessEntity().block();
		});
	}
	
	
	private void assertCountDown() throws InterruptedException{
		countDownLatch.await(1000, TimeUnit.MILLISECONDS);
		assertThat(countDownLatch.getCount()).isEqualTo(0);
	}

}
