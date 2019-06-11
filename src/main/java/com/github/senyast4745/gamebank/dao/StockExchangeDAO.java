package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.CompanyModel;
import com.github.senyast4745.gamebank.repository.CompanyRepository;
import com.github.senyast4745.gamebank.repository.ShareRepository;
import com.github.senyast4745.gamebank.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockExchangeDAO {

    private final UserRepository userRepository;

    private final ShareRepository shareRepository;

    private final CompanyRepository companyRepository;

    public StockExchangeDAO(UserRepository userRepository, ShareRepository shareRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.companyRepository = companyRepository;
    }

    public CompanyModel createCompany(String companyName, Double sharePrice) {
        return companyRepository.findByCompanyName(companyName).orElse(companyRepository.save(new CompanyModel(companyName, sharePrice)));
    }

    public Iterable<CompanyModel> getAllCompany() throws NotFoundException {
        Iterable<CompanyModel> companyModels = companyRepository.findAll();
        if(!companyModels.iterator().hasNext()){
            throw new NotFoundException("Companies not found");
        }
        return companyModels;
    }


}
