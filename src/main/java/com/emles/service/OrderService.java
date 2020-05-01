package com.emles.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emles.dto.OrderDTO;
import com.emles.dto.OrderStatusDTO;
import com.emles.model.Order;
import com.emles.model.request.OrderRequestModel;

public interface OrderService {
	OrderDTO findOrderById(Long orderId);

	Order saveOrder(Order order);

	OrderDTO createOrder(OrderRequestModel model, String username);

	OrderDTO updateOrderStatus(OrderStatusDTO orderStatusDTO);

	void deleteOrder(Long orderId);

	Page<OrderDTO> findOrdersBy(Pageable pageable, String searchBy, String id);
}
