package kopo.poly.repository.impl;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "userInfo")
public class UserInfoEntity {

    @NonNull
    @Field(name = "EMAIL")
    private String email;

    @NonNull
    @Field(name = "NAME")
    private String name;

    @NonNull
    @Field(name = "PASSWORD")
    private String password;

}
