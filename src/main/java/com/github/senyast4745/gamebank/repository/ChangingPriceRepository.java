package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.ChangingPriceModel;
import org.springframework.data.repository.CrudRepository;

public interface ChangingPriceRepository extends CrudRepository<ChangingPriceModel, Long> {
    Iterable<ChangingPriceModel> findAllByCompanyId(Long companyId);
    void deleteAllByCompanyId(Long companyId);
}
