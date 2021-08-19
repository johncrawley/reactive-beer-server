package guru.springframework.sfgrestbrewery.repositories;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import guru.springframework.sfgrestbrewery.domain.Beer;
import reactor.core.publisher.Mono;


public interface BeerRepository extends ReactiveCrudRepository<Beer, Integer> {
	/*
    Page<Beer> findAllByBeerName(String beerName, Pageable pageable);

    Page<Beer> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);

    Page<Beer> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum beerStyle, Pageable pageable);
*/
    Mono<Beer> findByUpc(String upc);
}
