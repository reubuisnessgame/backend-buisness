package com.github.senyast4745.gamebank.form;

public class NewNewsForm {
    private Long companyId;

    private double changingPrice;

    private String heading;

    private String article;

    public NewNewsForm(Long companyId, double changingPrice, String heading, String article) {
        this.companyId = companyId;
        this.changingPrice = changingPrice;
        this.heading = heading;
        this.article = article;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public double getChangingPrice() {
        return changingPrice;
    }

    public String getHeading() {
        return heading;
    }

    public String getArticle() {
        return article;
    }
}
