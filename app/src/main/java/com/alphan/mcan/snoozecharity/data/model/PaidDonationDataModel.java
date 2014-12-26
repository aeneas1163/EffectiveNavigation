package com.alphan.mcan.snoozecharity.data.model;

/**
 * Created by Alphan on 26-Dec-14.
 */
public class PaidDonationDataModel {

    private long id;
    private int charityIndex;
    private Double paidAmount;
    private String paymentDate;

    public PaidDonationDataModel(int chaIndx, Double amount, String payDate) {
        charityIndex = chaIndx;
        paidAmount = amount;
        paymentDate = payDate;
        id = -1;
    }

    public PaidDonationDataModel(PendingDonationDataModel pendingModel, String payDate) {
        charityIndex = pendingModel.getCharityIndex();
        paidAmount = pendingModel.getPendingAmount();
        paymentDate = payDate;
        id = -1;
    }

    public int getCharityIndex() {
        return charityIndex;
    }

    public void setCharityIndex(int charityIndex) {
        this.charityIndex = charityIndex;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
