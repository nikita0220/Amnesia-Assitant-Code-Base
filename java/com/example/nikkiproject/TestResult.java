package com.example.nikkiproject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TestResult {

    @SerializedName("result")
    @Expose
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

//    public ArrayList<Integer> result;
//
//    public void setResult(ArrayList result) {
//        this.result = result;
//    }
//
//    public ArrayList getResult() {
//        return this.result;
//    }
}
