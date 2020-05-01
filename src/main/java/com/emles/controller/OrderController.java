package com.emles.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emles.dto.OrderDTO;
import com.emles.dto.OrderStatusDTO;
import com.emles.exception.ValidationException;
import com.emles.model.request.OrderRequestModel;
import com.emles.model.request.OrderStatusRequestModel;
import com.emles.model.response.OrderResponseModel;
import com.emles.modelmappings.OrderModelMapping;
import com.emles.service.OrderService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	public OrderController(OrderService orderService) {
		super();
		this.orderService = orderService;
	}

	private OrderService orderService;
	
	@Value("${config.pagination.default_page_offset}")
	private Integer PER_PAGE;

	private ModelMapper mapper = new ModelMapper();

	@ApiOperation("Create order endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PostMapping(value = "/create")
	public ResponseEntity<OrderResponseModel> createOrder(@Valid @RequestBody OrderRequestModel model, @ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		String createdByUsername = SecurityContextHolder.getContext().getAuthentication().getName();
		OrderDTO orderDTO = orderService.createOrder(model, createdByUsername);
		OrderResponseModel response = OrderModelMapping.res(orderDTO);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Update order endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PutMapping(value = "/update")
	public ResponseEntity<OrderResponseModel> updateOrderStatus(@Valid @RequestBody OrderStatusRequestModel model,
			@ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		OrderStatusDTO orderStatusDTO = mapper.map(model, OrderStatusDTO.class);
		OrderDTO orderDTO = orderService.updateOrderStatus(orderStatusDTO);
		OrderResponseModel response = OrderModelMapping.res(orderDTO);//(orderDTO, OrderResponseModel.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Show single order endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = "/show/{orderId}")
	public ResponseEntity<OrderResponseModel> showOrder(@PathVariable("orderId") Long orderId) {
		OrderDTO orderDTO = orderService.findOrderById(orderId);
		OrderResponseModel response = OrderModelMapping.res(orderDTO);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Delete order endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@DeleteMapping(value = "/delete/{orderId}")
	public ResponseEntity<?> deleteOrder(@PathVariable("orderId") Long orderId) {
		orderService.deleteOrder(orderId);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation("Get list of paginated orders endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = { "/list/{page}", "/list" })
	public Page<OrderResponseModel> showOrders(@PathVariable(name = "page", required = false) Integer page,
			@RequestParam(name = "searchBy", required = false, defaultValue = "order") String searchBy,
			@RequestParam(name = "per_page", required = false) String perPage,
			@RequestParam(name = "id", required = false) String id) {
		if (page == null) {
			page = 0;
		}
		Integer numberOfElementsPerPage = 0;
		try {
			numberOfElementsPerPage = Integer.parseInt(perPage);
		} catch (RuntimeException e) {
			numberOfElementsPerPage = PER_PAGE;
		}
		Pageable pageable = PageRequest.of(page, numberOfElementsPerPage);
		Page<OrderDTO> pages = orderService.findOrdersBy(pageable, searchBy, id);
		List<OrderResponseModel> content = pages.getContent()
				.stream()
				.map(orderDTO -> OrderModelMapping.res(orderDTO))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, pages.getTotalElements());
	}
}
