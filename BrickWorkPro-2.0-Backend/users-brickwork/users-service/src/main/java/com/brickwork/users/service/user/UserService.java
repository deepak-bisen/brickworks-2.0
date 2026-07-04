package com.brickwork.users.service.user;

import com.brickwork.users.dto.*;
import com.brickwork.users.entity.User;

import java.util.Optional;

public interface UserService {
    JwtResponseDTO authenticateUser(LoginRequestDTO loginRequest);
    UserDTO registerUser(UserDTO userDTO);
    UserDTO registerCustomer(CustomerRegistrationDTO customerDTO);
    UserDTO registerEmployee(EmployeeRegistrationDTO employeeDTO);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void registerNewAdmin(EmployeeRegistrationDTO request);
    UserDTO getUserByUsername(String username);
    UserDTO updateCustomerProfile(String email, CustomerUpdateDTO updateDTO);
    void forgotPassword(String email);
    void verifyOtp(VerifyOtpRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
}