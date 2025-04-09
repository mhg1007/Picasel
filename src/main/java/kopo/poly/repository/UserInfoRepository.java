package kopo.poly.repository;


import kopo.poly.repository.impl.UserInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfoEntity,String>, UserInfoCustomRepository {

    Optional<UserInfoEntity> findByEmail(String email);

    Optional<UserInfoEntity> findByEmailAndPassword(String email, String password);

//    Optional<UserInfoEntity> updatePassword(String email, String password);

    void deleteByEmail(String email);

}
