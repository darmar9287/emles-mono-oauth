package com.emles.modelmappings;

import java.util.stream.Collectors;

import com.emles.dto.OrderDTO;
import com.emles.model.Order;
import com.emles.model.response.OrderResponseModel;

public class OrderModelMapping {
	private OrderModelMapping() {
	}

	public static OrderDTO of(Order order) {
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderId(order.getId());
		orderDTO.setCreatedBy(UserModelMapping.of(order.getCreatedBy()));
		orderDTO.setCustomer(CustomerModelMapping.of(order.getCustomer()));
		orderDTO.setOrderDate(order.getOrderDate());
		orderDTO.setStatus(order.getStatus());
		orderDTO.setOrderDetails(order.getOrderDetails().stream().map(od -> OrderDetailsModelMapping.of(od)).collect(Collectors.toList()));
		return orderDTO;
	}

//	public static Order of(OrderDTO orderDTO) {
//		Order order = new Order();
//		order.setOrderName(orderDTO.getOrderName());
//		order.setOrderId(orderDTO.getOrderId());
//		order.setPrice(orderDTO.getPrice());
//		return order;
//	}

	public static OrderResponseModel res(OrderDTO orderDTO) {
		OrderResponseModel response = new OrderResponseModel();
		response.setOrderId(orderDTO.getOrderId());
		response.setCreatedBy(UserModelMapping.res(orderDTO.getCreatedBy()));
		response.setCustomer(CustomerModelMapping.res(orderDTO.getCustomer()));
		response.setOrderDate(orderDTO.getOrderDate());
		response.setOrderId(orderDTO.getOrderId());
		response.setStatus(orderDTO.getStatus());
		response.setOrderDetails(orderDTO.getOrderDetails().stream().map(od -> OrderDetailsModelMapping.res(od)).collect(Collectors.toList()));
		return response;
	}

//	public static OrderDTO req(OrderRequestModel request) {
//		OrderDTO orderDTO = new OrderDTO();
//		orderDTO.setPrice(request.getPrice());
//		orderDTO.setOrderName(request.getOrderName());
//		return orderDTO;
//	}
}
