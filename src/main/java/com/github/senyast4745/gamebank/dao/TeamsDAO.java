package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.ShareModel;
import com.github.senyast4745.gamebank.model.UserModel;
import com.github.senyast4745.gamebank.repository.CompanyRepository;
import com.github.senyast4745.gamebank.repository.ShareRepository;
import com.github.senyast4745.gamebank.repository.UserRepository;
import com.github.senyast4745.gamebank.secutity.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class TeamsDAO {

    //TODO


    private final double creditRate = 1.12;
    private final double depositRate = 1.07;

    public static final long PAY_TIME = 1_800_000; // 30 minutes
    private final long sleepCheckTime = 60_000; // 1 minutes



    private final Logger LOGGER = LoggerFactory.getLogger(TeamsDAO.class.getSimpleName());

    private final CompanyRepository companyRepository;

    private final ShareRepository shareRepository;

    private final UtilBean utilBean;

    private final
    UserRepository userRepository;

    public TeamsDAO(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, UtilBean utilBean, CompanyRepository companyRepository, ShareRepository shareRepository) {
        this.userRepository = userRepository;
        CheckingThread checkingThread = new CheckingThread();
        checkingThread.start();
        this.utilBean = utilBean;
        this.companyRepository = companyRepository;
        this.shareRepository = shareRepository;
    }

    public UserModel getTeam(Long number) {
        return utilBean.getTeam(number);
    }

    // take a credit
    public UserModel takeCredit(String token, Double credit) {
        UserModel userModel = utilBean.getUserByToken(token);
        userModel.setScore(userModel.getScore() + credit);
        userModel.setCredit(userModel.getCredit() + credit);
        userModel.setCreditTime(System.currentTimeMillis() + PAY_TIME);
        return userRepository.save(userModel);
    }


    // open a contribution
    public UserModel takeDeposit(String token, Double deposit) {
        UserModel userModel = utilBean.getUserByToken(token);
        Double tmpDeposit = userModel.getDeposit();
        if (deposit > tmpDeposit) {
            deposit = tmpDeposit;
        }
        userModel.setScore(userModel.getScore() - deposit);
        userModel.setDeposit(tmpDeposit + deposit);
        userModel.setCreditTime(System.currentTimeMillis() + PAY_TIME);
        return userRepository.save(userModel);
    }


    // reply a Loan
    public UserModel replyLoan(String token, Double credit) {
        credit = Math.round(credit * 100) / 100D;
        UserModel userModel = utilBean.getUserByToken(token);
        Double tmpCredit = userModel.getCredit();
        if (credit > tmpCredit) {
            credit = tmpCredit;
        }
        tmpCredit -= credit;
        if (tmpCredit < 0.01) {
            tmpCredit = 0D;
            userModel.setCreditTime(null);
        }
        userModel.setCredit(tmpCredit);
        userModel.setScore(userModel.getScore() - credit);
        return userRepository.save(userModel);
    }

    // withdraw money from a deposit
    public UserModel returnDeposit(String token, Double deposit) {

        deposit = Math.round(deposit * 100) / 100D;
        UserModel userModel = utilBean.getUserByToken(token);
        Double tmpDeposit = userModel.getDeposit();
        if (deposit > tmpDeposit) {
            deposit = tmpDeposit;
        }
        tmpDeposit -= deposit;
        userModel.setScore(userModel.getScore() + deposit);
        if (tmpDeposit < 0.01) {
            tmpDeposit = 0D;
            userModel.setDepositTime(null);
        }
        userModel.setDeposit(tmpDeposit);
        return userRepository.save(userModel);
    }


    public Iterable<ShareModel> getTeamShares(Long number){
        return shareRepository.findAllByUserId(utilBean.getTeam(number).getId());
    }



    //TODO





    class CheckingThread extends Thread {

        public void run() {
            try {
                while (true) {
                    Thread.sleep(sleepCheckTime);
                    Iterable<UserModel> teams = userRepository.findAll();
                    teams.forEach((team) -> {
                        Long tmpCreditTime = team.getCreditTime();
                        Long tmpDepositTime = team.getDepositTime();
                        if (tmpCreditTime != null && tmpCreditTime < System.currentTimeMillis()) {
                            Double tmpCredit = team.getCredit();
                            tmpCredit = Math.round(tmpCredit * creditRate * 100) / 100D;
                            tmpCreditTime += PAY_TIME;
                            team.setCredit(tmpCredit);
                            team.setCreditTime(tmpCreditTime);
                        }
                        if (tmpDepositTime != null && tmpDepositTime < System.currentTimeMillis()) {
                            Double tmpDeposit = team.getDeposit();
                            tmpDeposit = Math.round(tmpDeposit * depositRate * 100) / 100D;
                            tmpDepositTime += PAY_TIME;
                            team.setCredit(tmpDeposit);
                            team.setCreditTime(tmpDepositTime);
                        }
                        userRepository.save(team);
                    });

                }
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
