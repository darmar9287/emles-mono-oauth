package com.emles.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emles.dto.CustomerDTO;
import com.emles.exception.CustomerEmailAlreadyExistException;
import com.emles.exception.CustomerNotFoundException;
import com.emles.exception.CustomerPhoneAlreadyExistsException;
import com.emles.model.Customer;
import com.emles.repository.CustomerRepository;
import com.emles.utils.ValidationErrorMessages;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired
	private CustomerRepository customerRepository;

	private ModelMapper mapper = new ModelMapper();

	@Override
	public Customer findCustomerByEmail(String email) {
		return customerRepository.findByEmail(email);
	}

	@Override
	public Customer findCustomerByPhone(String phone) {
		return customerRepository.findByPhone(phone);
	}

	@Override
	@Transactional
	public Page<CustomerDTO> findCustomersBy(String findBy, String phrase, Pageable pageable) {
		Page<Customer> result = null;
		try {
			result = customerRepository.findCustomersBy(findBy, phrase, pageable);
		} catch (IllegalArgumentException e) {
			result = customerRepository.findAll(pageable);
		}
		List<CustomerDTO> content = result
				.getContent()
				.stream()
				.map(customer -> mapper.map(customer, CustomerDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, result.getTotalElements());
	}

	@Override
	@Transactional
	public CustomerDTO createCustomer(CustomerDTO customerDTO) {
		checkIfEmailExists(customerDTO.getEmail());
		checkIfPhoneNumberExists(customerDTO.getPhone());
		Customer customer = mapper.map(customerDTO, Customer.class);
		customer = customerRepository.save(customer);
		return mapper.map(customer, CustomerDTO.class);
	}

	private void checkIfEmailExists(String emailAddress) {
		Customer found = customerRepository.findByEmail(emailAddress);
		if (found != null) {
			throw new CustomerEmailAlreadyExistException(ValidationErrorMessages.CUSTOMER_EMAIL_EXISTS_MESSAGE);
		}
	}

	private void checkIfPhoneNumberExists(String phoneNumber) {
		Customer found = customerRepository.findByPhone(phoneNumber);
		if (found != null) {
			throw new CustomerPhoneAlreadyExistsException(ValidationErrorMessages.CUSTOMER_PHONE_EXISTS_MESSAGE);
		}
	}

	@Override
	@Transactional
	public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
		Optional<Customer> foundOpt = customerRepository.findById(customerDTO.getCustomerId());
		if (!foundOpt.isPresent()) {
			throw new CustomerNotFoundException(ValidationErrorMessages.CUSTOMER_NOT_FOUND_MESSAGE);
		}
		Customer found = foundOpt.get();
		if (!found.getEmail().equals(customerDTO.getEmail())) {
			checkIfEmailExists(customerDTO.getEmail());
		}
		if (!found.getPhone().equals(customerDTO.getPhone())) {
			checkIfPhoneNumberExists(customerDTO.getPhone());
		}
		found.setAddress(customerDTO.getAddress());
		found.setEmail(customerDTO.getEmail());
		found.setFirstName(customerDTO.getFirstName());
		found.setSecondName(customerDTO.getSecondName());
		found = customerRepository.save(found);
		return mapper.map(found, CustomerDTO.class);
	}

	@Override
	@Transactional
	public CustomerDTO findCustomerById(Long customerId) {
		Optional<Customer> found = customerRepository.findById(customerId);
		if (!found.isPresent()) {
			throw new CustomerNotFoundException(ValidationErrorMessages.CUSTOMER_NOT_FOUND_MESSAGE);
		}
		return mapper.map(found.get(), CustomerDTO.class);
	}

	@Override
	@Transactional
	public void deleteCustomer(Long customerId) {
		Optional<Customer> found = customerRepository.findById(customerId);
		if (!found.isPresent()) {
			throw new CustomerNotFoundException(ValidationErrorMessages.CUSTOMER_NOT_FOUND_MESSAGE);
		}
		customerRepository.delete(found.get());
	}
}
