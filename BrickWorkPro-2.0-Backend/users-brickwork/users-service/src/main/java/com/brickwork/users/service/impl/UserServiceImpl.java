package com.brickwork.users.service.impl;

import com.brickwork.users.dto.CustomerRegistrationDTO;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.entity.Customer;
import com.brickwork.users.entity.Employee;
import com.brickwork.users.entity.User;
import com.brickwork.users.enums.Role;
import com.brickwork.users.repository.UserRepository;
import com.brickwork.users.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- REUSABLE HELPER METHOD ---
    private void populateBaseUserFields(User targetEntity, UserDTO sourceDto) {
        targetEntity.setUsername(sourceDto.getUsername());
        targetEntity.setEmail(sourceDto.getEmail());
        targetEntity.setPassword(passwordEncoder.encode(sourceDto.getPassword()));
        targetEntity.setFullName(sourceDto.getFullName());
        targetEntity.setPhoneNumber(sourceDto.getPhoneNumber());
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        User user = new User();

        populateBaseUserFields(user, userDTO);
        user.setRole(userDTO.getRole());

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDTO registerCustomer(CustomerRegistrationDTO customerDTO) {
        Customer customer = new Customer();

        populateBaseUserFields(customer, customerDTO);
        customer.setRole(com.brickwork.users.enums.Role.CUSTOMER);

        customer.setCustomerType(customerDTO.getCustomerType());
        customer.setCompanyName(customerDTO.getCompanyName());
        customer.setGstNumber(customerDTO.getGstNumber());
        customer.setBillingAddress(customerDTO.getBillingAddress());

        Customer savedCustomer = userRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    @Override
    public UserDTO registerEmployee(EmployeeRegistrationDTO employeeDTO) {
        Employee employee = new Employee();

        populateBaseUserFields(employee, employeeDTO);
        employee.setRole(Role.STAFF); // Or STAFF

        employee.setEmployeeCode(employeeDTO.getEmployeeCode());
        employee.setShiftTiming(employeeDTO.getShiftTiming());

        Employee savedEmployee = userRepository.save(employee);
        return mapToDTO(savedEmployee);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void registerNewAdmin(EmployeeRegistrationDTO request) {

        // 1. Validation: Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        // 2. Map DTO to User Entity
        User newAdmin = new User();
        newAdmin.setUsername(request.getUsername());

        // 3. Security: ALWAYS hash the password before saving!
        newAdmin.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Force the Role to ADMIN (Ignore whatever the user sent in the request)
        newAdmin.setRole(Role.ADMIN);

        // 5. Map the remaining fields based on your DB schema/DTO
        newAdmin.setEmail(request.getEmail());
        newAdmin.setFullName(request.getFullName());
        newAdmin.setPhoneNumber(request.getPhoneNumber());

        // Note: If your User entity doesn't auto-generate UUIDs, uncomment the next line:
        // newAdmin.setId(java.util.UUID.randomUUID().toString());

        // 6. Save to Database
        userRepository.save(newAdmin);
    }


    // Helper method to map Entity to DTO
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());

        // Example of mapping specific fields if returning a generic DTO for a subclass
        // if (user instanceof Customer) { ... }

        return dto;
    }
}