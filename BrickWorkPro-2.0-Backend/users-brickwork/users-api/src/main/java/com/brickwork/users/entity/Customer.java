package com.brickwork.users.entity;

import com.brickwork.users.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CUSTOMERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends User {

    @Column(name = "CUSTOMER_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType; // INDIVIDUAL or BUSINESS

    // These are explicitly optional
    @Column(name = "COMPANY_NAME", columnDefinition = "VARCHAR(25)", nullable = true)
    private String companyName;

    @Column(name = "GST_NUMBER", columnDefinition = "VARCHAR(25)", nullable = true)
    private String gstNumber;

    // Every customer (individual or business) needs a billing address
    @Column(name = "BILLING_ADDRESS", columnDefinition = "VARCHAR(500)", nullable = false)
    private String billingAddress;

}