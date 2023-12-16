-- ----------------------------
-- Procedure structure for InsertStatus
-- ----------------------------
CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertStatus`(
    IN id_config INT,
    IN `status` VARCHAR(255),
		IN date VARCHAR(255)
)
BEGIN
    DECLARE formatted_date VARCHAR(255);
	DECLARE lowerCaseStatus VARCHAR(255);

    SELECT created_at INTO formatted_date
    FROM data_files
    WHERE df_config_id = id_config
    ORDER BY created_at DESC
    LIMIT 1;

    SET formatted_date = DATE_FORMAT(date, '%d_%m_%Y');
		
	SET lowerCaseStatus = LOWER(`status`);
		
	SET @df_name = CONCAT('LotteryResults_', formatted_date);
    SET @note = CONCAT(
    CASE
        WHEN lowerCaseStatus LIKE '%ing' THEN 'Data is'
        WHEN lowerCaseStatus LIKE '%ed' THEN 'Data has been'
        ELSE 'Unknown status'
    END,
    ' ',
    lowerCaseStatus
	);

    INSERT INTO data_files(df_config_id, name, `status`, note)
    VALUES (id_config, @df_name, `status`, @note);
END