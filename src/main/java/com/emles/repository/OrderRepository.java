package com.emles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.emles.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	public static final String COUNT_ORDERS_BY_CUSTOMER_ID = "select count(o.order_id) from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where c.customer_id=?1";

	public static final String FETCH_ORDERS_BY_CUSTOMER_ID = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name"
			+ ", c.second_name, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where c.customer_id=?1";

	public static final String FETCH_ORDERS_BY_NULL_CUSTOMER = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name"
			+ ", c.second_name, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where c.customer_id is null";

	public static final String COUNT_ORDERS_BY_NULL_CUSTOMER = "select count(o.order_id) from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where c.customer_id is null";

	public static final String FETCH_ORDERS_BY_NULL_PRODUCT = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name,"
			+ "c.second_name, c.email, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id left join order_details od "
			+ "on o.order_id=od.order_id left join products p on od.product_id = p.product_id where p.product_id is null";

	public static final String COUNT_ORDERS_BY_NULL_PRODUCT = "select count(o.order_id)"
			+ " from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id left join order_details od "
			+ "on o.order_id=od.order_id left join products p on od.product_id = p.product_id where p.product_id is null";

	public static final String FETCH_ORDERS_BY_NULL_USER = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name"
			+ ", c.second_name, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where u.app_user_id is null";

	public static final String COUNT_ORDERS_BY_NULL_USER = "select count(o.order_id) from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where u.app_user_id is null";

	public static final String FETCH_ORDERS_BY_PRODUCT_ID = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name,"
			+ "c.second_name, c.email, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id left join order_details od "
			+ "on o.order_id=od.order_id left join products p on od.product_id = p.product_id where p.product_id = ?1";

	public static final String COUNT_ORDERS_BY_PRODUCT_ID = "select count(o.order_id)"
			+ " from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id left join order_details od "
			+ "on o.order_id=od.order_id left join products p on od.product_id = p.product_id where p.product_id = ?1";

	public static final String FETCH_ORDERS_BY_USER_ID = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name"
			+ ", c.second_name, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where u.app_user_id = ?1";

	public static final String COUNT_ORDERS_BY_USER_ID = "select count(o.order_id) from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where u.app_user_id = ?1";

	public static final String FETCH_ORDERS_BY_ORDER_STATUS = "select o.order_id, o.order_date, o.status, u.app_user_id, u.email, c.customer_id, c.first_name"
			+ ", c.second_name, c.phone, c.address from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where cast(o.status as text) = ?1";

	public static final String COUNT_ORDERS_BY_ORDER_STATUS = "select count(o.order_id) from orders o left join user_order uo on o.order_id = uo.order_id"
			+ " left join app_user u on u.app_user_id = uo.app_user_id left join customer_order co "
			+ "on o.order_id = co.order_id left join customers c on c.customer_id = co.customer_id where cast(o.status as text) = ?1";

	@Query(value = FETCH_ORDERS_BY_ORDER_STATUS, countQuery = COUNT_ORDERS_BY_ORDER_STATUS, nativeQuery = true)
	Page<Order> findPaginatedOrdersByStatus(Pageable pageable, String orderStatus);

	default Page<Order> getOrdersBy(FindOrderBy findBy, Pageable pageable, long id) {
		switch (findBy) {
		case CUSTOMER_ID:
			return getOrdersByCustomerId(pageable, id);
		case NULL_CUSTOMER:
			return findByNullCustomer(pageable);
		case NULL_PRODUCT:
			return findByNullProduct(pageable);
		case NULL_USER:
			return findByNullUser(pageable);
		case PRODUCT_ID:
			return findByProductId(pageable, id);
		case USER_ID:
			return findByCreatedBy(pageable, id);
		default:
			return findAll(pageable);
		}
	}

	@Query(value = FETCH_ORDERS_BY_USER_ID, countQuery = COUNT_ORDERS_BY_USER_ID, nativeQuery = true)
	Page<Order> findByCreatedBy(Pageable pageable, long id);

	@Query(value = FETCH_ORDERS_BY_PRODUCT_ID, countQuery = COUNT_ORDERS_BY_PRODUCT_ID, nativeQuery = true)
	Page<Order> findByProductId(Pageable pageable, long id);

	@Query(value = FETCH_ORDERS_BY_NULL_USER, countQuery = COUNT_ORDERS_BY_NULL_USER, nativeQuery = true)
	Page<Order> findByNullUser(Pageable pageable);

	@Query(value = FETCH_ORDERS_BY_NULL_PRODUCT, countQuery = COUNT_ORDERS_BY_NULL_PRODUCT, nativeQuery = true)
	Page<Order> findByNullProduct(Pageable pageable);

	@Query(value = FETCH_ORDERS_BY_NULL_CUSTOMER, countQuery = COUNT_ORDERS_BY_NULL_CUSTOMER, nativeQuery = true)
	Page<Order> findByNullCustomer(Pageable pageable);

	@Query(value = FETCH_ORDERS_BY_CUSTOMER_ID, countQuery = COUNT_ORDERS_BY_CUSTOMER_ID, nativeQuery = true)
	Page<Order> getOrdersByCustomerId(Pageable pageable, Long id);

}
