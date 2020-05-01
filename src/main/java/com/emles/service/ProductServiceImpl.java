package com.emles.service;

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

import com.emles.dto.ProductDTO;
import com.emles.exception.ProductNotFoundException;
import com.emles.model.Product;
import com.emles.repository.ProductRepository;
import com.emles.utils.ValidationErrorMessages;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	
	private ModelMapper mapper = new ModelMapper();

	@Override
	@Transactional
	public ProductDTO findProductById(Long productId) {
		Optional<Product> found = productRepository.findById(productId);
		if (!found.isPresent()) {
			throw new ProductNotFoundException(ValidationErrorMessages.CUSTOMER_NOT_FOUND_MESSAGE);
		}
		return mapper.map(found.get(), ProductDTO.class);
	}

	@Override
	@Transactional
	public Page<ProductDTO> findProductsByProductName(String productName, Pageable pageable) {
		Page<Product> result = productRepository.findPaginatedProductsByProductName(productName, pageable);
		List<ProductDTO> content = result
				.getContent()
				.stream()
				.map(product -> mapper.map(product, ProductDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, result.getTotalElements());
	}

	@Override
	@Transactional
	public void deleteProduct(Long productId) {
		Optional<Product> foundOpt = productRepository.findById(productId);
		if (!foundOpt.isPresent()) {
			throw new ProductNotFoundException(ValidationErrorMessages.PRODUCT_NOT_FOUND_MESSAGE);
		}
		productRepository.delete(foundOpt.get());
	}

	@Override
	@Transactional
	public ProductDTO createProduct(ProductDTO productDTO) {
		Product product = mapper.map(productDTO, Product.class);
		product = productRepository.save(product);
		return mapper.map(product, ProductDTO.class);
	}

	@Override
	public ProductDTO updateProduct(ProductDTO productDTO) {
		Optional<Product> foundOpt = productRepository.findById(productDTO.getProductId());
		if (!foundOpt.isPresent()) {
			throw new ProductNotFoundException(ValidationErrorMessages.PRODUCT_NOT_FOUND_MESSAGE);
		}
		Product found = foundOpt.get();
		found.setProductPrice(productDTO.getPrice());
		found.setProductName(productDTO.getProductName());
		found = productRepository.save(found);
		return mapper.map(found, ProductDTO.class);
	}
}
