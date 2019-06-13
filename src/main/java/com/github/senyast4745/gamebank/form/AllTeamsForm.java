package com.github.senyast4745.gamebank.form;

import com.github.senyast4745.gamebank.model.TeamModel;

public class AllTeamsForm {
    private Iterable<TeamModel> teams;

    public AllTeamsForm() {
    }

    public AllTeamsForm(Iterable<TeamModel> teams) {
        this.teams = teams;
    }

    public Iterable<TeamModel> getTeams() {
        return teams;
    }

    public void setTeams(Iterable<TeamModel> teams) {
        this.teams = teams;
    }
}
