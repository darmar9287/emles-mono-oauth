package com.emles.model.response;

import java.util.Date;
import java.util.List;

import com.emles.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderResponseModel {

	private long orderId;
	private Date orderDate;
	private OrderStatus status;
	private UserDataResponse createdBy;
	private CustomerResponseModel customer;
	private List<OrderDetailsResponseModel> orderDetails;
}
