-- ---------------Procedure Load data to warehouse ----

CREATE DEFINER=`root`@`localhost` PROCEDURE `LoadDataToWH`()
BEGIN
		
		INSERT INTO data_warehouse.lottery_results_fact (date, region, lottery_code_id, province, prize, number, dtChanged)
		SELECT staging.date, staging.region, staging.lottery_code, staging.province, staging.prize, staging.number, staging.created_date
		FROM data_staging.lottery_results_staging staging;

END