package com.emles.model.request;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.emles.utils.ValidationErrorMessages;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderRequestModel {
	
	@NotNull(message = ValidationErrorMessages.CUSTOMER_ID_NOT_EMPTY_MESSAGE)
	private Long customerId;

	@NotNull(message = ValidationErrorMessages.ORDER_MUST_HAVE_DETAILS_MESSAGE)
	private Set<@Valid OrderDetailsRequestModel> orderDetailsRequests = new HashSet<>();
}
