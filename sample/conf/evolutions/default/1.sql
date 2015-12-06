# --- !Ups

create table users (
    id bigint primary key not null,
    username varchar(255) not null,
    occ_version_number int not null,
    created_by_id bigint not null,
    date_created timestamp not null,
    updated_by_id bigint not null,
    last_update timestamp not null
  );


create sequence "s_users_id";

create unique index "idx_user_username" on "users" ("username");

# --- !Downs

drop table "users";