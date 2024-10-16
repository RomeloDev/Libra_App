package com.example.libraapp;

public class dataModel {
    private String id, name, courseYr, department;

    public dataModel(){

    }

    public dataModel(String studentId, String name, String courseYr, String department){
        this.id = studentId;
        this.name = name;
        this.courseYr = courseYr;
        this.department = department;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getCourseYr(){
        return courseYr;
    }

    public String getDepartment(){
        return department;
    }
}
