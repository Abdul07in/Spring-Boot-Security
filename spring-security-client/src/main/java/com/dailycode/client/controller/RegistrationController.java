package com.dailycode.client.controller;

import com.dailycode.client.entity.User;
import com.dailycode.client.entity.VerificationToken;
import com.dailycode.client.event.RegistrationCompleteEvent;
import com.dailycode.client.model.PasswordModal;
import com.dailycode.client.model.UserModel;
import com.dailycode.client.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        return "Success";
    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, final HttpServletRequest request) {
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl + "/verifyRegistration?token=" + verificationToken.getToken();
        log.info("Click here to get link {}", url);
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModal passwordModal, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModal);
        String url = "";
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }
        return "";
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl + "/savePassword?token=" + token;
        log.info("Click here to reset password get link {}", url);
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token , @RequestBody PasswordModal passwordModal , HttpServletRequest request) {
        String result = userService.validatePasswordResetToken(token);
        if ("valid".equalsIgnoreCase(result)) {
            Optional<User> user = userService.getUserByPasswordResetToken(token);
            if (user.isPresent()) {
                userService.updatePassword(user.get(), passwordModal.getNewPassword());
                return "Password Changed Successfully";
            } else {
                return "Invalid Token";
            }
        } else {
            return "Verification Failed : " + result;
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModal passwordModal) {
        User user = userService.findUserByEmail(passwordModal);
        if (!userService.checkValidOldPassword(user , passwordModal.getOldPassword())) {
            return "Invalid Old Password";
        }
        userService.updatePassword(user, passwordModal.getNewPassword());
        return "Password Changed Successfully";
    }


    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token, HttpServletRequest request) {
        String result = userService.validateVerificationToken(token);
        if ("valid".equalsIgnoreCase(result)) {
            return "User Verifies Successfully";
        } else {
            if ("Token Expired".equalsIgnoreCase(result))
                return "Token Expired Click Here To Resend : " + applicationUrl(request) + "/resendVerifyToken?token=" + token;
            return "Verification Failed : " + result;
        }
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
