package com.example.nikkiproject;

public class Person {
    private String id;
    private String name;
    private String relationship;

    public Person(String id, String name, String relationship) {
        this.name = name;
        this.id = id;
        this.relationship = relationship;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
