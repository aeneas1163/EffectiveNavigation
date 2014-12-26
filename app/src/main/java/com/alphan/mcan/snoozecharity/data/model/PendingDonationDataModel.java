package com.alphan.mcan.snoozecharity.data.model;

/**
 * Created by mcni on 11/24/14.
 * TODO
 */
public class PendingDonationDataModel {

    private long id;
    private int charityIndex;
    private Double pendingAmount;

    public PendingDonationDataModel(int chaIndx, Double amount) {
        charityIndex = chaIndx;
        pendingAmount = amount;
        id = -1;
    }

    public void increasePendingAmount(Double incease){
        pendingAmount += incease;
    }

    public int getCharityIndex() {
        return charityIndex;
    }

    public void setCharityIndex(int charityIndex) {
        this.charityIndex = charityIndex;
    }

    public Double getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(Double pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
