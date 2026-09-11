package org.example.customerservice.customer.service;

import org.example.customerservice.customer.model.CustomerResponse;
import org.example.customerservice.customer.model.*;
import org.example.customerservice.customer.repository.CustomerRepository;
import org.example.customerservice.utils.encoder.Encoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

            List<Customer> list =
                    customerRepository.getCustomerByEmail(request.email());

            //Ändringen från tomt objekt kan möjligen inte fungera ihop med mastern på hotelbooking.

            if(!list.isEmpty()){
                throw new ResponseStatusException(
                        CONFLICT,
                        "Epostadressen är redan registrerad"
                );
            }

            Customer customer = new Customer();

            customer.setFirstName(request.firstName());
            customer.setLastName(request.lastName());
            customer.setEmail(request.email());
            customer.setPhoneNumber(request.phoneNumber());
            customer.setPassword(Encoder.hashPassword(request.password()));

            return toResponse(customerRepository.save(customer));

    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Kunden finns inte"));

        return toResponse(customer);
    }

    public CustomerResponse loginCustomer(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email());

        if (customer == null) {
            return null;
        }

        boolean correctPassword = Encoder.checkPassword(request.password(), customer.getPassword());

        if (!correctPassword) {
            return null;
        }

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber()
        );
    }

    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Kunden finns inte"));

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhoneNumber(request.phoneNumber());

        if (request.changePassword()) {
            customer.setPassword(Encoder.hashPassword(request.newPassword()));
        }

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    public boolean checkPassword(CheckPasswordRequest passwordRequest) {
        if (passwordRequest.password() == null || passwordRequest.password().isBlank()) {
            return false;
        }
        if (passwordRequest.newPassword() == null || passwordRequest.newPassword().isBlank()) {
            return false;
        }

        Customer customer = customerRepository.findByEmail(passwordRequest.email());

        return Encoder.checkPassword(passwordRequest.password(), customer.getPassword());
    }

    public void removeUser(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Kunden finns inte"));
        customerRepository.delete(customer);
    }
}