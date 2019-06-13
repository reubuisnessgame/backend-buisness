package com.github.senyast4745.gamebank.controller;


import com.github.senyast4745.gamebank.dao.AdminsDAO;
import com.github.senyast4745.gamebank.dao.NewsDAO;
import com.github.senyast4745.gamebank.dao.StockExchangeDAO;
import com.github.senyast4745.gamebank.form.*;
import com.github.senyast4745.gamebank.model.ExceptionModel;
import javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/admin")
public class AdminsController {

    private final AdminsDAO adminsDAO;

    private final NewsDAO newsDAO;

    private final StockExchangeDAO stockExchangeDAO;

    public AdminsController(AdminsDAO adminsDAO, NewsDAO newsDAO, StockExchangeDAO stockExchangeDAO) {
        this.adminsDAO = adminsDAO;
        this.newsDAO = newsDAO;
        this.stockExchangeDAO = stockExchangeDAO;
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/create_adm", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity createNewAdmin(@RequestBody CreateAdminForm form) {
        try {
            return ResponseEntity.ok(adminsDAO.createNewAdmin(form.getUsername(), form.getPassword(), form.getRole(),
                    form.getMaxScore(), form.getCoefficient()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/admin/create_adm"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/create_adm"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/change_adm", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity changeAdmin(@RequestBody ChangeAdminForm form) {
        try {
            return ResponseEntity.ok(adminsDAO.changeAdmin(form.getLastUsername(), form.getNewUsername(), form.getNewPassword(),
                    form.getNewRole(), form.getNewMaxScore(), form.getNewCoefficient()));
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/admin/change_adm"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/change_adm"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('LEADING') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/me", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity aboutMe(@RequestHeader(value = "Authorization") String token) {
        try {
            return ResponseEntity.ok(adminsDAO.getMyInfo(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/admin/me"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/me"));
        }
    }


    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity startGame() {
        try {
            adminsDAO.startGame();
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/start"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity stopGame() {
        try {
            adminsDAO.closeGame();
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/stop"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('LEADING') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/create_team/{number}", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity createTeam(@PathVariable Long number) {
        try {
            return ResponseEntity.ok(adminsDAO.createNewTeam(number));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ExceptionModel(400, "Bad Request", e.getMessage(), "/admin/create_team/" + number));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/create_team/" + number));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getAllTeams() {
        try {
            return ResponseEntity.ok(new AllTeamsForm(adminsDAO.getAllTeams()));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionModel(404, "Not Found", e.getMessage(), "/admin/teams"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/teams"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getAllUsers() {
        try {
            AllUsersForm form = new AllUsersForm();
            form.setTeams(adminsDAO.getAllTeams());
            form.setAdmins(adminsDAO.getAllAdmins());
            return ResponseEntity.ok(form);
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionModel(404, "Not Found", e.getMessage(), "/admin/users"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/users"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('LEADING') or hasAuthority('EXCHANGE_WORKER')")
    @RequestMapping(value = "/team/{number}", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getTeam(@PathVariable Long number) {
        try {
            return ResponseEntity.ok(adminsDAO.getTeam(number));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionModel(404, "Not Found", e.getMessage(), "/admin/team/" + number));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/team/" + number));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/lock/{usr}", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity lockUser( @PathVariable(value = "usr") String username, @PathVariable boolean lock) {
        try {
            return ResponseEntity.ok(adminsDAO.lockUnlockUserByUsername(username, lock));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionModel(404, "Not Found", e.getMessage(), "/admin/lock/" + username));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/lock/" + username));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('LEADING')")
    @RequestMapping(value = "/add_scr", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity addScore(@RequestHeader(value = "Authorization") String token, AddScoreForm form) {
        try {
            return ResponseEntity.ok(adminsDAO.addScore(token, form.getRate(), form.getTeamNumber(), form.isWin()));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionModel(404, "Not Found", e.getMessage(), "/admin/add_scr"));
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/add_scr"));
        }
    }

    @PreAuthorize("hasAuthority('MODERATOR')")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity clearAll() {
        try {
            adminsDAO.clearAll();
            stockExchangeDAO.clearAll();
            newsDAO.clearAll();
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ResponseEntity.status(500).body(new ExceptionModel(500, "Internal Error", t.getMessage(), "/admin/clear"));
        }
    }


}
