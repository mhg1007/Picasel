package kopo.poly.repository.impl;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "selfDiagnosis")
public class SelfDiagnosisEntity {

    @NonNull
    @Field(name = "EMAIL")
    private String email;

    @NonNull
    @Field(name = "RESULT")
    private String result;

    @NonNull
    @Field(name = "SAVE_TIME")
    private String saveTime;

}
