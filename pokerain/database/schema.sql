-- ============================================================
--  Pokerain – Schema MySQL (referinta)
--  Rulat automat la startup via DatabaseInitializer.java
-- ============================================================

CREATE DATABASE IF NOT EXISTS pokerain_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pokerain_db;

-- ── Traineri ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS trainers (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL UNIQUE,
    money INT          NOT NULL DEFAULT 0
);

-- ── Mutari (moves) ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS moves (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    power       INT,
    accuracy    INT,
    pp          INT,
    max_pp      INT,
    type        VARCHAR(50),
    category    VARCHAR(50),
    side_effect VARCHAR(50)
);

-- ── Creaturi ale trainerilor ────────────────────────────────
CREATE TABLE IF NOT EXISTS trainer_creatures (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    trainer_id    INT          NOT NULL,
    name          VARCHAR(100),
    nickname      VARCHAR(100),
    max_hp        INT,
    attack        INT,
    defense       INT,
    speed         INT,
    level         INT,
    experience    INT     DEFAULT 0,
    loyalty       INT     DEFAULT 70,
    type          VARCHAR(50),
    status_effect VARCHAR(50) DEFAULT 'NONE',
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE
);

-- ── Creaturi salbatice ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS wild_creatures (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    max_hp     INT,
    attack     INT,
    defense    INT,
    speed      INT,
    level      INT,
    type       VARCHAR(50),
    catch_rate DOUBLE,
    is_caught  BOOLEAN DEFAULT FALSE
);
