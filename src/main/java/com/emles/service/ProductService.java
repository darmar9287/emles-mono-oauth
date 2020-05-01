package com.emles.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emles.dto.ProductDTO;

public interface ProductService {
	ProductDTO findProductById(Long productId);
	Page<ProductDTO> findProductsByProductName(String productName, Pageable pageable);
	void deleteProduct(Long productId);
	ProductDTO createProduct(ProductDTO productDTO);
	ProductDTO updateProduct(ProductDTO productDTO);
}
