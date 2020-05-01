package com.emles.model.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderDetailsResponseModel {

	private long orderDetailsId;
	private long quantity;
	private long productId;
	private long orderId;
	private BigDecimal priceUnit;
}
