package com.brickwork.users.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeRegistrationDTO extends UserDTO {

    private String employeeCode;
    private String shiftTiming;
}