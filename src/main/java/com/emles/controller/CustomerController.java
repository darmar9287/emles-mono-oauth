package com.emles.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emles.dto.CustomerDTO;
import com.emles.exception.ValidationException;
import com.emles.model.request.CustomerRequestModel;
import com.emles.model.response.CustomerResponseModel;
import com.emles.service.CustomerService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

	private CustomerService customerService;
	
	public CustomerController(CustomerService customerService) {
		super();
		this.customerService = customerService;
	}

	@Value("${config.pagination.default_page_offset}")
	private Integer PER_PAGE;

	private ModelMapper mapper = new ModelMapper();
	
	@ApiOperation("Create customer endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PostMapping(value = "/create")
	public ResponseEntity<CustomerResponseModel> createCustomer(@Valid @RequestBody CustomerRequestModel model, @ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		
		CustomerDTO customerDTO = mapper.map(model, CustomerDTO.class);
		CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
		CustomerResponseModel response = mapper.map(createdCustomer, CustomerResponseModel.class);
		return ResponseEntity.ok(response);
	}
	
	@ApiOperation("Update customer endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PutMapping(value = "/update")
	public ResponseEntity<CustomerResponseModel> updateCustomerStatus(@Valid @RequestBody CustomerRequestModel model,
			@ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		CustomerDTO customerDTO = mapper.map(model, CustomerDTO.class);
		CustomerDTO updatedCustomerDTO = customerService.updateCustomer(customerDTO);
		CustomerResponseModel response = mapper.map(updatedCustomerDTO, CustomerResponseModel.class);
		return ResponseEntity.ok(response);
	}
	
	@ApiOperation("Get single customer endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = "/show/{customerId}")
	public ResponseEntity<CustomerResponseModel> showCustomer(@PathVariable("customerId") Long customerId) {
		CustomerDTO customerDTO = customerService.findCustomerById(customerId);
		CustomerResponseModel response = mapper.map(customerDTO, CustomerResponseModel.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Delete customer endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@DeleteMapping(value = "/delete/{customerId}")
	public ResponseEntity<?> deleteCustomer(@PathVariable("customerId") Long customerId) {
		customerService.deleteCustomer(customerId);
		return ResponseEntity.noContent().build();
	}
	
	@ApiOperation("Get paginated list of customers endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = { "/list/{page}", "/list" })
	public Page<CustomerResponseModel> showCustomers(@PathVariable(name = "page", required = false) Integer page,
			@RequestParam(name = "search", required = false, defaultValue = "") String search,
			@RequestParam(name = "searchBy", required = false, defaultValue = "name") String searchBy,
			@RequestParam(name = "perPage", required = false) String perPage) {
		if (page == null) {
			page = 0;
		}
		Integer numberOfElementsPerPage = 0;
		try {
			numberOfElementsPerPage = Integer.parseInt(perPage);
		} catch (RuntimeException e) {
			numberOfElementsPerPage = PER_PAGE;
		}
		Pageable pageable = PageRequest.of(page, numberOfElementsPerPage);
		Page<CustomerDTO> pages = customerService.findCustomersBy(searchBy, search, pageable);
		List<CustomerResponseModel> content = pages.getContent()
				.stream()
				.map(customerDTO -> mapper.map(customerDTO, CustomerResponseModel.class))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, pages.getTotalElements());
	}
}
