package guru.springframework.sfgrestbrewery.web.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;


@WebFluxTest(BeerController.class)
public class BeerControllerTest {

	@Autowired
	WebTestClient webTestClient;
	
	@MockBean
	BeerService beerService;
	
	BeerDto validBeer;
	
	
	@BeforeEach
	void setup() {
		validBeer = BeerDto.builder()
				.beerName("test beer")
				.beerStyle("PALE_ALE")
				.upc(BeerLoader.BEER_10_UPC)
				.build();
	}
	
	@Test
	void getBeerById() {
			UUID beerId = UUID.randomUUID();
			given(beerService.getById(any(), any())).willReturn(validBeer);
			
			webTestClient.get()
			.uri("/api/v1/beer/" + beerId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(BeerDto.class)
			.value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));
	}

	
	@Test
	void getBeerByUpc() {
			String beerUpc = validBeer.getUpc();
			given(beerService.getByUpc(beerUpc)).willReturn(validBeer);
			
			webTestClient.get()
			.uri("/api/v1/beerUpc/" + beerUpc)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(BeerDto.class)
			.value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));
	}

	
	
	@Test
	void getListOfBeers() {
		
		List<BeerDto> beerList = Collections.singletonList(validBeer);
		BeerPagedList beerPagedList = new BeerPagedList(beerList, PageRequest.of(1, 1), beerList.size());
		
		given(beerService.listBeers(any(), any(), any(), any())).willReturn(beerPagedList);

		webTestClient.get()
		.uri("/api/v1/beer/")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectBody(BeerPagedList.class);
		
		
	}
	
	
}
