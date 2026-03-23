CREATE SCHEMA IF NOT EXISTS "docker";

CREATE TABLE "docker"."container_configs" (
    "container_name"      varchar(255) primary key,
    "command_strategy_id" varchar(255),
    "run_as_user"         varchar(255),
    "auto_attach_on_start" boolean
);