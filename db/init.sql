-- ========================================
-- Database Initialization Script
-- Login System - MySQL 8.0
-- ========================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS login_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE login_db;

-- ========================================
-- User Table
-- ========================================
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
    username VARCHAR(50) NOT NULL COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password (encrypted)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT 'Nickname',
    email VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    avatar VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    status TINYINT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- ========================================
-- Initial Data
-- ========================================

-- Insert test accounts
-- Password: 123456 (MD5 encrypted: "123456" + "login_system_salt_2024")
-- MD5("123456login_system_salt_2024") = 384e49109f743d5eac4442473efb5e25
INSERT INTO t_user (username, password, nickname, email, status) VALUES
('admin', '384e49109f743d5eac4442473efb5e25', 'Administrator', 'admin@example.com', 1),
('testuser', '384e49109f743d5eac4442473efb5e25', 'Test User', 'test@example.com', 1);

-- ========================================
-- Verification Code Table
-- ========================================
DROP TABLE IF EXISTS t_verification_code;

CREATE TABLE t_verification_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    email VARCHAR(100) NOT NULL COMMENT 'Email address',
    code VARCHAR(6) NOT NULL COMMENT 'Verification code (6 digits)',
    type VARCHAR(20) NOT NULL DEFAULT 'PASSWORD_RESET' COMMENT 'Code type: PASSWORD_RESET',
    expire_time DATETIME NOT NULL COMMENT 'Expiration time',
    used TINYINT DEFAULT 0 COMMENT 'Used status: 0-not used, 1-used',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    INDEX idx_email (email),
    INDEX idx_code (code),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Verification code table';

-- ========================================
-- Verify Data
-- ========================================
SELECT id, username, nickname, email, status, create_time FROM t_user;
