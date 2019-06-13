package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.NewsModel;
import com.github.senyast4745.gamebank.repository.NewsRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NewsDAO {

    private final NewsRepository newsRepository;

    public NewsDAO(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public NewsModel createNews(String heading, String article) {
        return newsRepository.save(new NewsModel(heading, article, StockExchangeDAO.simpleDateFormat.format(new Date())));
    }

    public Iterable<NewsModel> getAllNews() throws NotFoundException {
        Iterable<NewsModel> newsModels = newsRepository.findAll();
        if (!newsModels.iterator().hasNext()) {
            throw new NotFoundException("News not found");
        }
        return newsModels;
    }

    public void clearAll() {
        newsRepository.deleteAll();
    }

}
