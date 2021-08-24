package guru.springframework.sfgrestbrewery.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

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
	
	
	private void assertCountDown() throws InterruptedException{
		countDownLatch.await(1000, TimeUnit.MILLISECONDS);
		assertThat(countDownLatch.getCount()).isEqualTo(0);
	}

}
