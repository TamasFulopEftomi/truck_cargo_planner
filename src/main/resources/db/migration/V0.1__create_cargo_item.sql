CREATE TABLE cargo_item(
id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
item_number VARCHAR(40),
qty_needs INT,
qty_to_be_delivered INT
);