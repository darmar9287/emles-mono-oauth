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

import com.emles.dto.ProductDTO;
import com.emles.exception.ValidationException;
import com.emles.model.request.ProductRequestModel;
import com.emles.model.response.ProductResponseModel;
import com.emles.service.ProductService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/product")
public class ProductController {

	private ProductService productService;
	
	public ProductController(ProductService productService) {
		super();
		this.productService = productService;
	}

	@Value("${config.pagination.default_page_offset}")
	private Integer PER_PAGE;

	private ModelMapper mapper = new ModelMapper();
	
	@ApiOperation("Create product endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PostMapping(value = "/create")
	public ResponseEntity<ProductResponseModel> createProduct(@Valid @RequestBody ProductRequestModel model, @ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		
		ProductDTO productDTO = mapper.map(model, ProductDTO.class);
		ProductDTO createdProduct = productService.createProduct(productDTO);
		ProductResponseModel response = mapper.map(createdProduct, ProductResponseModel.class);
		return ResponseEntity.ok(response);
	}
	
	@ApiOperation("Update product endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@PutMapping(value = "/update")
	public ResponseEntity<ProductResponseModel> updateProduct(@Valid @RequestBody ProductRequestModel model,
			@ApiIgnore Errors errors) {
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		ProductDTO productDTO = mapper.map(model, ProductDTO.class);
		ProductDTO updatedProductDTO = productService.updateProduct(productDTO);
		ProductResponseModel response = mapper.map(updatedProductDTO, ProductResponseModel.class);
		return ResponseEntity.ok(response);
	}
	
	@ApiOperation("Show single product endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = "/show/{productId}")
	public ResponseEntity<ProductResponseModel> showProduct(@PathVariable("productId") Long productId) {
		ProductDTO productDTO = productService.findProductById(productId);
		ProductResponseModel response = mapper.map(productDTO, ProductResponseModel.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Delete product endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@DeleteMapping(value = "/delete/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable("productId") Long productId) {
		productService.deleteProduct(productId);
		return ResponseEntity.noContent().build();
	}
	
	@ApiOperation("Show list of paginated products endpoint.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@GetMapping(value = { "/list/{page}", "/list" })
	public Page<ProductResponseModel> showProducts(@PathVariable(name = "page", required = false) Integer page,
			@RequestParam(name = "search", required = false, defaultValue = "") String search,
			@RequestParam(name = "perPage", required = false) String perPage) {
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
		Page<ProductDTO> pages = productService.findProductsByProductName(search, pageable);
		List<ProductResponseModel> content = pages.getContent()
				.stream()
				.map(productDTO -> mapper.map(productDTO, ProductResponseModel.class))
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, pages.getTotalElements());
	}
}
