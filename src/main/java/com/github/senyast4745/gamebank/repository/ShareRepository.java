package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.ShareModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ShareRepository extends CrudRepository<ShareModel, Long> {

    Iterable<ShareModel> findAllByUserId(Long userId);
}
