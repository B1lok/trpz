-- Table: users
CREATE TABLE IF NOT EXISTS users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

-- Table: global_limits
CREATE TABLE IF NOT EXISTS global_limits
(
    id              SERIAL PRIMARY KEY,
    max_connections INT       NOT NULL,
    max_speed       INT       NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    updated_by      INT       REFERENCES users (id) ON DELETE SET NULL
);


-- Table: groups
CREATE TABLE IF NOT EXISTS groups
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Table: user_group (many-to-many between users and groups)
CREATE TABLE IF NOT EXISTS user_group
(
    user_id  INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    group_id INT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

-- Table: directories
CREATE TABLE IF NOT EXISTS directories
(
    id         SERIAL PRIMARY KEY,
    path       VARCHAR(255) NOT NULL UNIQUE,
    parent_id  INT          REFERENCES directories (id) ON DELETE CASCADE ,
    owner_id   INT          REFERENCES users (id) ON DELETE SET NULL ,
    group_id   INT          REFERENCES groups (id) ON DELETE SET NULL,
    permission INT          NOT NULL
);

-- Table: files
CREATE TABLE IF NOT EXISTS files
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    directory_id INT          NOT NULL REFERENCES directories (id) ON DELETE CASCADE,
    owner_id     INT          REFERENCES users (id) ON DELETE SET NULL ,
    group_id     INT          REFERENCES groups (id) ON DELETE SET NULL,
    permission   INT          NOT NULL
);

-- Table: connection_statistics
CREATE TABLE IF NOT EXISTS connection_statistics
(
    id                 SERIAL PRIMARY KEY,
    connection_time    TIMESTAMP NOT NULL,
    disconnection_time TIMESTAMP,
    user_id            INT       NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

-- Table: connection_limits
CREATE TABLE IF NOT EXISTS connection_limits
(
    id              SERIAL PRIMARY KEY,
    max_connections INT NOT NULL,
    max_speed       INT NOT NULL,
    user_id         INT NOT NULL REFERENCES users (id) ON DELETE CASCADE
);