package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class ExtendedAdDto {

    private int id;
    private String authorFirstName;
    private String authorLastName;
    private String title;
    private String description;
    private String email;
    private String image;
    private String phone;
    private int price;

}