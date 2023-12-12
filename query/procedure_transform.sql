-- ---------------Procedure Transform field Date ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformDate`()
BEGIN

    -- 13.2 Lấy ra danh sách các ngày trong bảng staging vào date_dim lấy date_sk tương ứng
    -- 13.3 cập nhật lại vào bảng staging
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.dates_dim AS dim ON staging.date = dim.full_date
    SET staging.date = dim.date_sk;

END

-- ---------------Procedure Transform field Region ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformRegion`()
BEGIN
    CREATE TEMPORARY TABLE TempRegionList (region_name VARCHAR(255));

    -- 13.4 Lấy ra danh sách các region khác nhau trong bảng staging
    INSERT INTO TempRegionList
    SELECT DISTINCT region FROM lottery_results_staging;

    -- 13.5 Thêm các region đó vào bảng region_dim nếu chưa tồn tại
		INSERT INTO data_warehouse.regions_dim (`name`)
		SELECT region_name FROM TempRegionList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.regions_dim
				WHERE data_warehouse.regions_dim.`name` = TempRegionList.region_name
);
    -- 13.6 Lấy ra region_sk tương ứng với region trong bảng staging cập nhật lại vào bảng staging
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.regions_dim AS dim ON staging.region = dim.`name`
    SET staging.region = dim.region_sk;

    DROP TEMPORARY TABLE TempRegionList;
END

-- ---------------Procedure Transform field lottery_code ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformLotteryCode`()
BEGIN
    CREATE TEMPORARY TABLE TempCodeList (code_name VARCHAR(255));

    -- 13.7 Lấy ra danh sách các lottery_code trong bảng staging
    INSERT INTO TempCodeList
    SELECT DISTINCT lottery_code FROM lottery_results_staging;

    -- 13.8 Thêm các lottery_code đó vào bảng lottery_codes_dim nếu chưa tồn tại
		INSERT INTO data_warehouse.lottery_codes_dim (`code`)
		SELECT code_name FROM TempCodeList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.lottery_codes_dim
				WHERE data_warehouse.lottery_codes_dim.`code` = TempCodeList.code_name
);
    -- 13.9 Lây ra lottery_code_sk tương ứng với lottery_code trong bảng staging cập nhật lại vào bảng staging
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.lottery_codes_dim AS dim ON staging.lottery_code = dim.`code`
    SET staging.lottery_code = dim.lottery_code_sk;

    DROP TEMPORARY TABLE TempCodeList;
END

-- ---------------Procedure Transform field province ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformProvince`()
BEGIN
    CREATE TEMPORARY TABLE TempProvinceList (province_name VARCHAR(255));
    -- 13.10 Lấy ra danh sách các province trong bảng staging
    INSERT INTO TempProvinceList
    SELECT DISTINCT province FROM lottery_results_staging;
    -- 13.11 Thêm các province đó vào bảng provinces_dim nếu chưa tồn tại
		INSERT INTO data_warehouse.provinces_dim (`name`)
		SELECT province_name FROM TempProvinceList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.provinces_dim
				WHERE data_warehouse.provinces_dim.`name` = TempProvinceList.province_name
);
    -- 13.12 Lây ra province_sk tương ứng với province trong bảng staging cập nhật lại vào bảng staging
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.provinces_dim AS dim ON staging.province = dim.`name`
    SET staging.province = dim.province_sk;

    DROP TEMPORARY TABLE TempProvinceList;
END

-- ---------------Procedure Transform field Prize ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformPrize`()
BEGIN
    CREATE TEMPORARY TABLE TempPrizeList (prize_name VARCHAR(255));
    -- 13.13 Lấy ra danh sách các prize trong bảng staging
    INSERT INTO TempPrizeList
    SELECT DISTINCT prize FROM lottery_results_staging;
        -- 13.14 Thêm các prize đó vào bảng prizes_dim nếu chưa tồn tại
		INSERT INTO data_warehouse.prizes_dim (`name`)
		SELECT prize_name FROM TempPrizeList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.prizes_dim
				WHERE data_warehouse.prizes_dim.`name` = TempPrizeList.prize_name
);
    -- 13.15 Lây ra prize_sk tương ứng với prize trong bảng staging cập nhật lại vào bảng staging
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.prizes_dim AS dim ON staging.prize = dim.`name`
    SET staging.prize = dim.prize_sk;

    DROP TEMPORARY TABLE TempPrizeList;
END

-- ---------------Procedure Transform field CreatedDate ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformCreatedDate`()
BEGIN
    -- 13.16 Lấy ra danh sách các ngày trong bảng staging vào date_dim lấy date_sk tương ứng
    -- 13.17 cập nhật lại vào bảng staging
    JOIN data_warehouse.dates_dim AS dim ON staging.created_date = dim.full_date
    SET staging.created_date = dim.date_sk;

END

-- ---------------Procedure call all ----
CREATE DEFINER=`root`@`localhost` PROCEDURE `TransformData`()
BEGIN
    -- Gọi các procedure transform
    CALL data_staging.TransformDate();
    CALL data_staging.TransformRegion();
    CALL data_staging.TransformLotteryCode();
    CALL data_staging.TransformProvince();
    CALL data_staging.TransformPrize();
		CALL data_staging.TransformCreatedDate();
END