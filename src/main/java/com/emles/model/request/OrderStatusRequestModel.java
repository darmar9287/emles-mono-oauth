package com.emles.model.request;

import javax.validation.constraints.NotNull;

import com.emles.model.OrderStatus;
import com.emles.utils.ValidationErrorMessages;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatusRequestModel {

	@NotNull(message = ValidationErrorMessages.ORDER_ID_NOT_NULL_MESSAGE)
	private Long orderId;
	
	@NotNull(message = ValidationErrorMessages.ORDER_STATUS_NOT_BLANK_MESSAGE)
	private OrderStatus orderStatus;
}
