package com.github.senyast4745.gamebank.controller;

import com.github.senyast4745.gamebank.dao.NewsDAO;
import com.github.senyast4745.gamebank.dao.StockExchangeDAO;
import com.github.senyast4745.gamebank.form.NewNewsForm;
import com.github.senyast4745.gamebank.model.ExceptionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/stock")
public class StockExchangeController {
    private final StockExchangeDAO stockExchangeDAO;

    private final NewsDAO newsDAO;

    public StockExchangeController(StockExchangeDAO stockExchangeDAO, NewsDAO newsDAO) {
        this.stockExchangeDAO = stockExchangeDAO;
        this.newsDAO = newsDAO;
    }


    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createCompany(@RequestParam(value = "name") String companyName, @RequestParam(value = "price") double sharePrice) {
        try {
            return ResponseEntity.ok(stockExchangeDAO.createCompany(companyName, sharePrice));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/create"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public ResponseEntity changeSharePrice(@RequestBody NewNewsForm form) {
        try {
            stockExchangeDAO.changeSharePrice(form.getCompanyId(), form.getChangingPrice());
            return ResponseEntity.ok(newsDAO.createNews(form.getHeading(), form.getArticle()));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/change"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/change"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/buy/{number}", method = RequestMethod.POST)
    public ResponseEntity buyShares(@PathVariable Long number, @RequestParam int count, @RequestParam String companyName) {
        try {
            return ResponseEntity.ok(stockExchangeDAO.buyShares(number, count, companyName));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/buy" + number));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/buy/" + number));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/sell/{number}", method = RequestMethod.POST)
    public ResponseEntity sellShares(@PathVariable Long number, @RequestParam int count, @RequestParam String companyName) {
        try {
            return ResponseEntity.ok(stockExchangeDAO.sellShares(number, count, companyName));
        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/sell/" + number));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/sell/" + number));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER') or hasAuthority('TEAM')")
    @RequestMapping(value = "/changes/{companyName}", method = RequestMethod.GET)
    public ResponseEntity sellShares(@PathVariable String companyName) {
        try {
            return ResponseEntity.ok(stockExchangeDAO.getChangingPrise(companyName));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/changes/" + companyName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/changes/" + companyName));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER') or hasAuthority('TEAM')")
    @RequestMapping(value = "/companies}", method = RequestMethod.GET)
    public ResponseEntity getAllCompanies() {
        try {
            return ResponseEntity.ok(stockExchangeDAO.getAllCompanies());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/companies"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/companies"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/delete/{companyName}", method = RequestMethod.POST)
    public ResponseEntity deleteCompany(@PathVariable String companyName) {
        try {
            stockExchangeDAO.deleteCompany(companyName);
            return ResponseEntity.ok().build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/stock/changes/" + companyName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", e.getMessage(), "/stock/changes/" + companyName));
        }
    }
}
