package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record CenterDTO(

        String name,
        String address,
        String tel,
        double lat,
        double lng

) {
}
