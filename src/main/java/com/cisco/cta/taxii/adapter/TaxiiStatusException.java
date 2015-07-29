package com.cisco.cta.taxii.adapter;

public class TaxiiStatusException extends Exception {
    private String statusType;
    private String statusMessage;

    public TaxiiStatusException(String statusType, String statusMessage) {
        super("Taxii status type: " + statusType + ", status message: " + statusMessage);
        this.statusType = statusType;
        this.statusMessage = statusMessage;
    }

    public String getStatusType() {
        return statusType;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
