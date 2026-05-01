package com.brickwork.users.entity;

import com.brickwork.users.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType; // INDIVIDUAL or BUSINESS

    // These are explicitly optional
    @Column(nullable = true)
    private String companyName;

    @Column(nullable = true)
    private String gstNumber;

    // Every customer (individual or business) needs a billing address
    @Column(nullable = false)
    private String billingAddress;

}