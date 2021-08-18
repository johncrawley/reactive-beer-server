package guru.springframework.sfgrestbrewery.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;

/**
 * Created by jt on 2019-05-25.
 */
//@Mapper(uses = {DateMapper.class})
@Mapper
public interface BeerMapper {

	@Mapping(target = "quantityOnHand", ignore = true)
    BeerDto beerToBeerDto(Beer beer);

    BeerDto beerToBeerDtoWithInventory(Beer beer);

    Beer beerDtoToBeer(BeerDto dto);
}
