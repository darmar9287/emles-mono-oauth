package com.emles.dto;

import java.util.Date;
import java.util.List;

import com.emles.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

	private long orderId;
	private Date orderDate;
	private OrderStatus status;
	private UserDTO createdBy;
	private CustomerDTO customer;
	private List<OrderDetailsDTO> orderDetails;
}
