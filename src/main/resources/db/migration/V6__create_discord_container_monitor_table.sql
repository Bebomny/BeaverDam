CREATE SCHEMA IF NOT EXISTS "discord";

CREATE TABLE IF NOT EXISTS "discord"."monitored_containers" (
    "container_name" varchar(255) primary key,
    "formatter_type" varchar(255),
    "discord_channel_id" varchar(255)
);