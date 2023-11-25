-- ---------------Procedure Transform field Date ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformDate`()
BEGIN

    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.dates_dim AS dim ON staging.date = dim.full_date
    SET staging.date = dim.date_sk;

END

-- ---------------Procedure Transform field Region ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformRegion`()
BEGIN
    CREATE TEMPORARY TABLE TempRegionList (region_name VARCHAR(255));

    INSERT INTO TempRegionList
    SELECT DISTINCT region FROM lottery_results_staging;
    
		INSERT INTO data_warehouse.regions_dim (`name`)
		SELECT region_name FROM TempRegionList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.regions_dim
				WHERE data_warehouse.regions_dim.`name` = TempRegionList.region_name
);

    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.regions_dim AS dim ON staging.region = dim.`name`
    SET staging.region = dim.region_sk;

    DROP TEMPORARY TABLE TempRegionList;
END

-- ---------------Procedure Transform field lottery_code ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformLotteryCode`()
BEGIN
    CREATE TEMPORARY TABLE TempCodeList (code_name VARCHAR(255));


    INSERT INTO TempCodeList
    SELECT DISTINCT lottery_code FROM lottery_results_staging;

    
		INSERT INTO data_warehouse.lottery_codes_dim (`code`)
		SELECT code_name FROM TempCodeList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.lottery_codes_dim
				WHERE data_warehouse.lottery_codes_dim.`code` = TempCodeList.code_name
);

    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.lottery_codes_dim AS dim ON staging.lottery_code = dim.`code`
    SET staging.lottery_code = dim.lottery_code_sk;

    DROP TEMPORARY TABLE TempCodeList;
END

-- ---------------Procedure Transform field province ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformProvince`()
BEGIN
    CREATE TEMPORARY TABLE TempProvinceList (province_name VARCHAR(255));

    INSERT INTO TempProvinceList
    SELECT DISTINCT province FROM lottery_results_staging;

		INSERT INTO data_warehouse.provinces_dim (`name`)
		SELECT province_name FROM TempProvinceList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.provinces_dim
				WHERE data_warehouse.provinces_dim.`name` = TempProvinceList.province_name
);

    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.provinces_dim AS dim ON staging.province = dim.`name`
    SET staging.province = dim.province_sk;

    DROP TEMPORARY TABLE TempProvinceList;
END

-- ---------------Procedure Transform field Prize ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformPrize`()
BEGIN
    CREATE TEMPORARY TABLE TempPrizeList (prize_name VARCHAR(255));

    INSERT INTO TempPrizeList
    SELECT DISTINCT prize FROM lottery_results_staging;
    
		INSERT INTO data_warehouse.prizes_dim (`name`)
		SELECT prize_name FROM TempPrizeList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.prizes_dim
				WHERE data_warehouse.prizes_dim.`name` = TempPrizeList.prize_name
);
    
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.prizes_dim AS dim ON staging.prize = dim.`name`
    SET staging.prize = dim.prize_sk;

    DROP TEMPORARY TABLE TempPrizeList;
END

-- ---------------Procedure Transform field CreatedDate ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformCreatedDate`()
BEGIN
	
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.dates_dim AS dim ON staging.created_date = dim.full_date
    SET staging.created_date = dim.date_sk;

END

-- ---------------Procedure call all ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformData`()
BEGIN
    CALL data_staging.TransformDate();
    CALL data_staging.TransformRegion();
    CALL data_staging.TransformLotteryCode();
    CALL data_staging.TransformProvince();
    CALL data_staging.TransformPrize();
		CALL data_staging.TransformCreatedDate();
END