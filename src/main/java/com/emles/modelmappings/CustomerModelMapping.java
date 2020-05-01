package com.emles.modelmappings;

import com.emles.dto.CustomerDTO;
import com.emles.model.Customer;
import com.emles.model.request.CustomerRequestModel;
import com.emles.model.response.CustomerResponseModel;

public class CustomerModelMapping {
	
	private CustomerModelMapping() {}
	
	public static CustomerDTO of(Customer customer) {
		CustomerDTO customerDTO = new CustomerDTO();
		customerDTO.setCustomerId(customer.getId());
		customerDTO.setAddress(customer.getAddress());
		customerDTO.setCustomerId(customer.getId());
		customerDTO.setEmail(customer.getEmail());
		customerDTO.setFirstName(customer.getFirstName());
		customerDTO.setSecondName(customer.getSecondName());
		customerDTO.setPhone(customer.getPhone());
		return customerDTO;
	}
	
	public static Customer of(CustomerDTO customerDTO) {
		Customer customer = new Customer();
		customer.setId(customerDTO.getCustomerId());
		customer.setAddress(customerDTO.getAddress());
		customer.setEmail(customerDTO.getEmail());
		customer.setFirstName(customerDTO.getFirstName());
		customer.setSecondName(customerDTO.getSecondName());
		customer.setPhone(customerDTO.getPhone());
		return customer;
	}
	
	public static CustomerResponseModel res(CustomerDTO customerDTO) {
		CustomerResponseModel response = new CustomerResponseModel();
		response.setCustomerId(customerDTO.getCustomerId());
		response.setAddress(customerDTO.getAddress());
		response.setCustomerId(customerDTO.getCustomerId());
		response.setEmail(customerDTO.getEmail());
		response.setFirstName(customerDTO.getFirstName());
		response.setSecondName(customerDTO.getSecondName());
		response.setPhone(customerDTO.getPhone());
		return response;
	}
	
	public static CustomerDTO req(CustomerRequestModel request) {
		CustomerDTO customerDTO = new CustomerDTO();
		customerDTO.setAddress(request.getAddress());
		customerDTO.setEmail(request.getEmail());
		customerDTO.setFirstName(request.getFirstName());
		customerDTO.setSecondName(request.getSecondName());
		customerDTO.setPhone(request.getPhone());
		return customerDTO;
	}
}
