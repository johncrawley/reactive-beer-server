package guru.springframework.sfgrestbrewery.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

/**
 * Created by jt on 3/7/21.
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientIT{
    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;
    CountDownLatch countDownLatch;
    private final String V1_API = "api/v1/beer";
    private final String V1_API_SLASH = V1_API + "/";

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();

        countDownLatch = new CountDownLatch(1);
    }
    
    
    @Test
    void getBeerById() throws InterruptedException {

    	Mono<BeerDto> beerDtoMono = webClient.get().uri("api/v1/beer/1")
    			.accept(MediaType.APPLICATION_JSON)
    			.retrieve().bodyToMono(BeerDto.class);
    	
    	beerDtoMono.subscribe(beer -> {
    		assertThat(beer).isNotNull();
    		assertThat(beer.getBeerName()).isNotNull();
    		countDownLatch.countDown();
    	});
    	
    	countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    	assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
    
    
    @Test
    void getBeerByUpc() throws InterruptedException {
    	
    	Mono<BeerDto> beerDtoMono = webClient.get().uri("api/v1/beerUpc/" + BeerLoader.BEER_10_UPC)
    			.accept(MediaType.APPLICATION_JSON)
    			.retrieve().bodyToMono(BeerDto.class);
    	
    	beerDtoMono.subscribe(beer -> {
    		assertThat(beer).isNotNull();
    		assertThat(beer.getBeerName()).isNotNull();
    		countDownLatch.countDown();
    	});
    	
    	countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    	assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
    
    
    
    

    @Test
    void testListBeers() throws InterruptedException {

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);


//        BeerPagedList pagedList = beerPagedListMono.block();
//        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await();
    }
    
    @Test
    void testSaveNewBeer() throws InterruptedException{
    	
    	BeerDto beerDto = BeerDto.builder()
    			.beerName("Test Beer")
    			.upc("1234555")
    			.beerStyle("PALE_ALE")
    			.price(new BigDecimal("8.99"))
    			.build();	
    	
    	Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri("/api/v1/beer")
    			.accept(MediaType.APPLICATION_JSON)
    			.body(BodyInserters.fromValue(beerDto))
    			.retrieve()
    			.toBodilessEntity();
    	
    		beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity ->{
    			assertThat(responseEntity.getStatusCode().is2xxSuccessful());
    			countDownLatch.countDown();
    		});
    		
    		countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    		assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
    
    
    @Test
    void updateBeer() throws InterruptedException {
    	countDownLatch = new CountDownLatch(3);
    	
    	webClient.get().uri(V1_API)
    		.accept(MediaType.APPLICATION_JSON)
    		.retrieve()
    		.bodyToMono(BeerPagedList.class)
    		.publishOn(Schedulers.single())
    		.subscribe( pagedList -> {
    			countDownLatch.countDown();
    			BeerDto beerDto = pagedList.getContent().get(0);
    			
    			String updatedBeerName = "updatedBeer";
    			BeerDto updatedBeer = BeerDto.builder()
    					.beerName(updatedBeerName)
    					.beerStyle(beerDto.getBeerStyle())
    					.upc(beerDto.getUpc())
    					.price(beerDto.getPrice())
    					.build();
    			
    			webClient.put().uri(V1_API_SLASH + beerDto.getId())
    			.contentType(MediaType.APPLICATION_JSON)
    			.body(BodyInserters.fromValue(updatedBeer))
    			.retrieve()
    			.toBodilessEntity()
    			.flatMap(responseEntity -> {
    				countDownLatch.countDown();
    				return webClient.get().uri(V1_API_SLASH + beerDto.getId())
    						.accept(MediaType.APPLICATION_JSON)
    						.retrieve()
    						.bodyToMono(BeerDto.class);
    			}).subscribe( savedDto -> {
    				assertThat(savedDto.getBeerName()).isEqualTo(updatedBeerName);
    				countDownLatch.countDown();
    			});
    		});
    	countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    	assertThat(countDownLatch.getCount()).isEqualTo(0);
    	
    }
    
}

