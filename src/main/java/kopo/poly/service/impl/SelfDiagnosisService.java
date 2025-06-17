package kopo.poly.service.impl;

import kopo.poly.dto.ResultDTO;
import kopo.poly.repository.SelfDiagnosisRepository;
import kopo.poly.repository.impl.SelfDiagnosisEntity;
import kopo.poly.service.ISelfDiagnosisService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SelfDiagnosisService implements ISelfDiagnosisService {

    private final SelfDiagnosisRepository selfDiagnosisRepository;

    @Override
    public int insertResult(ResultDTO pDTO) throws Exception {

        log.info("{}.insertResult Start!",this.getClass().getName());

        int res=0;

        String email= CmmUtil.nvl(pDTO.email());
        String result= CmmUtil.nvl(pDTO.result()).substring(0,2);
        if(result.equals("매우")){
            result+=" 우울";
        }
        String saveTime= CmmUtil.nvl(DateUtil.getDateTime("yyyy-MM-dd HH:mm"));

        SelfDiagnosisEntity pEntity=SelfDiagnosisEntity.builder()
                .email(email).result(result).saveTime(saveTime)
                .build();

        selfDiagnosisRepository.insert(pEntity);

        Optional<SelfDiagnosisEntity> rEntity=selfDiagnosisRepository.findByEmailAndSaveTime(email,saveTime);

        if(rEntity.isPresent()) {
            res=1;
        }

        log.info("{}.insertResult End!",this.getClass().getName());

        return res;
    }

    @Override
    public List<ResultDTO> getResultList(String email) throws Exception {

        log.info("{}.getResultList Start!",this.getClass().getName());

        List<SelfDiagnosisEntity> rList = selfDiagnosisRepository
                .findAllByEmailOrderBySaveTimeDesc(email)
                .orElseGet(ArrayList::new);

        List<ResultDTO> nList = rList.stream().map(entity ->
                ResultDTO.builder()
                        .email(entity.getEmail())
                        .saveTime(entity.getSaveTime())
                        .result(entity.getResult())
                        .build()
        ).toList();

        log.info("{}.getResultList End!", this.getClass().getName());

        return nList;
    }

    @Override
    public ResultDTO getResultInfo(ResultDTO pDTO) throws Exception {

        log.info("{}.getNoticeInfo Start!", this.getClass().getName());

        Optional<SelfDiagnosisEntity> rEntity=selfDiagnosisRepository.findByEmailAndSaveTime(pDTO.email(), pDTO.saveTime());

        ResultDTO rDTO = rEntity.map(entity -> ResultDTO.builder()
                .email(entity.getEmail())
                .saveTime(entity.getSaveTime())
                .result(entity.getResult())
                .build()
        ).orElseGet(() -> ResultDTO.builder().build());

        log.info("{}.getNoticeInfo End!", this.getClass().getName());

        return rDTO;

    }

    @Override
    public void deleteResult(ResultDTO pDTO) throws Exception {

        log.info("{}.deleteResult Start!", this.getClass().getName());

        String email= CmmUtil.nvl(pDTO.email());

        selfDiagnosisRepository.deleteByEmail(email);

        log.info("{}.deleteResult End!", this.getClass().getName());

    }
}
