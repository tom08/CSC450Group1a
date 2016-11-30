DROP DATABASE IF EXISTS adData;
CREATE DATABASE adData;
USE adData;
CREATE TABLE adData.page (
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	url VARCHAR(2083)
);

CREATE TABLE adData.ad_location_visit(
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_location varchar(2),
	focus_ratio double,
	active_ratio double,
	total_spent double,
	page_id bigint,
	created_at datetime DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY(page_id) REFERENCES page(id)
);

CREATE TABLE adData.keyword(
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	keyword_name varchar(128)
);

CREATE TABLE adData.page_keywords(
	keywords bigint,
	page bigint,
	FOREIGN KEY(page)
		REFERENCES page(id),
	FOREIGN KEY(keywords)
		REFERENCES keyword(id),
	PRIMARY KEY(keywords, page)
);
