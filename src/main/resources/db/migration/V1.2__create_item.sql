CREATE TABLE item(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
item_no VARCHAR(40) NOT NULL,
item_weight FLOAT,
pcs_in_box INT,
box_id INT,
pallet_id INT
);