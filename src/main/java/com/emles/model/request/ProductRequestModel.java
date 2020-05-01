package com.emles.model.request;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.emles.utils.ValidationErrorMessages;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRequestModel {

	private Long productId;
	
	@NotBlank(message = ValidationErrorMessages.PRODUCT_NAME_NOT_EMPTY_MESSAGE)
	private String productName;
	
	@NotNull(message = ValidationErrorMessages.PRODUCT_PRICE_NOT_EMPTY_MESSAGE)
	@Min(value = 0, message = ValidationErrorMessages.PRODUCT_PRICE_HIGHER_THAN_ZERO_MESSAGE)
	private BigDecimal price;
}
