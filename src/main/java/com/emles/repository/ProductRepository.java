package com.emles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.emles.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query(value = "from Product p WHERE p.productName LIKE %?1%",
			countQuery = "SELECT COUNT(p) FROM Product p WHERE p.productName LIKE %?1%", nativeQuery = false)
	Page<Product> findPaginatedProductsByProductName(String productName, Pageable pageable);

}
