package com.emles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.emles.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository <Customer, Long> {

	@Query(value = "from Customer c WHERE c.firstName LIKE %?1% OR c.secondName LIKE %?1%",
			countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.firstName LIKE %?1% or c.secondName LIKE %?1%", nativeQuery = false)
	Page<Customer> findByName(String name, Pageable pageable);
	
	@Query(value = "from Customer c WHERE c.phone LIKE %?1%",
			countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.phone LIKE %?1%", nativeQuery = false)
	Page<Customer> findCustomersByPhone(String phone, Pageable pageable);
	
	@Query(value = "from Customer c WHERE c.email LIKE %?1%",
			countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.email LIKE %?1%", nativeQuery = false)
	Page<Customer> findCustomersByEmail(String email, Pageable pageable);
	
	@Query(value = "from Customer c WHERE c.address LIKE %?1%",
			countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.address LIKE %?1%", nativeQuery = false)
	Page<Customer> findCustomersByAddress(String address, Pageable pageable);
	
	default Page<Customer> findCustomersBy(String findBy, String phrase, Pageable pageable) {
		FindCustomerBy by = FindCustomerBy.valueOf(findBy.toUpperCase());
		
		switch (by) {
		case ADDRESS:
			return findCustomersByAddress(phrase, pageable);
		case EMAIL:
			return findCustomersByEmail(phrase, pageable);
		case NAME:
			return findByName(phrase, pageable);
		case PHONE:
			return findCustomersByPhone(phrase, pageable);
		default:
			return findAll(pageable);
		}
	}
	
	Customer findByEmail(String email);
	
	Customer findByPhone(String phone);
}