package com.github.senyast4745.gamebank.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;

import static com.github.senyast4745.gamebank.model.Role.LEADING;

@Entity
@Table(name = "teams")
public class UserModel implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "user_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "password")
    private String password;

    @Column(name = "team_number", unique = true)
    private Long teamNumber;

    @Column(name = "score")
    private Double score;

    @Column(name = "full_score")
    private Double fullScore;

    @Column(name = "credit")
    private Double credit;

    @Column(name = "deposit")
    private Double deposit;

    @Column(name = "credit_time")
    private Long creditTime;

    @Column(name = "deposit_time")
    private Long depositTime;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "coefficient")
    private Double coefficient;

    @Column(name = "non_locked")
    private boolean nonLocked;

    public UserModel() {
    }

    public UserModel(String username, Long teamNumber, Double score, Double credit, Double deposit, Long creditTime, Long depositTime, Double fullScore) {
        this.teamNumber = teamNumber;
        this.score = score;
        this.fullScore = fullScore;
        this.credit = credit;
        this.deposit = deposit;
        this.creditTime = creditTime;
        this.role = Role.TEAM;
        this.depositTime = depositTime;
        password = null;
        this.username = username;
        nonLocked = false;
        maxScore = null;
        coefficient = null;
    }

    public UserModel(String username, String password, String role, Double maxScore, Double coefficient) {
        this.username = username;
        this.password = password;
        this.credit = null;
        this.deposit = null;
        this.creditTime = null;
        this.depositTime = null;
        this.score = null;
        this.fullScore = null;
        this.maxScore = maxScore;
        this.coefficient = coefficient;
        switch (role) {
            case "LEADING": {
                this.role = LEADING;
                break;
            }
            case "MODERATOR": {
                this.role = Role.MODERATOR;
                break;
            }
            default:
                throw new IllegalArgumentException("Incorrect role");
        }
        nonLocked = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(Long teamNumber) {
        this.teamNumber = teamNumber;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Double getDeposit() {
        return deposit;
    }

    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }

    public Long getCreditTime() {
        return creditTime;
    }

    public void setCreditTime(Long creditTime) {
        this.creditTime = creditTime;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDepositTime() {
        return depositTime;
    }

    public void setDepositTime(Long depositTime) {
        this.depositTime = depositTime;
    }

    public void setNonLocked(boolean nonLocked) {
        this.nonLocked = nonLocked;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Double coefficient) {
        this.coefficient = coefficient;
    }

    public boolean isNonLocked() {
        return nonLocked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return nonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Double getFullScore() {
        return fullScore;
    }

    public void setFullScore(Double fullScore) {
        this.fullScore = fullScore;
    }
}
