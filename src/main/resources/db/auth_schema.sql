CREATE TABLE users_auth (
    user_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    username       VARCHAR(100) NOT NULL UNIQUE,
    role           ENUM ('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    status         ENUM ('ACTIVE', 'LOCKED', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT NOT NULL DEFAULT 0,
    last_login     TIMESTAMP NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE password_history (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users_auth(user_id) ON DELETE CASCADE
);

-- Performance indexes (username already has UNIQUE index, but adding explicit index for clarity)
CREATE INDEX idx_password_history_user ON password_history(user_id, created_at);

