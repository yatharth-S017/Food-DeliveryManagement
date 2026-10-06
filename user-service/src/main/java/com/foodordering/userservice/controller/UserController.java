package com.foodordering.userservice.controller;

import com.foodordering.userservice.dto.AuthRequest;
import com.foodordering.userservice.dto.AuthResponse;
import com.foodordering.userservice.dto.RegisterRequest;
import com.foodordering.userservice.dto.UserResponse;
import com.foodordering.userservice.model.Customer;
import com.foodordering.userservice.model.Role;
import com.foodordering.userservice.repository.CustomerRepository;
import com.foodordering.userservice.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public UserController(CustomerRepository customerRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setPhone(request.getPhone());
        customer.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtils.generateToken(customer.getEmail(), customer.getRole().name(), customer.getCustomerId());

        return ResponseEntity.ok(new AuthResponse(
                token,
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getRole().name()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(customer -> ResponseEntity.ok(toResponse(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody RegisterRequest request) {
        return customerRepository.findById(id)
                .map(customer -> {
                    if (request.getName() != null) customer.setName(request.getName());
                    if (request.getPhone() != null) customer.setPhone(request.getPhone());
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        customer.setPassword(passwordEncoder.encode(request.getPassword()));
                    }
                    Customer updated = customerRepository.save(customer);
                    return ResponseEntity.ok(toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerRepository.deleteById(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    private UserResponse toResponse(Customer customer) {
        return new UserResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getRole()
        );
    }
}
