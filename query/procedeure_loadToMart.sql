CREATE DEFINER=`root`@`localhost` PROCEDURE `LoadToDM`()
BEGIN
-- 16.2 truncate bảng lottery_results_mien_nam
TRUNCATE TABLE data_mart.lottery_results_mien_nam;

-- 16.4 insert vào bảng lottery_results_mien_nam
INSERT INTO data_mart.lottery_results_mien_nam (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
-- 16.3 Lấy dữ liệu từ bảng  lottery_results trong data_warehouse của miền nam
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Nam';



-- 16.5 truncate bảng lottery_results_mien_bac
TRUNCATE TABLE data_mart.lottery_results_mien_bac;
-- 16.7 insert vào bảng lottery_results_mien_bac
INSERT INTO data_mart.lottery_results_mien_bac (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
-- 16.6 Lấy dữ liệu từ bảng lottery_results trong data_warehouse của miền bắc
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Bắc';



-- 16.10 insert vào bảng lottery_results_mien_trung
TRUNCATE TABLE data_mart.lottery_results_mien_trung;
-- 16.8 truncate bảng lottery_results_mien_trung
INSERT INTO data_mart.lottery_results_mien_trung (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
-- 16.9 Lấy dữ liệu từ bảng lottery_results trong data_warehouse của miền trung
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Trung';

END