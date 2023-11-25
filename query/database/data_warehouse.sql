/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3307
 Source Server Type    : MySQL
 Source Server Version : 100425
 Source Host           : localhost:3307
 Source Schema         : data_warehouse

 Target Server Type    : MySQL
 Target Server Version : 100425
 File Encoding         : 65001

 Date: 24/11/2023 16:33:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dates_dim
-- ----------------------------
DROP TABLE IF EXISTS `dates_dim`;
CREATE TABLE `dates_dim`  (
  `date_sk` int NOT NULL AUTO_INCREMENT,
  `full_date` date NULL DEFAULT NULL,
  `day_of_week` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `calendar_month` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `calendar_year` int NULL DEFAULT NULL,
  `day_of_month` int NULL DEFAULT NULL,
  `day_of_year` int NULL DEFAULT NULL,
  PRIMARY KEY (`date_sk`) USING BTREE,
  UNIQUE INDEX `uc_unique_full_date`(`full_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for lottery_codes_dim
-- ----------------------------
DROP TABLE IF EXISTS `lottery_codes_dim`;
CREATE TABLE `lottery_codes_dim`  (
  `lottery_code_sk` int NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `dt_changed` date NULL DEFAULT current_timestamp,
  `dt_expired` date NULL DEFAULT NULL,
  PRIMARY KEY (`lottery_code_sk`) USING BTREE,
  UNIQUE INDEX `uc_unique_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 165 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lottery_results
-- ----------------------------
DROP TABLE IF EXISTS `lottery_results`;
CREATE TABLE `lottery_results`  (
  `id` int NOT NULL,
  `date` date NULL DEFAULT NULL,
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `lottery_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `prize` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lottery_results_fact
-- ----------------------------
DROP TABLE IF EXISTS `lottery_results_fact`;
CREATE TABLE `lottery_results_fact`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `date` int NULL DEFAULT NULL,
  `region` int NULL DEFAULT NULL,
  `lottery_code_id` int NULL DEFAULT NULL,
  `province` int NULL DEFAULT NULL,
  `prize` int NULL DEFAULT NULL,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `isDelete` bit(1) NULL DEFAULT b'0',
  `dtChanged` int NULL DEFAULT current_timestamp,
  `dtExpired` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_datekey`(`date` ASC) USING BTREE,
  INDEX `fk_mienkey`(`region` ASC) USING BTREE,
  INDEX `fk_tinhkey`(`province` ASC) USING BTREE,
  INDEX `fk_giaikey`(`prize` ASC) USING BTREE,
  INDEX `fk_dtChange`(`dtChanged` ASC) USING BTREE,
  INDEX `fk_dtExpire`(`dtExpired` ASC) USING BTREE,
  CONSTRAINT `fk_datekey` FOREIGN KEY (`date`) REFERENCES `dates_dim` (`date_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dtChange` FOREIGN KEY (`dtChanged`) REFERENCES `dates_dim` (`date_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dtExpire` FOREIGN KEY (`dtExpired`) REFERENCES `dates_dim` (`date_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_giaikey` FOREIGN KEY (`prize`) REFERENCES `prizes_dim` (`prize_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mienkey` FOREIGN KEY (`region`) REFERENCES `regions_dim` (`region_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tinhkey` FOREIGN KEY (`province`) REFERENCES `provinces_dim` (`province_sk`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7385 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for prizes_dim
-- ----------------------------
DROP TABLE IF EXISTS `prizes_dim`;
CREATE TABLE `prizes_dim`  (
  `prize_sk` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `dt_changed` date NULL DEFAULT current_timestamp,
  `dt_expired` date NULL DEFAULT NULL,
  PRIMARY KEY (`prize_sk`) USING BTREE,
  UNIQUE INDEX `uc_unique_column1`(`name` ASC) USING BTREE,
  UNIQUE INDEX `uc_unique_prize`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for provinces_dim
-- ----------------------------
DROP TABLE IF EXISTS `provinces_dim`;
CREATE TABLE `provinces_dim`  (
  `province_sk` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `dt_changed` date NULL DEFAULT current_timestamp,
  `dt_expired` date NULL DEFAULT NULL,
  PRIMARY KEY (`province_sk`) USING BTREE,
  UNIQUE INDEX `uc_unique_column1`(`name` ASC) USING BTREE,
  UNIQUE INDEX `uc_unique_province`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for regions_dim
-- ----------------------------
DROP TABLE IF EXISTS `regions_dim`;
CREATE TABLE `regions_dim`  (
  `region_sk` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `dt_changed` date NULL DEFAULT current_timestamp,
  `dt_expired` date NULL DEFAULT NULL,
  PRIMARY KEY (`region_sk`) USING BTREE,
  UNIQUE INDEX `uc_unique_column2`(`name` ASC) USING BTREE,
  UNIQUE INDEX `uc_unique_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Triggers structure for table lottery_codes_dim
-- ----------------------------
DROP TRIGGER IF EXISTS `dt_expired_code`;
delimiter ;;
CREATE TRIGGER `dt_expired_code` BEFORE INSERT ON `lottery_codes_dim` FOR EACH ROW BEGIN
    SET NEW.dt_expired = "9999-12-31";
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table lottery_results_fact
-- ----------------------------
DROP TRIGGER IF EXISTS `trigger_dtExpire`;
delimiter ;;
CREATE TRIGGER `trigger_dtExpire` BEFORE INSERT ON `lottery_results_fact` FOR EACH ROW BEGIN
    SET NEW.dtExpired = 999999;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table prizes_dim
-- ----------------------------
DROP TRIGGER IF EXISTS `dt_expired_prize`;
delimiter ;;
CREATE TRIGGER `dt_expired_prize` BEFORE INSERT ON `prizes_dim` FOR EACH ROW BEGIN
    SET NEW.dt_expired = "9999-12-31";
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table provinces_dim
-- ----------------------------
DROP TRIGGER IF EXISTS `dt_expired_province`;
delimiter ;;
CREATE TRIGGER `dt_expired_province` BEFORE INSERT ON `provinces_dim` FOR EACH ROW BEGIN
    SET NEW.dt_expired = "9999-12-31";
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table regions_dim
-- ----------------------------
DROP TRIGGER IF EXISTS `dt_expired_region`;
delimiter ;;
CREATE TRIGGER `dt_expired_region` BEFORE INSERT ON `regions_dim` FOR EACH ROW BEGIN
    SET NEW.dt_expired = "9999-12-31";
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
