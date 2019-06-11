package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.CompanyModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CompanyRepository extends CrudRepository<CompanyModel, Long> {
    Optional<CompanyModel> findByCompanyName(String companyName);
}
