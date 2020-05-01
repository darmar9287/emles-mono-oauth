package com.emles.modelmappings;

import com.emles.dto.OrderDTO;
import com.emles.dto.OrderDetailsDTO;
import com.emles.model.Order;
import com.emles.model.OrderDetail;
import com.emles.model.response.OrderDetailsResponseModel;

public class OrderDetailsModelMapping {

	private OrderDetailsModelMapping() {}
	
	public static OrderDetailsDTO of(OrderDetail orderDetails) {
		OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();
		orderDetailsDTO.setOrderDetailsId(orderDetails.getId());
		orderDetailsDTO.setPriceUnit(orderDetails.getPriceUnit());
		orderDetailsDTO.setQuantity(orderDetails.getQuantity());
		orderDetailsDTO.setProduct(ProductModelMapping.of(orderDetails.getProduct()));
		OrderDTO orderDTO = new OrderDTO();
		Order o = orderDetails.getOrder();
		orderDTO.setOrderId(o.getId());
		orderDetailsDTO.setOrder(orderDTO);
		return orderDetailsDTO;
	}
	
	public static OrderDetail of(OrderDetailsDTO orderDetailsDTO) {
		OrderDetail orderDetails = new OrderDetail();
		orderDetails.setId(orderDetailsDTO.getOrderDetailsId());
		orderDetails.setPriceUnit(orderDetailsDTO.getPriceUnit());
		orderDetails.setQuantity(orderDetailsDTO.getQuantity());
		orderDetails.setProduct(ProductModelMapping.of(orderDetailsDTO.getProduct()));
		return orderDetails;
	}
	
	public static OrderDetailsResponseModel res(OrderDetailsDTO orderDetailsDTO) {
		OrderDetailsResponseModel response = new OrderDetailsResponseModel();
		response.setOrderDetailsId(orderDetailsDTO.getOrderDetailsId());
		response.setPriceUnit(orderDetailsDTO.getPriceUnit());
		response.setQuantity(orderDetailsDTO.getQuantity());
		response.setProductId(orderDetailsDTO.getProduct().getProductId());
		response.setOrderId(orderDetailsDTO.getOrder().getOrderId());
		return response;
	}
	
//	public static OrderDetailsDTO req(OrderDetailsRequestModel request) {
//		OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();
//		orderDetailsDTO.setOrderDetailsId(request.getOrderDetailsId());
//		orderDetailsDTO.setPriceUnit(request.getPriceUnit());
//		orderDetailsDTO.setQuantity(request.getQuantity());
//		orderDetailsDTO.setProduct(ProductModelMapping.req(request.getProduct()));
//		return orderDetailsDTO;
//	}
}
