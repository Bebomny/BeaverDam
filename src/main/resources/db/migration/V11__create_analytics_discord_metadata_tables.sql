CREATE SCHEMA IF NOT EXISTS "analytics";

CREATE TABLE "analytics"."discord_guilds"
(
    "guild_id"   bigint       PRIMARY KEY,
    "name"       varchar(255) NOT NULL,
    "icon_url"   varchar(512),
    "updated_at" timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "analytics"."discord_users"
(
    "user_id"    bigint       PRIMARY KEY,
    "username"   varchar(100) NOT NULL,
    "avatar_url" varchar(512),
    "updated_at" timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
