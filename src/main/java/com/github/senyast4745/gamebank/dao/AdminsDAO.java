package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.Role;
import com.github.senyast4745.gamebank.model.UserModel;
import com.github.senyast4745.gamebank.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
public class AdminsDAO {

    private final UtilBean utilBean;

    private final UserRepository userRepository;

    public AdminsDAO(UtilBean utilBean, UserRepository userRepository) {
        this.utilBean = utilBean;
        this.userRepository = userRepository;
    }


    public UserModel createNewUser(String token, String username, String password, String userRole, Double maxScore, Double coefficient) {
        if (utilBean.getUserByToken(token).getRole().equals(Role.MODERATOR)) {
            return userRepository.save(new UserModel(username, password, userRole, maxScore, coefficient));
        }
        throw new AccessDeniedException("You do not have enough rights to create new users");
    }

    public void clearAll(String token) {
        if (utilBean.getUserByToken(token).getRole().equals(Role.MODERATOR)) {
            userRepository.deleteAll();
            userRepository.save(new UserModel("admin", "admin", "MODERATOR", 10D, 1.5D));
        }
        throw new AccessDeniedException("You do not have enough delete users");
    }

    public void lockUnlockUserByUsername(String token, String username, boolean nonLocked) {
        if (utilBean.getUserByToken(token).getRole().equals(Role.MODERATOR)) {
            UserModel userModel = userRepository.findByUsername(username).orElseThrow(() ->
                    new UsernameNotFoundException("Username: " + username + " not found"));
            userModel.setNonLocked(nonLocked);
            userRepository.save(userModel);
        }
        throw new AccessDeniedException("You do not have enough delete users");
    }

    public Iterable<UserModel> getAllTeams() throws NotFoundException {
        Iterable<UserModel> userModels = userRepository.findAllByRoleOrderByScore(Role.TEAM);
        if (userModels.iterator().hasNext()) {
            return userModels;
        }
        throw new NotFoundException("Teams not found");
    }

    public Iterable<UserModel> getAllUsers() throws NotFoundException {
        Iterable<UserModel> userModels = userRepository.findAll();
        if (userModels.iterator().hasNext()) {
            return userModels;
        }
        throw new NotFoundException("Users not found");
    }

    public void addScore(String token, Double rate, Long teamNumber, boolean isWin) {

        UserModel leading = utilBean.getUserByToken(token);
        UserModel userModel = userRepository.findByTeamNumber(teamNumber).orElseThrow(() ->
                new UsernameNotFoundException("Team number: " + teamNumber + " not found"));

        if (isWin) {
            if (rate > leading.getMaxScore()) {
                rate = leading.getMaxScore();
            }
            rate *= leading.getCoefficient();
        } else {
            if (rate < -leading.getMaxScore()) {
                rate = -leading.getMaxScore();
            }
        }
        if (userModel.getRole().equals(Role.TEAM)) {
            userModel.setScore(userModel.getScore() + rate);
            userRepository.save(userModel);
        }
        throw new IllegalArgumentException("Incorrect data");
    }

    public UserModel getTeam(Long number) {
        return utilBean.getTeam(number);
    }
}
