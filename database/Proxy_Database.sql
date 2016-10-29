DROP DATABASE IF EXISTS ProxyAdData;
CREATE DATABASE ProxyAdData;
USE ProxyAdData;
CREATE TABLE ProxyAdData.page (
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	url VARCHAR(2083)
);

CREATE TABLE ProxyAdData.ad_location_visit(
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_location varchar(2),
	focus_ratio double,
	active_ratio double,
	total_spent double,
	page_id bigint,
	FOREIGN KEY(page_id) REFERENCES page(id)
);

CREATE TABLE ProxyAdData.keyword(
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	keyword_name varchar(128)
);

CREATE TABLE ProxyAdData.keyword_page(
	keyword_id bigint,
	page_id bigint,
	FOREIGN KEY(page_id)
		REFERENCES page(id),
	FOREIGN KEY(keyword_id)
		REFERENCES keyword(id),
	PRIMARY KEY(keyword_id, page_id)
);
