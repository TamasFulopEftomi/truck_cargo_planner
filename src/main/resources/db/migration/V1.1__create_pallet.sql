CREATE TABLE pallet(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
pallet_name VARCHAR(40) NOT NULL,
pallet_type VARCHAR(40) NOT NULL,
pallet_weight FLOAT,
lid_weight FLOAT,
stackable BOOLEAN
);