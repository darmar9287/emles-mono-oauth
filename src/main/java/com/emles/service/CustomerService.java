package com.emles.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emles.dto.CustomerDTO;
import com.emles.model.Customer;

public interface CustomerService {
	CustomerDTO findCustomerById(Long customerId);
	Customer findCustomerByEmail(String email);
	Customer findCustomerByPhone(String phone);
	Page<CustomerDTO> findCustomersBy(String findBy, String phrase, Pageable pageable);
	CustomerDTO updateCustomer(CustomerDTO customerDTO);
	void deleteCustomer(Long customerId);
	CustomerDTO createCustomer(CustomerDTO customerDTO);
}
