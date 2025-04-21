package com.example.tutorapp2.network;

public class ApiClient {
    public static final String BASE_URL = "http://8.138.229.36:3000";


    public static final String REGISTER_STUDENT = BASE_URL + "/api/users/register/student";
    public static final String REGISTER_PARENT = BASE_URL + "/api/users/register/parent";
    public static final String LOGIN = BASE_URL + "/api/auth/login";
    public static final String GET_TUTORS = BASE_URL + "/api/tutors";
    public static final String GET_FIND_TUTORS = BASE_URL + "/api/find-tutors";
}
