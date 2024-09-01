package com.dailycode.client.event.listener;

import com.dailycode.client.entity.User;
import com.dailycode.client.event.RegistrationCompleteEvent;
import com.dailycode.client.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // TODO : Create the Verification Token for the user with link

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        userService.saveVerificationTokenForUser(token, user);

        // TODO: Send Email to User. Later Create Send Email To User

        String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;

        log.info("Click the Link to Verify : {}", url);
    }
}
