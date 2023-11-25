/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3307
 Source Server Type    : MySQL
 Source Server Version : 100425
 Source Host           : localhost:3307
 Source Schema         : db_control

 Target Server Type    : MySQL
 Target Server Version : 100425
 File Encoding         : 65001

 Date: 24/11/2023 16:32:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_file_configs
-- ----------------------------
DROP TABLE IF EXISTS `data_file_configs`;
CREATE TABLE `data_file_configs`  (
  `id` int NOT NULL,
  `name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `source_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `location` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `flag` bit(1) NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `update_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_files
-- ----------------------------
DROP TABLE IF EXISTS `data_files`;
CREATE TABLE `data_files`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `df_config_id` int NULL DEFAULT NULL,
  `name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `row_count` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_timestamp` datetime NULL DEFAULT current_timestamp ON UPDATE CURRENT_TIMESTAMP,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT current_timestamp ON UPDATE CURRENT_TIMESTAMP,
  `update_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_config`(`df_config_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 523 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Procedure structure for AggregateTable
-- ----------------------------
DROP PROCEDURE IF EXISTS `AggregateTable`;
delimiter ;;
CREATE PROCEDURE `AggregateTable`()
BEGIN
    SET @current_date = CURDATE();
    SET @start_date = DATE_SUB(@current_date, INTERVAL 29 DAY);

    TRUNCATE TABLE data_warehouse.lottery_results;

    INSERT INTO data_warehouse.lottery_results (id, date, region, lottery_code, province, prize, number)
    SELECT
        fact.id,
        date.full_date,
        re.`name`,
        co.`code`,
        pro.`name`,
        pri.`name`,
        fact.number
    FROM
        data_warehouse.lottery_results_fact AS fact
        JOIN data_warehouse.dates_dim AS date ON fact.date = date.date_sk
        JOIN data_warehouse.regions_dim AS re ON fact.region = re.region_sk
        JOIN data_warehouse.lottery_codes_dim AS co ON fact.lottery_code_id = co.lottery_code_sk
        JOIN data_warehouse.provinces_dim AS pro ON fact.province = pro.province_sk
        JOIN data_warehouse.prizes_dim AS pri ON fact.prize = pri.prize_sk
    WHERE
        date.full_date BETWEEN @start_date AND @current_date;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for ExtractToStaging
-- ----------------------------
DROP PROCEDURE IF EXISTS `ExtractToStaging`;
delimiter ;;
CREATE PROCEDURE `ExtractToStaging`(IN date VARCHAR(255),
    IN region VARCHAR(255),
    IN lottery_code VARCHAR(255),
    IN province VARCHAR(255),
    IN prize VARCHAR(255),
    IN number VARCHAR(255),
		IN created_date VARCHAR(255))
BEGIN
    -- Chèn dữ liệu vào bảng your_table
    INSERT INTO data_staging.lottery_results_staging (date, region, lottery_code, province, prize, number, created_date)
    VALUES (date, region, lottery_code, province, prize, number, created_date);
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for InsertStatus
-- ----------------------------
DROP PROCEDURE IF EXISTS `InsertStatus`;
delimiter ;;
CREATE PROCEDURE `InsertStatus`(IN id_config INT,
    IN `status` VARCHAR(255))
BEGIN
    -- Chèn dữ liệu vào bảng
    INSERT INTO data_files(df_config_id, `status`)
    VALUES (id_config, `status`);

    -- Nếu bạn muốn thêm logic xử lý hoặc kiểm tra điều kiện, bạn có thể thực hiện ở đây.

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for InsertStatusdAndName
-- ----------------------------
DROP PROCEDURE IF EXISTS `InsertStatusdAndName`;
delimiter ;;
CREATE PROCEDURE `InsertStatusdAndName`(IN id_config INT,
    IN file_name VARCHAR(255),
    IN `status` VARCHAR(255))
BEGIN
    -- Chèn dữ liệu vào bảng
    INSERT INTO data_files(df_config_id, `name`, `status`)
    VALUES (id_config, file_name, `status`);

    -- Nếu bạn muốn thêm logic xử lý hoặc kiểm tra điều kiện, bạn có thể thực hiện ở đây.

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for LoadDataToWH
-- ----------------------------
DROP PROCEDURE IF EXISTS `LoadDataToWH`;
delimiter ;;
CREATE PROCEDURE `LoadDataToWH`()
BEGIN
		
		INSERT INTO data_warehouse.lottery_results_fact (date, region, lottery_code_id, province, prize, number, dtChanged)
		SELECT staging.date, staging.region, staging.lottery_code, staging.province, staging.prize, staging.number, staging.created_date
		FROM data_staging.lottery_results_staging staging;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for LoadToDM
-- ----------------------------
DROP PROCEDURE IF EXISTS `LoadToDM`;
delimiter ;;
CREATE PROCEDURE `LoadToDM`()
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
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformData
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformData`;
delimiter ;;
CREATE PROCEDURE `TransformData`()
BEGIN
    -- Thực hiện procedure 1
    CALL data_staging.TransformDate();

    -- Thực hiện procedure 2
    CALL data_staging.TransformRegion();

    -- Thực hiện procedure 3
    CALL data_staging.TransformLotteryCode();
		
		-- Thực hiện procedure 3
    CALL data_staging.TransformProvince();
		
		-- Thực hiện procedure 3
    CALL data_staging.TransformPrize();

    -- Thêm các procedure khác nếu cần
		CALL data_staging.TransformCreatedDate();
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for truncate_staging_table
-- ----------------------------
DROP PROCEDURE IF EXISTS `truncate_staging_table`;
delimiter ;;
CREATE PROCEDURE `truncate_staging_table`()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Thực hiện câu lệnh TRUNCATE TABLE tại đây
    TRUNCATE TABLE data_staging.lottery_results_staging;

    COMMIT;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
