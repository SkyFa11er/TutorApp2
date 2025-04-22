
package com.example.tutorapp2.model;
import java.io.Serializable;
import java.util.List;

public class TutorInfo implements Serializable{
    private int id;
    private String name;
    private List<String> subjects;
    private String salary;
    private String salaryNote;
    private String intro;
    private List<String> availableDays;
    private String startTime;
    private String endTime;
    private int userId;



    public TutorInfo(int id, int userId, String name, List<String> subjects, String salary, String salaryNote,
                     String intro, List<String> availableDays, String startTime, String endTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.subjects = subjects;
        this.salary = salary;
        this.salaryNote = salaryNote;
        this.intro = intro;
        this.availableDays = availableDays;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public TutorInfo(int id, String name, List<String> subjects, String salary, String salaryNote,
                     String intro, List<String> availableDays, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.subjects = subjects;
        this.salary = salary;
        this.salaryNote = salaryNote;
        this.intro = intro;
        this.availableDays = availableDays;
        this.startTime = startTime;
        this.endTime = endTime;
    }





    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public String getSalary() {
        return salary;
    }

    public String getSalaryNote() {
        return salaryNote;
    }

    public String getIntro() {
        return intro;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getUserId() { return userId; }


}
