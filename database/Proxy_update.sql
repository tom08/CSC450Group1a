ALTER TABLE ProxyAdData.page
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ProxyAdData.ad_location_visit
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE ProxyAdData.keyword
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ProxyAdData.keyword_page
    ADD COLUMN created_at datetime DEFAULT CURRENT_TIMESTAMP;
