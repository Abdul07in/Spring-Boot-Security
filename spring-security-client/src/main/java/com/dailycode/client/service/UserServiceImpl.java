package com.dailycode.client.service;

import com.dailycode.client.entity.PasswordResetToken;
import com.dailycode.client.entity.User;
import com.dailycode.client.entity.VerificationToken;
import com.dailycode.client.model.PasswordModal;
import com.dailycode.client.model.UserModel;
import com.dailycode.client.repository.PasswordResetTokenRepository;
import com.dailycode.client.repository.UserRepository;
import com.dailycode.client.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetRepository;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "invalid token";
        }

        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if ((verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
//            tokenRepository.delete(verificationToken);
            return "Token Expired";
        }
        user.setEnable(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = tokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(verificationToken.calculateExpirationDate());
        tokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(PasswordModal passwordModal) {
        return userRepository.findByEmail(passwordModal.getEmail());
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);
        passwordResetToken.setExpirationTime(passwordResetToken.calculateExpirationDate());
        passwordResetRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid token";
        }

        User user = passwordResetToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if ((passwordResetToken.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
//            passwordResetRepository.delete(verificationToken);
            return "Token Expired";
        }
        user.setEnable(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetRepository.findByToken(token).getUser());
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
