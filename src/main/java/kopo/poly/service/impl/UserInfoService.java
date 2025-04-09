package kopo.poly.service.impl;

import jakarta.mail.internet.MimeMessage;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.impl.UserInfoEntity;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public int getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!",this.getClass().getName());

        int res;

        String email= CmmUtil.nvl(pDTO.email());

        log.info("email:{}",email);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);

        if(rEntity.isPresent()) {
            res=1;
        }
        else{
            res=0;
        }
        log.info("res : {}",res);

        log.info("{}.getEmailExists End!",this.getClass().getName());

        return res;
    }

    @Override
    public int insertUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!",this.getClass().getName());

        int res=0;

        String email= CmmUtil.nvl(pDTO.email());
        String name= CmmUtil.nvl(pDTO.name());
        String password= CmmUtil.nvl(pDTO.password());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);

        if(rEntity.isPresent()) {
            res=2;
        }
        else {

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .email(email).name(name).password(password)
                    .build();

//            userInfoRepository.save(pEntity);
            userInfoRepository.insert(pEntity);

            rEntity = userInfoRepository.findByEmail(email);

            if(rEntity.isPresent()) {
                res=1;
            }

        }

        log.info("{}.insertUserInfo End!",this.getClass().getName());

        return res;
    }

    @Override
    public int getUserLogin(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserLogin Start!",this.getClass().getName());

        int res=0;

        String email= CmmUtil.nvl(pDTO.email());
        String password= CmmUtil.nvl(pDTO.password());

        log.info("email:{},password:{}",email,password);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndPassword(email, password);

        if(rEntity.isPresent()) {
            res=1;
        }

        log.info("{}.getUserLogin End!",this.getClass().getName());

        return res;
    }

    @Override
    public int sendEmail(UserInfoDTO pDTO) throws Exception {

        log.info("{}.sendEmail Start!",this.getClass().getName());

        String toMail=CmmUtil.nvl(EncryptUtil.decAES128CBC(pDTO.email()));
        String title="Picasel 인증번호";
        int authNumber= ThreadLocalRandom.current().nextInt(100000,1000000);

        log.info("toMail:{}, title:{}, authNumber:{}",toMail,title,authNumber);

        MimeMessage message= mailSender.createMimeMessage();

        MimeMessageHelper messageHelper=new MimeMessageHelper(message, "UTF-8");

        try {
            messageHelper.setTo(toMail);
            messageHelper.setFrom(fromMail);
            messageHelper.setSubject(title);
            messageHelper.setText(String.valueOf(authNumber));

            mailSender.send(message);
        }
        catch (Exception e){
            log.info("메일 발송 실패 : {}",e.toString());
        }

        log.info("{}.sendEmail End!",this.getClass().getName());

        return authNumber;
    }

    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updatePassword Start!",this.getClass().getName());

        int res=0;

        String email= CmmUtil.nvl(pDTO.email());
        String password= CmmUtil.nvl(pDTO.password());

        res=userInfoRepository.updatePasswordByEmail(email,password);

        log.info("비밀번호 변경 결과(res):{}",res);

        return res;
    }

    @Override
    public void deleteUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.deleteUserInfo Start!",this.getClass().getName());

        String email= CmmUtil.nvl(pDTO.email());

        userInfoRepository.deleteByEmail(email);

        log.info("{}.deleteUserInfo End!",this.getClass().getName());

    }
}
