package kopo.poly.service;

import kopo.poly.dto.UserInfoDTO;

public interface IUserInfoService {

    int getEmailExists(UserInfoDTO pDTO) throws Exception;

    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    int getUserLogin(UserInfoDTO pDTO) throws Exception;

    int sendEmail(UserInfoDTO pDTO) throws Exception;

    int updatePassword(UserInfoDTO pDTO) throws Exception;

    void deleteUserInfo(UserInfoDTO pDTO) throws Exception;

}
