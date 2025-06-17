package kopo.poly.service;

import kopo.poly.dto.ResultDTO;

import java.util.List;

public interface ISelfDiagnosisService {

    int insertResult(ResultDTO pDTO) throws Exception;

    List<ResultDTO> getResultList(String email) throws Exception;

    ResultDTO getResultInfo(ResultDTO pDTO) throws Exception;

    void deleteResult(ResultDTO pDTO) throws Exception;

}
