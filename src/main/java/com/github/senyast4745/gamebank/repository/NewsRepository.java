package com.github.senyast4745.gamebank.repository;

import com.github.senyast4745.gamebank.model.NewsModel;
import org.springframework.data.repository.CrudRepository;

public interface NewsRepository extends CrudRepository<NewsModel, Long> {
}
