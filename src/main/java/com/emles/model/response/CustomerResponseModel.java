package com.emles.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CustomerResponseModel {
	private long customerId;
	private String firstName;
	private String secondName;
	private String email;
	private String phone;
	private String address;
}
