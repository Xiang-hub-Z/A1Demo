/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306_1
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : wangbadb

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 07/12/2025 15:39:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '账号',
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '管理员名称',
  `phone` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '管理员手机号',
  `createtime` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 104 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (101, '54321', '54321', '王五', '098765432101', '2025-10-27 09:44:19');
INSERT INTO `admin` VALUES (102, '123', '321123', '张三', '19154874562', '2025-10-27 09:44:19');

-- ----------------------------
-- Table structure for area
-- ----------------------------
DROP TABLE IF EXISTS `area`;
CREATE TABLE `area`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '区域表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of area
-- ----------------------------
INSERT INTO `area` VALUES (1, '大厅', '普通上网区域', 1, 1, '2025-11-10 12:33:08', '2025-11-10 12:33:08');
INSERT INTO `area` VALUES (2, 'VIP包厢', '独立包厢环境', 1, 2, '2025-11-10 12:33:23', '2025-11-10 12:33:23');
INSERT INTO `area` VALUES (3, '竞技区', '高端配置游戏区', 1, 3, '2025-11-10 12:33:36', '2025-11-10 12:33:36');
INSERT INTO `area` VALUES (4, '测试', '测试', 1, 4, '2025-11-10 22:03:48', '2025-11-10 22:03:52');

-- ----------------------------
-- Table structure for computer
-- ----------------------------
DROP TABLE IF EXISTS `computer`;
CREATE TABLE `computer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `computer_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计算机编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计算机名称',
  `ip_address` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `configuration` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `price_per_hour` decimal(10, 2) NOT NULL COMMENT '每小时价格',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-空闲 2-使用中 3-维护中',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  INDEX `idx_computer_no`(`computer_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '计算机表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of computer
-- ----------------------------
INSERT INTO `computer` VALUES (1, 1, 'A001', '大厅001号机', '192.168.1.101', 'i5-10400F, GTX1660S, 16GB', 5.00, 2, '2025-11-10 12:35:07', '2025-11-10 12:35:07');
INSERT INTO `computer` VALUES (2, 1, 'A002', '大厅002号机', '192.168.1.102', 'i5-10400F, GTX1660S, 16GB', 5.00, 1, '2025-11-10 12:35:21', '2025-11-10 12:35:21');
INSERT INTO `computer` VALUES (3, 2, 'V001', 'VIP001号机', '192.168.1.201', 'i7-10700, RTX3060, 32GB', 10.00, 1, '2025-11-10 12:35:29', '2025-11-10 12:35:29');
INSERT INTO `computer` VALUES (4, 2, 'V002', 'VIP002号机', '192.168.1.202', 'i7-10700, RTX3070, 32GB', 12.00, 1, '2025-11-10 12:35:38', '2025-11-10 12:35:38');
INSERT INTO `computer` VALUES (5, 3, 'G001', '竞技001号机', '192.168.1.301', 'i9-10900, RTX3080, 64GB', 15.00, 1, '2025-11-10 12:35:45', '2025-11-10 12:35:45');
INSERT INTO `computer` VALUES (6, 2, 'V003', '测试', '192.168.2.2', '111111111', 22.00, 3, '2025-11-19 14:45:17', '2025-11-19 14:45:17');

-- ----------------------------
-- Table structure for computer_session
-- ----------------------------
DROP TABLE IF EXISTS `computer_session`;
CREATE TABLE `computer_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `computer_id` bigint NOT NULL COMMENT '计算机ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `total_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总费用',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-进行中 2-已结束 3-异常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_computer_id`(`computer_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '上机记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of computer_session
-- ----------------------------
INSERT INTO `computer_session` VALUES (32, 16, 4, '2025-11-16 15:32:21', '2025-11-16 15:32:41', 12.00, 2, '2025-11-16 15:32:21', '2025-11-16 15:32:21');
INSERT INTO `computer_session` VALUES (33, 16, 1, '2025-11-16 15:35:53', '2025-11-16 15:36:15', 5.00, 2, '2025-11-16 15:35:53', '2025-11-16 15:35:53');
INSERT INTO `computer_session` VALUES (34, 16, 1, '2025-11-16 15:37:55', '2025-11-16 15:38:01', 5.00, 2, '2025-11-16 15:37:55', '2025-11-16 15:37:55');
INSERT INTO `computer_session` VALUES (35, 16, 1, '2025-11-16 15:41:56', '2025-11-16 15:42:08', 5.00, 2, '2025-11-16 15:41:56', '2025-11-16 15:41:56');
INSERT INTO `computer_session` VALUES (36, 1, 5, '2025-11-19 15:15:06', '2025-11-19 15:15:57', 15.00, 2, '2025-11-19 15:15:06', '2025-11-19 15:15:06');
INSERT INTO `computer_session` VALUES (37, 16, 1, '2025-11-19 15:24:32', '2025-11-19 15:24:36', 5.00, 2, '2025-11-19 15:24:32', '2025-11-19 15:24:32');
INSERT INTO `computer_session` VALUES (38, 16, 1, '2025-11-20 13:44:38', '2025-11-20 13:45:02', 5.00, 2, '2025-11-20 13:44:38', '2025-11-20 13:44:38');
INSERT INTO `computer_session` VALUES (39, 15, 1, '2025-11-21 11:40:26', '2025-11-21 14:03:05', 15.00, 2, '2025-11-21 13:40:26', '2025-11-21 14:02:16');
INSERT INTO `computer_session` VALUES (40, 16, 3, '2025-11-20 13:52:50', '2025-11-23 14:05:39', 730.00, 2, '2025-11-23 13:52:50', '2025-11-23 13:59:07');
INSERT INTO `computer_session` VALUES (41, 1, 5, '2025-11-27 16:51:16', '2025-11-27 16:52:08', 15.00, 2, '2025-11-27 16:51:16', '2025-11-27 16:51:16');
INSERT INTO `computer_session` VALUES (42, 1, 1, '2025-11-27 16:53:24', NULL, 0.00, 1, '2025-11-27 16:53:24', '2025-11-27 16:53:24');
INSERT INTO `computer_session` VALUES (43, 16, 2, '2025-12-02 14:25:35', '2025-12-02 14:25:37', 5.00, 2, '2025-12-02 14:25:35', '2025-12-02 14:25:35');

-- ----------------------------
-- Table structure for recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `recharge_record`;
CREATE TABLE `recharge_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `amount` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-成功 2-失败 3-处理中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge_record
-- ----------------------------
INSERT INTO `recharge_record` VALUES (17, 16, 'RC17632733344781034', 10.00, 'bank', 1, '2025-11-16 14:08:57', '2025-11-16 14:08:57');
INSERT INTO `recharge_record` VALUES (18, 16, 'RC17632739760887836', 10.00, 'alipay', 1, '2025-11-16 14:19:38', '2025-11-16 14:19:38');
INSERT INTO `recharge_record` VALUES (19, 16, 'RC17632741209487826', 10.00, 'bank', 1, '2025-11-16 14:22:03', '2025-11-16 14:22:03');
INSERT INTO `recharge_record` VALUES (20, 16, 'RC17632771775714547', 10.00, 'alipay', 1, '2025-11-16 15:12:59', '2025-11-16 15:12:59');
INSERT INTO `recharge_record` VALUES (21, 16, 'RC17632778881292179', 50.00, 'alipay', 1, '2025-11-16 15:24:50', '2025-11-16 15:24:50');
INSERT INTO `recharge_record` VALUES (22, 16, 'RC1763278229247486', 100.00, 'alipay', 1, '2025-11-16 15:30:31', '2025-11-16 15:30:31');
INSERT INTO `recharge_record` VALUES (23, 1, 'RC17635364466836742', 100.00, 'wechat', 1, '2025-11-19 15:14:09', '2025-11-19 15:14:09');
INSERT INTO `recharge_record` VALUES (24, 16, 'RC17636175172281109', 50.00, 'alipay', 1, '2025-11-20 13:45:19', '2025-11-20 13:45:19');
INSERT INTO `recharge_record` VALUES (25, 15, 'RC17637049040174050', 50.00, 'wechat', 1, '2025-11-21 14:01:46', '2025-11-21 14:01:46');
INSERT INTO `recharge_record` VALUES (26, 16, 'RC17638778268986593', 500.00, 'alipay', 1, '2025-11-23 14:03:49', '2025-11-23 14:03:49');
INSERT INTO `recharge_record` VALUES (27, 16, 'RC17638778327248518', 200.00, 'alipay', 1, '2025-11-23 14:03:54', '2025-11-23 14:03:54');
INSERT INTO `recharge_record` VALUES (28, 16, 'RC17638786804172432', 10.00, 'alipay', 1, '2025-11-23 14:18:03', '2025-11-23 14:18:03');
INSERT INTO `recharge_record` VALUES (29, 1, 'RC17642334470022331', 10.00, 'bank', 1, '2025-11-27 16:50:49', '2025-11-27 16:50:49');
INSERT INTO `recharge_record` VALUES (30, 16, 'RC17646553022261631', 10.00, 'alipay', 1, '2025-12-02 14:01:44', '2025-12-02 14:01:44');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `realname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `createtime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '12345', '12345', '张三', '12345678901', 80.00, '2025-10-27 09:38:00');
INSERT INTO `user` VALUES (15, '1233', '123', '王四', '19154788000', 35.00, '2025-11-03 10:44:54');
INSERT INTO `user` VALUES (16, '321', '222222', '王五', '19122223333', 50.00, '2025-11-03 11:30:41');
INSERT INTO `user` VALUES (19, '1211', '123', '李四', '19112321232', 0.00, '2025-11-21 15:37:58');

SET FOREIGN_KEY_CHECKS = 1;
