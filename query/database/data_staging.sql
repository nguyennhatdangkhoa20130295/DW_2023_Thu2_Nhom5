/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3307
 Source Server Type    : MySQL
 Source Server Version : 100425
 Source Host           : localhost:3307
 Source Schema         : data_staging

 Target Server Type    : MySQL
 Target Server Version : 100425
 File Encoding         : 65001

 Date: 24/11/2023 16:33:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lottery_results_staging
-- ----------------------------
DROP TABLE IF EXISTS `lottery_results_staging`;
CREATE TABLE `lottery_results_staging`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `date` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `lottery_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `prize` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 126 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Procedure structure for TransformCreatedDate
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformCreatedDate`;
delimiter ;;
CREATE PROCEDURE `TransformCreatedDate`()
BEGIN
	
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.dates_dim AS dim ON staging.created_date = dim.full_date
    SET staging.created_date = dim.date_sk;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformDate
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformDate`;
delimiter ;;
CREATE PROCEDURE `TransformDate`()
BEGIN

    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.dates_dim AS dim ON staging.date = dim.full_date
    SET staging.date = dim.date_sk;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformLotteryCode
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformLotteryCode`;
delimiter ;;
CREATE PROCEDURE `TransformLotteryCode`()
BEGIN
     -- Tạo bảng tạm thời để lưu danh sách 
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

    -- Xóa bảng tạm thời
    DROP TEMPORARY TABLE TempCodeList;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformPrize
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformPrize`;
delimiter ;;
CREATE PROCEDURE `TransformPrize`()
BEGIN
     -- Tạo bảng tạm thời để lưu danh sách 
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

    -- Xóa bảng tạm thời
    DROP TEMPORARY TABLE TempPrizeList;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformProvince
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformProvince`;
delimiter ;;
CREATE PROCEDURE `TransformProvince`()
BEGIN
    -- Tạo bảng tạm thời để lưu danh sách các tỉnh cần thêm vào DIM tỉnh
    CREATE TEMPORARY TABLE TempProvinceList (province_name VARCHAR(255));

    -- Lấy danh sách tỉnh cần thêm vào bảng DIM tỉnh từ bảng staging
    INSERT INTO TempProvinceList
    SELECT DISTINCT province FROM lottery_results_staging;

    -- Thêm các tỉnh mới vào bảng DIM tỉnh nếu chưa tồn tại
		INSERT INTO data_warehouse.provinces_dim (`name`)
		SELECT province_name FROM TempProvinceList
		WHERE NOT EXISTS (
				SELECT 1
				FROM data_warehouse.provinces_dim
				WHERE data_warehouse.provinces_dim.`name` = TempProvinceList.province_name
);

    -- Cập nhật cột "tỉnh" trong bảng staging bằng ID của tỉnh
    UPDATE lottery_results_staging AS staging
    JOIN data_warehouse.provinces_dim AS dim ON staging.province = dim.`name`
    SET staging.province = dim.province_sk;

    -- Xóa bảng tạm thời
    DROP TEMPORARY TABLE TempProvinceList;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for TransformRegion
-- ----------------------------
DROP PROCEDURE IF EXISTS `TransformRegion`;
delimiter ;;
CREATE PROCEDURE `TransformRegion`()
BEGIN
     -- Tạo bảng tạm thời để lưu danh sách 
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

    -- Xóa bảng tạm thời
    DROP TEMPORARY TABLE TempRegionList;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
