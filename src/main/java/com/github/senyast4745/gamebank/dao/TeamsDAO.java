package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.form.FullTeamForm;
import com.github.senyast4745.gamebank.model.ShareModel;
import com.github.senyast4745.gamebank.model.TeamModel;
import com.github.senyast4745.gamebank.repository.ShareRepository;
import com.github.senyast4745.gamebank.repository.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TeamsDAO {

    //TODO

    private final double creditRate = 1.12;
    private final double depositRate = 1.07;

    public static final long PAY_TIME = 1_800_000; // 30 minutes
    private final long SLEEP_CHECK_TIME = 30_000; // 30 seconds

    private final double sharePercent = 0.25;

    static long lastPaySharesTime;


    private final Logger LOGGER = LoggerFactory.getLogger(TeamsDAO.class.getSimpleName());

    private final
    TeamsRepository teamsRepository;

    private final RepositoryComponent repositoryComponent;

    private final ShareRepository shareRepository;

    public TeamsDAO(TeamsRepository teamsRepository, RepositoryComponent repositoryComponent, ShareRepository shareRepository) {
        this.teamsRepository = teamsRepository;
        lastPaySharesTime = System.currentTimeMillis() + StockExchangeDAO.STOCK_PRICE_CHANGE;
        CheckingThread checkingThread = new CheckingThread();
        checkingThread.start();

        this.repositoryComponent = repositoryComponent;
        this.shareRepository = shareRepository;
    }

    public TeamModel getTeam(String token){
        return repositoryComponent.getTeamByToken(token);
    }

    public FullTeamForm getTeamFullInfo(String token){
        TeamModel teamModel = getTeam(token);
        return repositoryComponent.getTeamFullInfo(teamModel);
    }





    public TeamModel changeUsername(String token, String newUsername) {
        return repositoryComponent.changeTeamUsername(token, newUsername);
    }

    // take a credit
    public TeamModel takeCredit(String token, Double credit) throws IllegalAccessException {
        if (AdminsDAO.isGameStarted) {
            TeamModel teamModel = repositoryComponent.getTeamByToken(token);
            teamModel.setScore(teamModel.getScore() + credit);
            teamModel.setCredit(teamModel.getCredit() + credit);
            teamModel.setCreditTime(System.currentTimeMillis() + PAY_TIME);
            return teamsRepository.save(teamModel);
        }
        throw new IllegalAccessException("The game has not started yet");
    }


    // open a contribution
    public TeamModel takeDeposit(String token, Double deposit) throws IllegalAccessException {
        if (AdminsDAO.isGameStarted) {
            TeamModel teamModel = repositoryComponent.getTeamByToken(token);
            Double tmpDeposit = teamModel.getDeposit();
            if (deposit > tmpDeposit) {
                deposit = tmpDeposit;
            }
            teamModel.setScore(teamModel.getScore() - deposit);
            teamModel.setDeposit(tmpDeposit + deposit);
            teamModel.setCreditTime(System.currentTimeMillis() + PAY_TIME);
            return teamsRepository.save(teamModel);
        }
        throw new IllegalAccessException("The game has not started yet");
    }


    // reply a Loan
    public TeamModel replyLoan(String token, Double credit) throws IllegalAccessException {
        if (AdminsDAO.isGameStarted) {
            credit = Math.round(credit * 100) / 100D;
            TeamModel teamModel = repositoryComponent.getTeamByToken(token);
            Double tmpCredit = teamModel.getCredit();
            if (credit > tmpCredit) {
                credit = tmpCredit;
            }
            tmpCredit -= credit;
            if (tmpCredit < 0.01) {
                tmpCredit = 0D;
                teamModel.setCreditTime(null);
            }
            teamModel.setCredit(tmpCredit);
            teamModel.setScore(teamModel.getScore() - credit);
            return teamsRepository.save(teamModel);
        }
        throw new IllegalAccessException("The game has not started yet");
    }

    // withdraw money from a deposit
    public TeamModel returnDeposit(String token, Double deposit) throws IllegalAccessException {
        if (AdminsDAO.isGameStarted) {
            deposit = Math.round(deposit * 100) / 100D;
            TeamModel teamModel = repositoryComponent.getTeamByToken(token);
            Double tmpDeposit = teamModel.getDeposit();
            if (deposit > tmpDeposit) {
                deposit = tmpDeposit;
            }
            tmpDeposit -= deposit;
            teamModel.setScore(teamModel.getScore() + deposit);
            if (tmpDeposit < 0.01) {
                tmpDeposit = 0D;
                teamModel.setDepositTime(null);
            }
            teamModel.setDeposit(tmpDeposit);
            return teamsRepository.save(teamModel);
        }
        throw new IllegalAccessException("The game has not started yet");
    }

    // !!ALARM!! Database updated 30 seconds

    class CheckingThread extends Thread {

        public void run() {
            try {
                while (true) {
                    Thread.sleep(SLEEP_CHECK_TIME);
                    if (AdminsDAO.isGameStarted) {
                        Iterable<TeamModel> teams = teamsRepository.findAll();
                        teams.forEach((team) -> {
                            Long tmpCreditTime = team.getCreditTime();
                            Long tmpDepositTime = team.getDepositTime();
                            if (tmpCreditTime != null && tmpCreditTime <= System.currentTimeMillis()) {
                                Double tmpCredit = team.getCredit();
                                tmpCredit = Math.round(tmpCredit * creditRate * 100) / 100D;
                                tmpCreditTime += PAY_TIME;
                                team.setCredit(tmpCredit);
                                team.setCreditTime(tmpCreditTime);
                            }
                            if (tmpDepositTime != null && tmpDepositTime <= System.currentTimeMillis()) {
                                Double tmpDeposit = team.getDeposit();
                                tmpDeposit = Math.round(tmpDeposit * depositRate * 100) / 100D;
                                tmpDepositTime += PAY_TIME;
                                team.setCredit(tmpDeposit);
                                team.setCreditTime(tmpDepositTime);
                            }
                            double sharesPrice = repositoryComponent.calculateFullScore(team.getId());
                            double score = team.getScore();
                            if (lastPaySharesTime <= System.currentTimeMillis()) {
                                score += sharesPrice * sharePercent;
                                team.setScore(score);
                            }
                            double fullScore = score + team.getCredit() + team.getDeposit() + sharesPrice;
                            team.setFullScore(fullScore);
                            teamsRepository.save(team);

                        });
                    }

                }
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
