ALTER TABLE adData.page
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE adData.ad_location_visit
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE adData.keyword
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE adData.page_keywords
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;
