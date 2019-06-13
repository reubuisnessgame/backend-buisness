package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.AdminModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminRepository extends CrudRepository<AdminModel, Long> {

    Optional<AdminModel> findAllByUsername(String username);
}
