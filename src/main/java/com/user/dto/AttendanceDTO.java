package com.user.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceDTO {

    private Long id;
    private LocalDate date;
    private Double present;

}
