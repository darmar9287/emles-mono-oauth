package com.emles.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

	@Id
	@Column(name = "order_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "order_date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date orderDate;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@ManyToOne(targetEntity = AppUser.class, fetch = FetchType.LAZY)
	@JoinTable(name = "user_order", joinColumns = {
			@JoinColumn(name = "order_id", referencedColumnName = "order_id") }, inverseJoinColumns = {
					@JoinColumn(name = "app_user_id", referencedColumnName = "app_user_id") })
	private AppUser createdBy;
	
	@ManyToOne(targetEntity = Customer.class, fetch = FetchType.LAZY)
	@JoinTable(name = "customer_order", joinColumns = {
			@JoinColumn(name = "order_id", referencedColumnName = "order_id") }, inverseJoinColumns = {
					@JoinColumn(name = "customer_id", referencedColumnName = "customer_id") })
	private Customer customer;
	
	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<OrderDetail> orderDetails = new HashSet<>();
}
