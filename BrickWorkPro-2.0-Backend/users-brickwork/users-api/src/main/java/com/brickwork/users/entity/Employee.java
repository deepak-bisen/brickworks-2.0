package com.brickwork.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EMPLOYEES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends User {

    @Column(name = "EMPLOYEE_CODE",columnDefinition = "VARCHAR(25)", unique = true,  nullable = true)
    private String employeeCode;

    @Column(name = "SHIFT_TIMING", columnDefinition = "VARCHAR(25)", nullable = true)
    private String shiftTiming; // e.g., "MORNING", "NIGHT"

}