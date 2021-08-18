package guru.springframework.sfgrestbrewery.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 2019-04-20.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto {

    @Null
    private Integer id;

    @NotBlank
    private String beerName;

    @NotBlank
    private String beerStyle;

    private String upc;

    private BigDecimal price;

    private Integer quantityOnHand;

    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
}
