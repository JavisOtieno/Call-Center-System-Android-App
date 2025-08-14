package com.scg.scgcalls.models;

public class Email {

    public String emailto;
    public String emailfrom;
    public String subject;
    public String message;
    public String type;
    public String date;
    public String id;

    public boolean isOutbound() {
        if (type.equals("inbound")){
            return false;
        }
        return true;
    }
    public Email(String message, String date, boolean isOutbound) {
        this.message = message;
        this.date = date;
        this.type = isOutbound ? "outbound" : "inbound";
    }
    public Email(){

    }

}
