package kopo.poly.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.CenterDTO;
import kopo.poly.service.IFindNearService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FindNearService implements IFindNearService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vworld.api.key}")
    private String VWORLD_API_KEY;

    @Override
    public List<CenterDTO> getNearCenters(CenterDTO pDTO) throws Exception {

        log.info("{}.getNearCenters Start!",this.getClass().getName());

        double lat= pDTO.lat();
        double lng= pDTO.lng();
        log.info("lat: {}, lng: {}", lat, lng);

        int buffer=30000;
        String url = String.format(
                "https://api.vworld.kr/req/data?" +
                "service=data&request=GetFeature&version=2.0.0&" +
                "data=LT_P_MGPRTFC&key=%s&"+
//                "domain=localhost:11000/findnear/find&"+//http테스트용, https연결완료시 제거
                "domain=https://rementia.kr/findnear/find&"+
                "geomFilter=POINT(%f %f)&"+
//                "attrFilter=cat_nam:like:아동복지시설"+
                "buffer=%d&format=JSON&crs=EPSG:4326",
                VWORLD_API_KEY, lng, lat, buffer
        );

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("response: {}", response);
        List<CenterDTO> centerList = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode features = root.path("response").path("result").path("featureCollection").path("features");

                for (JsonNode feature : features) {
                    JsonNode geometry = feature.path("geometry");
                    JsonNode properties = feature.path("properties");

                    CenterDTO center = CenterDTO.builder()
                            .name(properties.path("fac_nam").asText("아동상담소"))
                            .address(properties.path("fac_n_add").asText("아동상담소"))
                            .tel(properties.path("fac_tel").asText("전화번호"))
                            .lat(geometry.path("coordinates").get(1).asDouble())
                            .lng(geometry.path("coordinates").get(0).asDouble())
                            .build();

                    centerList.add(center);
                }

            } catch (Exception e) {
                throw new RuntimeException("브이월드 응답 파싱 실패: " + e.getMessage());
            }
        }

        log.info("확인된 아동복지시설 수 : {}", centerList.size());

        log.info("{}.getNearCenters End!",this.getClass().getName());

        return centerList;
    }
}
