package com.emles.model.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.emles.utils.ValidationErrorMessages;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDetailsRequestModel {
	@Min(value = 1, message = ValidationErrorMessages.QUANTITY_HIGHER_THAN_ZERO_MESSAGE)
	@NotNull(message = ValidationErrorMessages.QUANTITY_NOT_EMPTY_MESSAGE)
	private Long quantity;
	
	@NotNull(message = ValidationErrorMessages.PRODUCT_ID_NOT_EMPTY_MESSAGE)
	private Long productId;
	
	@Override
	public int hashCode() {
		return productId == null ? 0 : productId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderDetailsRequestModel other = (OrderDetailsRequestModel) obj;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		return true;
	}
}
