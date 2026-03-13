package com.mgd.employee_mgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class EmployeeManagementApp {
    public static void main(String[] args) {
        System.out.println("Welcome to the Employee Management System by Mel!");

        SpringApplication.run(EmployeeManagementApp.class, args);

    }
}
