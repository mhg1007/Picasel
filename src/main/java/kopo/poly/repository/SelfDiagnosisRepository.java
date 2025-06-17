package kopo.poly.repository;

import kopo.poly.repository.impl.SelfDiagnosisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SelfDiagnosisRepository extends MongoRepository<SelfDiagnosisEntity,String> {

    Optional<SelfDiagnosisEntity> findByEmailAndSaveTime(String email, String saveTime);

    Optional<List<SelfDiagnosisEntity>> findAllByEmailOrderBySaveTimeDesc(String email);

    void deleteByEmail(String email);

}
