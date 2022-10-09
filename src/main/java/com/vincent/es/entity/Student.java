package com.vincent.es.entity;

import java.util.Date;
import java.util.List;

public class Student {
    private String id;                // 學生編號，可當作 document id
    private String name;              // 姓名
    private List<String> departments; // 科系
    private List<Course> courses;     // 修習課程
    private int grade;                // 年級
    private int conductScore;         // 操行成績
    private Job job;                  // 職務
    private String introduction;      // 自我介紹
    private Date englishIssuedDate;   // 英文檢定通過日

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getConductScore() {
        return conductScore;
    }

    public void setConductScore(int conductScore) {
        this.conductScore = conductScore;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Date getEnglishIssuedDate() {
        return englishIssuedDate;
    }

    public void setEnglishIssuedDate(Date englishIssuedDate) {
        this.englishIssuedDate = englishIssuedDate;
    }
}
