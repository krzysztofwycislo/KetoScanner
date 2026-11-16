create schema products;

create schema app;

create table app.install (
    id uuid primary key,
    created_at timestamptz not null default now(),
    last_seen_at timestamptz not null default now(),
    app_version_code integer not null
);
