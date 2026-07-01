-- V1_init_schema.sql
-- =============================================================
-- Author      : Dario
-- Created at  : 2026-06-28
-- Database    : robofleet_db
-- Schema      : IAM
-- Description : Initial schema for identity service (role, account)
-- =============================================================
CREATE SCHEMA IF NOT EXISTS IAM;

CREATE TABLE iam.role (
    id        BIGSERIAL PRIMARY KEY,
    role      VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO iam.role (role) VALUES ('admin'), ('operator'), ('customer');

CREATE TABLE iam.account (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    email      VARCHAR(30) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN DEFAULT TRUE,
    role_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at  TIMESTAMP,
    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES iam.role(id)
);

CREATE TABLE iam.refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL REFERENCES iam.account(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_account_id ON iam.refresh_token(account_id);
CREATE INDEX idx_refresh_token_token ON iam.refresh_token(token);