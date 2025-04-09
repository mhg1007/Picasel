package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ResultDTO(

        @NotBlank
        String email,

        @NotBlank
        String result,

        @NotBlank
        String saveTime

) {
}
