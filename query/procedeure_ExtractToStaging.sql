-- ----------------------------
-- Procedure structure for ExtractToStaging
-- ----------------------------
CREATE DEFINER=`root`@`localhost` PROCEDURE `ExtractToStaging`(IN date VARCHAR(255),
    IN region VARCHAR(255),
    IN lottery_code VARCHAR(255),
    IN province VARCHAR(255),
    IN prize VARCHAR(255),
    IN number VARCHAR(255),
	IN created_date VARCHAR(255))
BEGIN

    INSERT INTO data_staging.lottery_results_staging (date, region, lottery_code, province, prize, number, created_date)
    VALUES (date, region, lottery_code, province, prize, number, created_date);
END