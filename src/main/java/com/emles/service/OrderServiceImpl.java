package com.emles.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emles.dto.OrderDTO;
import com.emles.dto.OrderStatusDTO;
import com.emles.exception.CustomerNotFoundException;
import com.emles.exception.OrderNotFoundException;
import com.emles.exception.ProductNotFoundException;
import com.emles.model.Customer;
import com.emles.model.Order;
import com.emles.model.OrderDetail;
import com.emles.model.OrderStatus;
import com.emles.model.Product;
import com.emles.model.AppUser;
import com.emles.model.request.OrderRequestModel;
import com.emles.modelmappings.OrderModelMapping;
import com.emles.repository.CustomerRepository;
import com.emles.repository.FindOrderBy;
import com.emles.repository.OrderRepository;
import com.emles.repository.ProductRepository;
import com.emles.repository.UserRepository;
import com.emles.utils.ValidationErrorMessages;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	private ModelMapper mapper = new ModelMapper();

	@Override
	public OrderDTO findOrderById(Long orderId) {
		Optional<Order> orderOpt = orderRepository.findById(orderId);
		if (!orderOpt.isPresent()) {
			throw new OrderNotFoundException(ValidationErrorMessages.ORDER_NOT_FOUND_MESSAGE);
		}
		OrderDTO orderDTO = OrderModelMapping.of(orderOpt.get());
		return orderDTO;
	}
	
	@Override
	@Transactional
	public Order saveOrder(Order order) {
		orderRepository.save(order);
		return order;
	}

	@Override
	@Transactional
	public OrderDTO createOrder(OrderRequestModel model, String username) {
		Optional<Customer> customerOpt = customerRepository.findById(model.getCustomerId());
		if (!customerOpt.isPresent()) {
			throw new CustomerNotFoundException(ValidationErrorMessages.CUSTOMER_NOT_FOUND_MESSAGE);
		}

		Order order = new Order();
		model.getOrderDetailsRequests().forEach(orderDetailsRequest -> {
			Optional<Product> productOpt = productRepository.findById(orderDetailsRequest.getProductId());
			if (!productOpt.isPresent()) {
				throw new ProductNotFoundException(ValidationErrorMessages.PRODUCT_NOT_FOUND_MESSAGE);
			}
			Product product = productOpt.get();
			OrderDetail od = new OrderDetail();
			od.setPriceUnit(product.getProductPrice());
			od.setProduct(product);
			od.setOrder(order);
			od.setQuantity(orderDetailsRequest.getQuantity());
			order.getOrderDetails().add(od);
		});
		
		AppUser createdBy = userRepository.findAppUserByUserDataEmail(username);
		
		order.setCreatedBy(createdBy);
		order.setCustomer(customerOpt.get());
		order.setOrderDate(Date.from(Instant.now()));
		order.setStatus(OrderStatus.CREATED);
		
		Order savedOrder = orderRepository.save(order);
		OrderDTO orderDTO = OrderModelMapping.of(savedOrder);
		return orderDTO;
	}

	@Override
	@Transactional
	public OrderDTO updateOrderStatus(OrderStatusDTO orderStatusDTO) {
		Optional<Order> orderOpt = orderRepository.findById(orderStatusDTO.getOrderId());
		if (!orderOpt.isPresent()) {
			throw new OrderNotFoundException(ValidationErrorMessages.ORDER_NOT_FOUND_MESSAGE);
		}
		Order order = orderOpt.get();
		order.setStatus(orderStatusDTO.getOrderStatus());
		orderRepository.save(order);
		OrderDTO orderDTO = mapper.map(order, OrderDTO.class);
		return orderDTO;
	}

	@Override
	@Transactional
	public void deleteOrder(Long orderId) {
		Optional<Order> orderOpt = orderRepository.findById(orderId);
		if (!orderOpt.isPresent()) {
			throw new OrderNotFoundException(ValidationErrorMessages.ORDER_NOT_FOUND_MESSAGE);
		}
		orderRepository.delete(orderOpt.get());
	}

	@Override
	@Transactional
	public Page<OrderDTO> findOrdersBy(Pageable pageable, String searchBy, String id) {
		Page<Order> results = null;
		try {
			FindOrderBy findBy = FindOrderBy.valueOf(searchBy.toUpperCase());
			if (findBy == FindOrderBy.ORDER_STATUS) {
				OrderStatus orderStatus = OrderStatus.valueOf(id.toUpperCase());
				results = orderRepository.findPaginatedOrdersByStatus(pageable, orderStatus.name());
			} else {
				results = orderRepository.getOrdersBy(findBy, pageable, id != null ? Long.parseLong(id) : 0L);
			}
		} catch (RuntimeException e) {
			results = orderRepository.findAll(pageable);
		}

		List<OrderDTO> content = results
				.getContent()
				.stream()
				.map(order -> OrderModelMapping.of(order))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, results.getTotalElements());
	}
}
