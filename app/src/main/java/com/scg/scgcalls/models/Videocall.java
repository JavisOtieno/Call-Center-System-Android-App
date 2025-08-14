package com.scg.scgcalls.models;

import java.io.Serializable;

public class Videocall implements Serializable {

    public String duration;
    public String room;
    public String status;
    public String date;
    public String id;


    public Videocall(String room, String date, String status) {
        this.room = room;
        this.date = date;
        this.status = status;
    }
    public Videocall(){

    }


}
