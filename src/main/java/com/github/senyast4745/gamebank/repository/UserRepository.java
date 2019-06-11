package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.Role;
import com.github.senyast4745.gamebank.model.UserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {

    Optional<UserModel> findByTeamNumber(Long number);
    Optional<UserModel> findByUsername(String number);
    Iterable<UserModel> findAllByRoleOrderByScore(Role role);
}
