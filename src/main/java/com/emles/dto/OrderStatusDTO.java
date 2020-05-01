package com.emles.dto;

import com.emles.model.OrderStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatusDTO {

	private Long orderId;
	private OrderStatus orderStatus;
}
