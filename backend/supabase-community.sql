-- Esquema completo (base + comunidad) para Supabase/Postgres.
-- Puedes ejecutarlo completo en SQL Editor sin romper datos existentes.

create extension if not exists pgcrypto;

create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  email text not null unique,
  password_hash text not null,
  created_at timestamptz not null default now()
);

create table if not exists routines (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  name text not null,
  exercise_ids text[] not null default '{}',
  updated_at timestamptz not null default now()
);

create table if not exists progress (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  date_iso text not null,
  weight double precision not null,
  note text,
  created_at timestamptz not null default now()
);

create index if not exists idx_routines_user_id on routines(user_id);
create index if not exists idx_progress_user_id on progress(user_id);

create table if not exists public_routines (
  id uuid primary key default gen_random_uuid(),
  routine_id uuid not null,
  owner_user_id uuid not null,
  owner_name text not null,
  name text not null,
  exercise_ids text[] not null default '{}',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (routine_id, owner_user_id)
);

create table if not exists public_routine_favorites (
  id uuid primary key default gen_random_uuid(),
  public_routine_id uuid not null references public_routines(id) on delete cascade,
  user_id uuid not null,
  created_at timestamptz not null default now(),
  unique (public_routine_id, user_id)
);

create index if not exists idx_public_routines_updated_at on public_routines(updated_at desc);
create index if not exists idx_public_routine_favorites_routine on public_routine_favorites(public_routine_id);
create index if not exists idx_public_routine_favorites_user on public_routine_favorites(user_id);

-- Asegura FKs aunque las tablas ya existan (Postgres no soporta ADD CONSTRAINT IF NOT EXISTS directo).
do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_public_routines_routine'
  ) then
    alter table public_routines
      add constraint fk_public_routines_routine
      foreign key (routine_id) references routines(id) on delete cascade;
  end if;
end $$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_public_routines_owner'
  ) then
    alter table public_routines
      add constraint fk_public_routines_owner
      foreign key (owner_user_id) references users(id) on delete cascade;
  end if;
end $$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_public_routine_favorites_user'
  ) then
    alter table public_routine_favorites
      add constraint fk_public_routine_favorites_user
      foreign key (user_id) references users(id) on delete cascade;
  end if;
end $$;
