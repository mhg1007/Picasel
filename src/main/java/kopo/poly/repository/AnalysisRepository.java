package kopo.poly.repository;

import kopo.poly.repository.impl.AnalysisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends MongoRepository<AnalysisEntity,String> {

    Optional<AnalysisEntity> findByEmailAndSaveTime(String email, String saveTime);

    Optional<List<AnalysisEntity>> findAllByEmailOrderBySaveTimeDesc(String email);

    void deleteByEmail(String email);

}
