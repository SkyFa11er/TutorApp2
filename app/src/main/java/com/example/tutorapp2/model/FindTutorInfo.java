package com.example.tutorapp2.model;
import java.io.Serializable;
import java.util.List;
public class FindTutorInfo implements Serializable{
    private int id;
    private String childName;
    private String phone;
    private String district;
    private String address;
    private String subjects;
    private int salary;
    private String days;
    private String note;

    public FindTutorInfo(int id, String childName, String phone, String district, String address,
                         String subjects, int salary, String days, String note) {
        this.id = id;
        this.childName = childName;
        this.phone = phone;
        this.district = district;
        this.address = address;
        this.subjects = subjects;
        this.salary = salary;
        this.days = days;
        this.note = note;
    }
    public FindTutorInfo(int id, String childName, String subjects, int salary, String days, String note, String phone, String district, String address) {
        this.id = id;
        this.childName = childName;
        this.subjects = subjects;
        this.salary = salary;
        this.days = days;
        this.note = note;
        this.phone = phone;
        this.district = district;
        this.address = address;
    }
    public FindTutorInfo(int id, String childName, String subjects, int salary, String days, String note) {
        this.id = id;
        this.childName = childName;
        this.subjects = subjects;
        this.salary = salary;
        this.days = days;
        this.note = note;
        this.phone = "";
        this.district = "";
        this.address = "";
    }

    public int getId() {
        return id;
    }

    public String getChildName() {
        return childName;
    }

    public String getPhone() {
        return phone;
    }

    public String getDistrict() {
        return district;
    }

    public String getAddress() {
        return address;
    }

    public String getSubjects() {
        return subjects;
    }

    public int getSalary() {
        return salary;
    }

    public String getDays() {
        return days;
    }

    public String getNote() {
        return note;
    }
}
