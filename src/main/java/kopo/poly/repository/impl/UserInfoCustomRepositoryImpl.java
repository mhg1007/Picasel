package kopo.poly.repository.impl;

import com.mongodb.client.result.UpdateResult;
import kopo.poly.repository.UserInfoCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserInfoCustomRepositoryImpl implements UserInfoCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public int updatePasswordByEmail(String email, String newPassword) {

        Query query = new Query(Criteria.where("EMAIL").is(email));
        Update update = new Update().set("PASSWORD", newPassword);

        int res=0;
        UpdateResult result =  mongoTemplate.updateFirst(query, update, UserInfoEntity.class);

        if (result.getModifiedCount() != 0) {
            res=1;
        }

        return res;
    }
}
