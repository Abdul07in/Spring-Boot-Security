package com.dailycode.client.controller;

import com.dailycode.client.entity.User;
import com.dailycode.client.entity.VerificationToken;
import com.dailycode.client.event.RegistrationCompleteEvent;
import com.dailycode.client.model.UserModel;
import com.dailycode.client.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

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

    private String resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl + "/verifyRegistration?token=" + verificationToken.getToken();
        log.info("Click here to get link {}", url);
        return url;
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
