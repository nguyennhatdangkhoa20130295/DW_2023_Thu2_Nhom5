-- ---------------Procedure Load data to warehouse ----

CREATE DEFINER=`root`@`localhost` PROCEDURE `LoadDataToWH`()
BEGIN
        -- 14.2 Lấy dữ liệu từ bảng staging thêm vào bảng fact sau khi đã transform
		INSERT INTO data_warehouse.lottery_results_fact (date, region, lottery_code_id, province, prize, number, dtChanged)
		SELECT staging.date, staging.region, staging.lottery_code, staging.province, staging.prize, staging.number, staging.created_date
		FROM data_staging.lottery_results_staging staging;

END