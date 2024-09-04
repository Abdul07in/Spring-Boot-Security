package com.dailycode.client.service;

import com.dailycode.client.entity.User;
import com.dailycode.client.entity.VerificationToken;
import com.dailycode.client.model.PasswordModal;
import com.dailycode.client.model.UserModel;

import java.util.Optional;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(PasswordModal passwordModal);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void updatePassword(User user, String newPassword);

    boolean checkValidOldPassword(User user, String oldPassword);
}
