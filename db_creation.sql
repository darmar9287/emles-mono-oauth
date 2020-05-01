\set ON_ERROR_STOP on
CREATE DATABASE emles_mono_oauth;
\c emles_mono_oauth

CREATE TABLE oauth_client_token (
  token_id VARCHAR(255),
  token BYTEA,
  authentication_id VARCHAR(255),
  user_name VARCHAR(255),
  client_id VARCHAR(255)
);

CREATE TABLE oauth_client_details (
  client_id VARCHAR(255) NOT NULL,
  resource_ids VARCHAR(255) DEFAULT NULL,
  client_secret VARCHAR(255) DEFAULT NULL,
  scope VARCHAR(255) DEFAULT NULL,
  authorized_grant_types VARCHAR(255) DEFAULT NULL,
  web_server_redirect_uri VARCHAR(255) DEFAULT NULL,
  authorities VARCHAR(255) DEFAULT NULL,
  access_token_validity INTEGER DEFAULT NULL,
  refresh_token_validity INTEGER DEFAULT NULL,
  additional_information VARCHAR(255) DEFAULT NULL,
  autoapprove VARCHAR(255) DEFAULT NULL
);

CREATE TABLE oauth_access_token (
  token_id VARCHAR(255),
  token BYTEA,
  authentication_id VARCHAR(255),
  user_name VARCHAR(255),
  client_id VARCHAR(255),
  authentication BYTEA,
  refresh_token VARCHAR(255)
);

CREATE TABLE oauth_refresh_token(
  token_id VARCHAR(255),
  token BYTEA,
  authentication BYTEA
);

CREATE TABLE authority (
  authority_id SERIAL NOT NULL,
  authority_name VARCHAR(30) NOT NULL UNIQUE,
  PRIMARY KEY (authority_id)
);

CREATE TABLE roles (
    role_id SERIAL NOT NULL,
    role_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (role_id)
);

CREATE TABLE app_user (
  app_user_id  SERIAL NOT NULL,
  enabled BOOLEAN NOT NULL,
  password VARCHAR(255) NOT NULL,
  last_password_reset_date TIMESTAMP DEFAULT NOW(),
  email VARCHAR(255) NOT NULL,
  last_sign_in_ip VARCHAR(50) NULL,
  last_sign_in_date VARCHAR(50) NULL,
  PRIMARY KEY (app_user_id)
);

CREATE TABLE app_user_authority (
  app_user_id BIGINT NOT NULL,
  authority_id BIGINT NOT NULL,
  PRIMARY KEY(app_user_id, authority_id),
  FOREIGN KEY(app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
  FOREIGN KEY(authority_id) REFERENCES authority(authority_id) ON DELETE CASCADE
);

CREATE TABLE app_user_roles (
    app_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(app_user_id, role_id),
    FOREIGN KEY(app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE TABLE oauth_code (
  code VARCHAR(255), 
  authentication BYTEA
);

CREATE TABLE oauth_approvals (
    userId VARCHAR(255),
    clientId VARCHAR(255),
    scope VARCHAR(255),
    status VARCHAR(10),
    expiresAt TIMESTAMP(0),
    lastModifiedAt TIMESTAMP(0)
);

CREATE TABLE password_reset_token (
  password_reset_token_id SERIAL NOT NULL,
  app_user_id INTEGER NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  token VARCHAR(255) NOT NULL,
  PRIMARY KEY (password_reset_token_id),
  FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE
);

CREATE TABLE account_activation_token (
  account_activation_token_id SERIAL NOT NULL,
  app_user_id INTEGER NOT NULL,
  token VARCHAR(255) NOT NULL,
  PRIMARY KEY (account_activation_token_id),
  FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE
);

create sequence products_seq;

create table products (
	product_id int check (product_id > 0) not null primary key default nextval ('products_seq'),
    product_name varchar(255) not null,
    price decimal(10, 2) check (price > 0) not null,
	check(price > 0.0)
);

create sequence customers_seq;

create table customers (
	customer_id int check (customer_id > 0) not null primary key default nextval ('customers_seq'),
    first_name varchar(100) not null,
    second_name varchar(100) not null,
    email varchar(100) not null unique,
    phone varchar(30) not null unique,
    address varchar(100) not null
);

CREATE SEQUENCE orders_seq;

CREATE TABLE orders (
    order_id int check (order_id > 0) NOT NULL primary key default nextval ('orders_seq'),
    order_date timestamp(0) NOT NULL,
	status varchar(50) not null
);

CREATE TABLE user_order (
	app_user_id int check (app_user_id > 0) not null,
	order_id int check (order_id > 0) not null,
	foreign key(app_user_id) references app_user(app_user_id) on delete cascade,
	foreign key(order_id) references orders(order_id) on delete cascade,
	primary key(app_user_id, order_id)
);

CREATE TABLE customer_order (
	customer_id int check (customer_id > 0) not null,
	order_id int check (order_id > 0) not null,
	foreign key(customer_id) references customers(customer_id) on delete cascade,
	foreign key(order_id) references orders(order_id) on delete cascade,
	primary key(customer_id, order_id)
);

CREATE SEQUENCE order_details_seq;

CREATE TABLE order_details (
    order_detail_id int check (order_detail_id > 0) NOT NULL primary key default nextval ('order_details_seq'),
    order_id int check (order_id > 0) NOT NULL,
	product_id int check (product_id > 0) NOT NULL,
    quantity int check (quantity > 0) NOT NULL,
	price_unit decimal(10, 2) check (price_unit > 0) not null,
    foreign key(order_id) references orders(order_id) on delete cascade,
	check(quantity > 0),
	check(price_unit > 0.0)
);

INSERT INTO roles (role_name)  VALUES('ROLE_ADMIN');
INSERT INTO roles (role_name) VALUES('ROLE_USER');
INSERT INTO authority(authority_name) VALUES('READ_AUTHORITY');
INSERT INTO authority(authority_name) VALUES('WRITE_AUTHORITY');
INSERT INTO app_user (enabled, password, last_password_reset_date, email) VALUES(TRUE,'$2a$10$BurTWIy5NTF9GJJH4magz.9Bd4bBurWYG8tmXxeQh1vs7r/wnCFG2', now(), 'oauth_admin@test.com');

INSERT INTO app_user_authority (app_user_id, authority_id) VALUES (1,1);
INSERT INTO app_user_roles (app_user_id, role_id) VALUES(1, 1);

INSERT INTO oauth_client_details VALUES('oauth_client_id','oauth_server_api', '$2a$10$BurTWIy5NTF9GJJH4magz.9Bd4bBurWYG8tmXxeQh1vs7r/wnCFG2', 'read,write', 'refresh_token,password', 'http://127.0.0.1', 'ROLE_ADMIN,ROLE_USER', 7200, 14400, NULL, 'true');
INSERT INTO products (product_name,  price) VALUES ('SOS', 9.99);
INSERT INTO products (product_name,  price) VALUES ('SOS 1', 9.99);

INSERT INTO customers(first_name, second_name, email, phone, address) values ('Marek', 'Polny', 'marekpolny@test.com', '111111111', 'ABCD 12');

INSERT INTO orders (order_date, status) values (NOW(), 'CREATED');
INSERT INTO user_order (order_id, app_user_id) values (1, 1);
INSERT INTO customer_order (order_id, customer_id) values (1, 1);
INSERT INTO order_details (order_id, product_id, quantity, price_unit) values(1, 1, 2, 9.99);
------------------------------------------------------END OF DEVELOPMENT/PRODUCTION DATABASE SCHEMA------------------------------------------------------

CREATE DATABASE emles_mono_oauth_test;
\c emles_mono_oauth_test
CREATE TABLE oauth_client_token (
  token_id VARCHAR(255),
  token BYTEA,
  authentication_id VARCHAR(255),
  user_name VARCHAR(255),
  client_id VARCHAR(255)
);

CREATE TABLE oauth_client_details (
  client_id VARCHAR(255) NOT NULL,
  resource_ids VARCHAR(255) DEFAULT NULL,
  client_secret VARCHAR(255) DEFAULT NULL,
  scope VARCHAR(255) DEFAULT NULL,
  authorized_grant_types VARCHAR(255) DEFAULT NULL,
  web_server_redirect_uri VARCHAR(255) DEFAULT NULL,
  authorities VARCHAR(255) DEFAULT NULL,
  access_token_validity INTEGER DEFAULT NULL,
  refresh_token_validity INTEGER DEFAULT NULL,
  additional_information VARCHAR(255) DEFAULT NULL,
  autoapprove VARCHAR(255) DEFAULT NULL
);

CREATE TABLE oauth_access_token (
  token_id VARCHAR(255),
  token BYTEA,
  authentication_id VARCHAR(255),
  user_name VARCHAR(255),
  client_id VARCHAR(255),
  authentication BYTEA,
  refresh_token VARCHAR(255)
);

CREATE TABLE oauth_refresh_token(
  token_id VARCHAR(255),
  token BYTEA,
  authentication BYTEA
);

CREATE TABLE authority (
  authority_id SERIAL NOT NULL,
  authority_name VARCHAR(30) NOT NULL UNIQUE,
  PRIMARY KEY (authority_id)
);

CREATE TABLE roles (
    role_id SERIAL NOT NULL,
    role_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (role_id)
);

CREATE TABLE app_user (
  app_user_id  SERIAL NOT NULL,
  enabled BOOLEAN NOT NULL,
  password VARCHAR(255) NOT NULL,
  last_password_reset_date TIMESTAMP DEFAULT NOW(),
  email VARCHAR(255) NOT NULL,
  last_sign_in_ip VARCHAR(50) NULL,
  last_sign_in_date VARCHAR(50) NULL,
  PRIMARY KEY (app_user_id)
);

CREATE TABLE app_user_authority (
  app_user_id BIGINT NOT NULL,
  authority_id BIGINT NOT NULL,
  PRIMARY KEY(app_user_id, authority_id),
  FOREIGN KEY(app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
  FOREIGN KEY(authority_id) REFERENCES authority(authority_id) ON DELETE CASCADE
);

CREATE TABLE app_user_roles (
    app_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(app_user_id, role_id),
    FOREIGN KEY(app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE TABLE oauth_code (
  code VARCHAR(255), 
  authentication BYTEA
);

CREATE TABLE oauth_approvals (
    userId VARCHAR(255),
    clientId VARCHAR(255),
    scope VARCHAR(255),
    status VARCHAR(10),
    expiresAt TIMESTAMP(0),
    lastModifiedAt TIMESTAMP(0)
);

CREATE TABLE password_reset_token (
  password_reset_token_id SERIAL NOT NULL,
  app_user_id INTEGER NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  token VARCHAR(255) NOT NULL,
  PRIMARY KEY (password_reset_token_id),
  FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE
);

CREATE TABLE account_activation_token (
  account_activation_token_id SERIAL NOT NULL,
  app_user_id INTEGER NOT NULL,
  token VARCHAR(255) NOT NULL,
  PRIMARY KEY (account_activation_token_id),
  FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE
);

create sequence products_seq;

create table products (
	product_id int check (product_id > 0) not null primary key default nextval ('products_seq'),
    product_name varchar(255) not null,
    price decimal(10, 2) check (price > 0) not null,
	check(price > 0.0)
);

create sequence customers_seq;

create table customers (
	customer_id int check (customer_id > 0) not null primary key default nextval ('customers_seq'),
    first_name varchar(100) not null,
    second_name varchar(100) not null,
    email varchar(100) not null unique,
    phone varchar(30) not null unique,
    address varchar(100) not null
);

CREATE SEQUENCE orders_seq;

CREATE TABLE orders (
    order_id int check (order_id > 0) NOT NULL primary key default nextval ('orders_seq'),
    order_date timestamp(0) NOT NULL,
	status varchar(50) not null
);

CREATE TABLE user_order (
	app_user_id int check (app_user_id > 0) not null,
	order_id int check (order_id > 0) not null,
	foreign key(app_user_id) references app_user(app_user_id) on delete cascade,
	foreign key(order_id) references orders(order_id) on delete cascade,
	primary key(app_user_id, order_id)
);

CREATE TABLE customer_order (
	customer_id int check (customer_id > 0) not null,
	order_id int check (order_id > 0) not null,
	foreign key(customer_id) references customers(customer_id) on delete cascade,
	foreign key(order_id) references orders(order_id) on delete cascade,
	primary key(customer_id, order_id)
);

CREATE SEQUENCE order_details_seq;

CREATE TABLE order_details (
    order_detail_id int check (order_detail_id > 0) NOT NULL primary key default nextval ('order_details_seq'),
    order_id int check (order_id > 0) NOT NULL,
	product_id int check (product_id > 0) NOT NULL,
    quantity int check (quantity > 0) NOT NULL,
	price_unit decimal(10, 2) check (price_unit > 0) not null,
    foreign key(order_id) references orders(order_id) on delete cascade,
	check(quantity > 0),
	check(price_unit > 0.0)
);
\unset ON_ERROR_STOP
