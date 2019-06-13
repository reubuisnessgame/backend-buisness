package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.Role;
import com.github.senyast4745.gamebank.model.TeamModel;
import com.github.senyast4745.gamebank.model.UserModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TeamsRepository extends CrudRepository<TeamModel, Long> {

    Optional<TeamModel> findByTeamNumber(Long number);
}
