--liquibase formatted sql

--changeset steshabolk:create-chat-table
CREATE TABLE IF NOT EXISTS chats
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL
);

--changeset steshabolk:create-link-table
CREATE TABLE IF NOT EXISTS links
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link_type  SMALLINT                               NOT NULL,
    url        VARCHAR(255) UNIQUE                    NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    status     SMALLINT                               NOT NULL
);

--changeset steshabolk:create-chats-links-table
CREATE TABLE IF NOT EXISTS chats_links
(
    chat_id BIGINT REFERENCES chats (id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES links (id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id)
);
