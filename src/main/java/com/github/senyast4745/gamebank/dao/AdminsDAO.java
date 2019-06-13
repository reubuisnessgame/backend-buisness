package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.AdminModel;
import com.github.senyast4745.gamebank.model.TeamModel;
import com.github.senyast4745.gamebank.model.UserModel;
import com.github.senyast4745.gamebank.repository.TeamsRepository;
import com.github.senyast4745.gamebank.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class AdminsDAO {
    private final RepositoryComponent repositoryComponent;

    private final UserRepository userRepository;

    private final TeamsRepository teamsRepository;

    static boolean isGameStarted = false;

    public AdminsDAO(RepositoryComponent repositoryComponent, TeamsRepository teamsRepository, UserRepository userRepository) {
        this.repositoryComponent = repositoryComponent;
        this.teamsRepository = teamsRepository;
        this.userRepository = userRepository;
    }


    public AdminModel createNewAdmin(String username, String password, String userRole, Double maxScore, Double coefficient) {
        return repositoryComponent.saveNewAdmin(username, password, userRole, maxScore, coefficient);
    }

    public AdminModel changeAdmin(String username, String newUsername, String newPassword, String newRole, Double newMaxScore, Double newCoefficient) {
        return repositoryComponent.changeAdminData(username, newUsername, newPassword, newRole, newMaxScore, newCoefficient);
    }

    public void startGame() {
        isGameStarted = true;
        TeamsDAO.lastPaySharesTime = System.currentTimeMillis() + StockExchangeDAO.STOCK_PRICE_CHANGE;
    }

    public void closeGame() {
        isGameStarted = false;
    }

    public TeamModel createNewTeam(Long number) {
        return repositoryComponent.saveNewTeam(number);
    }


    public TeamModel getTeam(Long number) {
        return repositoryComponent.getTeamByNumber(number);
    }

    public UserModel lockUnlockUserByUsername(String username, boolean nonLocked) {
        UserModel userModel = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username: " + username + " not found"));
        userModel.setNonLocked(nonLocked);
        return userRepository.save(userModel);
    }

    public Iterable<TeamModel> getAllTeams() throws NotFoundException {
        Iterable<TeamModel> userModels = teamsRepository.findAll();
        List<TeamModel> teamModels = new ArrayList<>();

        userModels.forEach(teamModels::add);
        teamModels.sort((t, t1) -> (int) (t.getFullScore() - t1.getFullScore()));
        throw new NotFoundException("Teams not found");
    }

    public Iterable<AdminModel> getAllAdmins() throws NotFoundException {
        return repositoryComponent.getAllAdmins();
    }

    public AdminModel getMyInfo(String token){
        return repositoryComponent.getAdminByToken(token);
    }

    public TeamModel addScore(String token, Double rate, Long teamNumber, boolean isWin) {

        AdminModel leading = repositoryComponent.getAdminByToken(token);
        TeamModel teamModel = repositoryComponent.getTeamByNumber(teamNumber);
        if (rate > leading.getMaxScore()) {
            rate = leading.getMaxScore();
        }
        if (isWin) {
            rate *= leading.getCoefficient();
        } else {
            rate = -leading.getMaxScore();
        }
        teamModel.setScore(teamModel.getScore() + rate);
        return teamsRepository.save(teamModel);
    }

    public void clearAll() {
        repositoryComponent.clearAll();
    }
}
