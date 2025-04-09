package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "이름을 입력해주세요.")
        String name,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password

) {
}
