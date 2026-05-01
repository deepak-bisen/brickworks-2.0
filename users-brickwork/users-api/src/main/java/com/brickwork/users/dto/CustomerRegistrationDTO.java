package com.brickwork.users.dto;

import com.brickwork.users.enums.CustomerType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerRegistrationDTO extends UserDTO {

    private CustomerType customerType; // INDIVIDUAL or BUSINESS
    private String companyName;
    private String gstNumber;
    private String billingAddress;
}