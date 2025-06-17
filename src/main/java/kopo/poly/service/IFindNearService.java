package kopo.poly.service;

import kopo.poly.dto.CenterDTO;

import java.util.List;

public interface IFindNearService {

    List<CenterDTO> getNearCenters(CenterDTO pDTO) throws Exception;

}
