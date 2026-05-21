CREATE TABLE users (
    id          BIGINT                      NOT NULL,
    username    VARCHAR(80)                 NOT NULL,
    pwd_hash    VARCHAR(200)                NOT NULL,
    email       VARCHAR(100)                NOT NULL,
    stream_key  VARCHAR(200),
    avatar_url  VARCHAR(200),
    active      BOOLEAN                     NOT NULL DEFAULT TRUE,
    verified    BOOLEAN                     NOT NULL DEFAULT FALSE,
    token_ver   BIGINT                      NOT NULL DEFAULT 0,
    deleted     BOOLEAN                     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_users_username ON users (username);
CREATE UNIQUE INDEX uq_users_email    ON users (email);
