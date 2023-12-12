CREATE DEFINER=`root`@`localhost` PROCEDURE `LoadToDM`()
BEGIN

TRUNCATE TABLE data_mart.lottery_results_mien_nam;

INSERT INTO data_mart.lottery_results_mien_nam (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Nam';

TRUNCATE TABLE data_mart.lottery_results_mien_bac;

INSERT INTO data_mart.lottery_results_mien_bac (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Bắc';

TRUNCATE TABLE data_mart.lottery_results_mien_trung;

INSERT INTO data_mart.lottery_results_mien_trung (id, date, region, lottery_code, province, prize, number)
SELECT *
FROM data_warehouse.lottery_results
WHERE data_warehouse.lottery_results.region = 'KẾT QUẢ XỔ SỐ Miền Trung';

END