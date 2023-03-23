CREATE TABLE box(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
box_name VARCHAR(40) NOT NULL,
box_type VARCHAR(40) NOT NULL,
box_weight FLOAT,
boxes_in_row INT,
rows_on_pallet INT
);