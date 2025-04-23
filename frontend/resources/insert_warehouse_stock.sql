INSERT INTO warehouse_stock (warehouse_id, product_id, stock_level)
VALUES (1, 101, 5);

INSERT INTO warehouse_stock (warehouse_id, product_id, stock_level)
VALUES (2, 101, 10);

UPDATE warehouse_stock
SET stock_level = 10
WHERE warehouse_id = 2 AND product_id = 101;

UPDATE warehouse_stock
SET stock_level = 5
WHERE warehouse_id = 1 AND product_id = 101;