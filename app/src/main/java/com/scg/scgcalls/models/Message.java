package com.scg.scgcalls.models;

import java.io.Serializable;

public class Message implements Serializable {


    public String phone;
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
    public Message(String message, String date, boolean isOutbound) {
        this.message = message;
        this.date = date;
        this.type = isOutbound ? "outbound" : "inbound";
    }
    public Message(){

    }
}