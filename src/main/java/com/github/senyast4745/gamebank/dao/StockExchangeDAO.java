package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.form.FullTeamForm;
import com.github.senyast4745.gamebank.model.ChangingPriceModel;
import com.github.senyast4745.gamebank.model.CompanyModel;
import com.github.senyast4745.gamebank.model.ShareModel;
import com.github.senyast4745.gamebank.model.TeamModel;
import com.github.senyast4745.gamebank.repository.ChangingPriceRepository;
import com.github.senyast4745.gamebank.repository.CompanyRepository;
import com.github.senyast4745.gamebank.repository.ShareRepository;
import com.github.senyast4745.gamebank.repository.UserRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

@Component
public class StockExchangeDAO {

    private final ShareRepository shareRepository;

    private final CompanyRepository companyRepository;

    private final ChangingPriceRepository changingPriceRepository;

    private final RepositoryComponent repositoryComponent;

    static final int STOCK_PRICE_CHANGE = 300_000; //5 minutes

    private static final String formatDate = "HH:mm:ss";
    static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);

    private Random random = new Random();


    private final Logger LOGGER = LoggerFactory.getLogger(StockExchangeDAO.class.getSimpleName());

    public StockExchangeDAO(ShareRepository shareRepository,
                            CompanyRepository companyRepository, ChangingPriceRepository changingPriceRepository, RepositoryComponent repositoryComponent) {
        this.shareRepository = shareRepository;
        this.companyRepository = companyRepository;
        this.changingPriceRepository = changingPriceRepository;
        ChangingRandomPriceThread priceThread = new ChangingRandomPriceThread();
        priceThread.start();
        this.repositoryComponent = repositoryComponent;
    }

    public CompanyModel createCompany(String companyName, Double sharePrice) {
        return companyRepository.findByCompanyName(companyName).orElse(companyRepository.save(new CompanyModel(companyName, sharePrice)));
    }

    public CompanyModel changeSharePrice(Long companyId, Double changingPrice) {
        CompanyModel companyModel = companyRepository.findById(companyId).orElseThrow(() ->
                new UsernameNotFoundException("Company with ID " + companyId + "not found"));
        double tmpSharePrice = companyModel.getSharePrice() + changingPrice;
        companyModel.setSharePrice(tmpSharePrice);
        changingPriceRepository.save(new ChangingPriceModel(companyModel.getId(), tmpSharePrice,
                simpleDateFormat.format(new Date())));
        return companyRepository.save(companyModel);
    }

    public Iterable<ShareModel> buyShares(Long number, int count, String companyName) {
        TeamModel teamModel = repositoryComponent.getTeamByNumber(number);
        CompanyModel companyModel = companyRepository.findByCompanyName(companyName).orElseThrow(() ->
                new UsernameNotFoundException("Company with name " + companyName + "not found"));
        double fullPrice = count * companyModel.getSharePrice();
        if (fullPrice < teamModel.getScore()) {
            count = (int) (teamModel.getScore() / companyModel.getSharePrice());
            fullPrice = count * companyModel.getSharePrice();
        }
        teamModel.setScore(teamModel.getScore() - fullPrice);
        repositoryComponent.saveTeam(teamModel);
        shareRepository.save(new ShareModel(teamModel.getId(), companyModel.getId(), (long) count, companyModel));
        return shareRepository.findAllByUserId(teamModel.getId());
    }

    public Iterable<ShareModel> sellShares(Long number, long count, String companyName) {
        TeamModel teamModel = repositoryComponent.getTeamByNumber(number);
        CompanyModel companyModel = companyRepository.findByCompanyName(companyName).orElseThrow(() ->
                new UsernameNotFoundException("Company with name " + companyName + "not found"));
        ShareModel shareModel = shareRepository.findByCompanyIdAndUserId(companyModel.getId(), teamModel.getId()).orElseThrow(()
                -> new IllegalArgumentException("Not found shares"));
        if(count >= shareModel.getSharesNumbers()){
            shareRepository.deleteByCompanyIdAndUserId(companyModel.getId(), teamModel.getId());
        } else{
            shareModel.setSharesNumbers(shareModel.getSharesNumbers() - count);
            shareRepository.save(shareModel);
        }
        return shareRepository.findAllByUserId(teamModel.getId());
    }

    public Iterable<CompanyModel> getAllCompanies() throws NotFoundException {
        Iterable<CompanyModel> companyModels = companyRepository.findAll();
        if (!companyModels.iterator().hasNext()) {
            throw new NotFoundException("Companies not found");
        }
        return companyModels;
    }

    public void deleteCompany(String companyName) {
        CompanyModel companyModel =  companyRepository.findByCompanyName(companyName).orElseThrow(() ->
                new UsernameNotFoundException("Company with name " + companyName + "not found"));
        Long id = companyModel.getId();
        changingPriceRepository.deleteAllByCompanyId(id);
        companyRepository.deleteById(id);
        shareRepository.deleteAllByCompanyId(id);
    }

    public Iterable<ChangingPriceModel> getChangingPrise(String companyName) throws NotFoundException {
        CompanyModel companyModel = companyRepository.findByCompanyName(companyName).orElseThrow(() ->
                new UsernameNotFoundException("Company with name " + companyName + "not found"));
        return getChangingPrice(companyModel.getId());
    }

    private Iterable<ChangingPriceModel> getChangingPrice(Long companyId) throws NotFoundException {
        Iterable<ChangingPriceModel> changingPriceModels = changingPriceRepository.findAllByCompanyId(companyId);
        if (!changingPriceModels.iterator().hasNext()) {
            throw new NotFoundException("Changing price not found");
        }
        return changingPriceModels;
    }

    public void clearAll() {
        shareRepository.deleteAll();
        companyRepository.deleteAll();
    }

    class ChangingRandomPriceThread extends Thread {
        public void run() {
            try {
                while (true) {
                    Thread.sleep(STOCK_PRICE_CHANGE);
                    if (AdminsDAO.isGameStarted) {
                        Iterable<CompanyModel> companyModels = companyRepository.findAll();
                        companyModels.forEach((company) -> {
                            double lastPrice = company.getSharePrice();
                            boolean sign = random.nextBoolean();
                            if (sign) {
                                lastPrice += random.nextDouble();
                            } else {
                                lastPrice -= random.nextDouble();
                            }
                            String date = simpleDateFormat.format(new Date());
                            changingPriceRepository.save(new ChangingPriceModel(company.getId(), lastPrice, date));
                            company.setSharePrice(lastPrice);
                        });
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
