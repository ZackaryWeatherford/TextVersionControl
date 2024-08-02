package com.example.textversioncontrol.models;

public class EditData {

    /** */
    private final int id;

    /** */
    private final String date;

    /** */
    public EditData(int id, String date){
        this.id = id;
        this.date = date;
    }

    /** */
    public String getDate(){ return date;}

    /** */
    public int getId() {return id;}

}
