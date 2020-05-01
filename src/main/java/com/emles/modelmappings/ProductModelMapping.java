package com.emles.modelmappings;

import com.emles.dto.ProductDTO;
import com.emles.model.Product;
import com.emles.model.request.ProductRequestModel;
import com.emles.model.response.ProductResponseModel;

public class ProductModelMapping {

	private ProductModelMapping() {}
	
	public static ProductDTO of(Product product) {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductId(product.getId());
		productDTO.setProductName(product.getProductName());
		productDTO.setPrice(product.getProductPrice());
		return productDTO;
	}
	
	public static Product of(ProductDTO productDTO) {
		Product product = new Product();
		product.setProductName(productDTO.getProductName());
		product.setId(productDTO.getProductId());
		product.setProductPrice(productDTO.getPrice());
		return product;
	}
	
	public static ProductResponseModel res(ProductDTO productDTO) {
		ProductResponseModel response = new ProductResponseModel();
		response.setProductId(productDTO.getProductId());
		response.setProductName(productDTO.getProductName());
		response.setPrice(productDTO.getPrice());
		return response;
	}
	
	public static ProductDTO req(ProductRequestModel request) {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setPrice(request.getPrice());
		productDTO.setProductName(request.getProductName());
		return productDTO;
	}
}
