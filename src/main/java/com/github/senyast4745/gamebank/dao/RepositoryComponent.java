package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.form.FullTeamForm;
import com.github.senyast4745.gamebank.model.*;
import com.github.senyast4745.gamebank.repository.AdminRepository;
import com.github.senyast4745.gamebank.repository.ShareRepository;
import com.github.senyast4745.gamebank.repository.TeamsRepository;
import com.github.senyast4745.gamebank.repository.UserRepository;
import com.github.senyast4745.gamebank.secutity.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import javassist.NotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class RepositoryComponent {

    private final UserRepository userRepository;

    private final AdminRepository adminRepository;

    private final TeamsRepository teamsRepository;

    private final
    JwtTokenProvider jwtTokenProvider;

    private final ShareRepository shareRepository;

    public RepositoryComponent(UserRepository userRepository, AdminRepository adminRepository, TeamsRepository teamsRepository, JwtTokenProvider jwtTokenProvider, ShareRepository shareRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teamsRepository = teamsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.shareRepository = shareRepository;
    }

    Iterable<AdminModel> getAllAdmins() throws NotFoundException {
        Iterable<AdminModel> adminModels = adminRepository.findAll();
        if (!adminModels.iterator().hasNext()) {
            throw new NotFoundException("Admins not found");
        }
        return adminModels;
    }

    AdminModel getAdminByToken(String token) {
        Long userId = getUserIdFromToken(token);
        return adminRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("Admin ID: " + userId + " not found"));
    }

    TeamModel getTeamByToken(String token) {
        Long userId = getUserIdFromToken(token);
        return teamsRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("Team ID: " + userId + " not found"));
    }


    private Long getUserIdFromToken(String token) {
        Jws<Claims> claims = jwtTokenProvider.getClaims(resolveToken(token));
        return (Long) claims.getBody().get("userId");
    }


    TeamModel getTeamByNumber(Long number) {
        return teamsRepository.findByTeamNumber(number).orElseThrow(() ->
                new UsernameNotFoundException("Number: " + number + " not found"));
    }

    TeamModel saveTeam(TeamModel model) {
        return teamsRepository.save(model);
    }

    FullTeamForm getTeamFullInfo(TeamModel teamModel){
        Iterable<ShareModel> shareModels = shareRepository.findAllByUserId(teamModel.getId());
        FullTeamForm fullTeamForm = new FullTeamForm();
        fullTeamForm.setTeam(teamModel);
        fullTeamForm.setShares(shareModels);
        return fullTeamForm;
    }


    AdminModel saveNewAdmin(String username, String password, String role, Double maxScore, Double coefficient) {
        if (username == null || password == null || role == null || username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            throw new IllegalArgumentException("Incorrect data in creating new admin");
        }
        username = username.trim();
        password = password.trim();
        role = role.trim().toUpperCase();
        UserModel userModel = new UserModel(username, password, role);
        if (userModel.getRole().equals(Role.TEAM)) {
            throw new IllegalArgumentException("Incorrect role in creating new admin");
        }
        userRepository.save(userModel);
        AdminModel adminModel = new AdminModel(userModel.getId(), username, userModel.getRole(), maxScore, coefficient);
        return adminRepository.save(adminModel);
    }

    TeamModel saveNewTeam(Long number) {
        if (number == null || number.toString().length() != 16) {
            throw new IllegalArgumentException("Incorrect data in creating new team");

        }
        UserModel userModel = new UserModel(number.toString(), null, "TEAM");
        userRepository.save(userModel);
        TeamModel teamModel = new TeamModel(userModel.getId(), number);
        return teamsRepository.save(teamModel);
    }

    AdminModel changeAdminData(String username, String newUsername, String newPassword, String newRole, Double newMaxScore, Double newCoefficient) {
        AdminModel adminModel = adminRepository.findAllByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Admin Username: " + username + " not found"));
        UserModel userModel = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Admin Username: " + username + " not found"));

        if (newUsername != null && !newUsername.trim().isEmpty()) {
            newUsername = newUsername.trim();
            adminModel.setUsername(newUsername);
            userModel.setUsername(newUsername);
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            newPassword = newPassword.trim();
            userModel.setPassword(newPassword);
        }
        if (newRole != null && !newRole.trim().isEmpty()) {
            newRole = newRole.trim().toUpperCase();
            switch (newRole) {
                case "MODERATOR":
                    adminModel.setRole(Role.MODERATOR);
                    break;
                case "EXCHANGE_WORKER": {
                    adminModel.setRole(Role.EXCHANGE_WORKER);
                    break;
                }
                case "LEADING": {
                    adminModel.setRole(Role.LEADING);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Incorrect role in changing admin " + username);
            }
        }

        if (newMaxScore != null && newMaxScore > 0) {
            adminModel.setMaxScore(newMaxScore);
        }

        if (newCoefficient != null && newCoefficient > 1) {
            adminModel.setCoefficient(newCoefficient);
        }
        userRepository.save(userModel);
        return adminRepository.save(adminModel);

    }

    TeamModel changeTeamUsername(String token, String newUsername) {
        TeamModel teamModel = getTeamByToken(token);
        UserModel userModel = userRepository.findByUsername(teamModel.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException("Team Username: " + teamModel.getUsername() + " not found"));
        teamModel.setUsername(newUsername);
        userModel.setUsername(newUsername);
        userRepository.save(userModel);
        return teamsRepository.save(teamModel);
    }

    Double calculateFullScore(Long id) {
        TeamModel teamModel = teamsRepository.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("Team ID: " + id + " not found"));
        Iterable<ShareModel> shareModels = shareRepository.findAllByUserId(teamModel.getId());
        AtomicReference<Double> fullScore = new AtomicReference<>((double) 0);
        shareModels.forEach((s) -> {
            double price = s.getCompanyModel().getSharePrice() * s.getSharesNumbers();
            fullScore.updateAndGet(v -> v + price);
        });
        return fullScore.get();
    }

    void clearAll() {
        userRepository.deleteAll();
        adminRepository.deleteAll();
        teamsRepository.deleteAll();
        saveNewAdmin("admin", "admin", "MODERATOR", 0D, 0D);
    }


    private String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Incorrect token");
    }


}
