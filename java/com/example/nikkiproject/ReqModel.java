package com.example.nikkiproject;

import java.util.ArrayList;

public class ReqModel {
    private String title;
    private String main;
    private ArrayList<String> result;
    private String personId;

    public ReqModel(String title, String main) {
        this.title = title;
        this.main = main;
    }

    public ReqModel (String title, String main, String personId) {
        this.title = title;
        this.main = main;
        this.personId = personId;
    }

    public String getMain() {
        return main;
    }

    public ArrayList<String> getResult() {
        return this.result;
    }

    public void setResult(ArrayList<String> result) {
        this.result = result;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }
}
