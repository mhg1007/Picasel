package kopo.poly.repository;

public interface UserInfoCustomRepository {
    int updatePasswordByEmail(String email, String newPassword);
}
