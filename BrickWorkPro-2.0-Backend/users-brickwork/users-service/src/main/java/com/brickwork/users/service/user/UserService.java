package com.brickwork.users.service.user;

import com.brickwork.users.dto.CustomerRegistrationDTO;
import com.brickwork.users.dto.CustomerUpdateDTO;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.entity.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(UserDTO userDTO);
    UserDTO registerCustomer(CustomerRegistrationDTO customerDTO);
    UserDTO registerEmployee(EmployeeRegistrationDTO employeeDTO);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void registerNewAdmin(EmployeeRegistrationDTO request);

    UserDTO getUserByUsername(String username);

    UserDTO updateCustomerProfile(String email, CustomerUpdateDTO updateDTO);

}