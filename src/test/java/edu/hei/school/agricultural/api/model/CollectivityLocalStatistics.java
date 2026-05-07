package edu.hei.school.agricultural.api.model;

import java.math.BigDecimal;

public class CollectivityLocalStatistics {

    public MemberDescription memberDescription;
    public BigDecimal earnedAmount;
    public BigDecimal unpaidAmount;
    public Double assiduityPercentage;

    @Override
    public String toString() {
        return "CollectivityLocalStatistics{" +
                "memberDescription=" + memberDescription +
                ", earnedAmount=" + earnedAmount +
                ", unpaidAmount=" + unpaidAmount +
                ", assiduityPercentage=" + assiduityPercentage +
                '}';
    }
}
