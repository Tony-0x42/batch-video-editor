package com.example.cj.videoeditor.bean;

public class CustomerService {
    public String phone;
    public String serviceTime;
    public String onlineUrl;

    public CustomerService(String phone, String serviceTime, String onlineUrl) {
        this.phone = phone;
        this.serviceTime = serviceTime;
        this.onlineUrl = onlineUrl;
    }
}