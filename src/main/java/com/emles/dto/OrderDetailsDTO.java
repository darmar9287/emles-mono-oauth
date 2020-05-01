package com.emles.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderDetailsDTO {

	private long orderDetailsId;
	private OrderDTO order;
	private long quantity;
	private ProductDTO product;
	private BigDecimal priceUnit;
}
