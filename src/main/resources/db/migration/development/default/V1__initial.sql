-- create session
-- H2 Database compatible
create table skinny_sessions (
  id bigserial not null primary key,
  created_at timestamp not null,
  expire_at timestamp not null
);
create table servlet_sessions (
  jsession_id varchar(100) not null primary key,
  skinny_session_id bigint not null,
  created_at timestamp not null,
  foreign key(skinny_session_id) references skinny_sessions(id)
);
create table skinny_session_attributes (
  skinny_session_id bigint not null,
  attribute_name varchar(128) not null,
  attribute_value bytea,
  foreign key(skinny_session_id) references skinny_sessions(id)
);
alter table skinny_session_attributes add constraint
  skinny_session_attributes_unique_idx
  unique(skinny_session_id, attribute_name);


-- create t_member
create table t_member (
  member_id serial primary key,
  email_address varchar(250) not null,
  password varchar(100) not null,
  is_admin char not null default '0',
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

create unique index member_IX1 on t_member(email_address);

-- create t_group
create table t_group (
  group_id serial primary key,
  group_name varchar(250) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

-- create r_member_group
create table r_member_group (
  member_id integer not null,
  group_id integer not null,
  primary key (member_id, group_id)
);

-- create t_function
create table t_function (
  function_id integer primary key,
  function_name varchar(250) not null,
  created_at timestamp not null,
  updated_at timestamp not null
);

-- create r_group_function
create r_group_function (
  group_id integer not null,
  function_id integer not null
);

-- create t_report
create table t_report (
  report_id serial primary key,
  report_name varchar(250) not null,
  template_path varchar(250) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

-- create t_scheduled_report
create table t_scheduled_report (
  scheduled_id serial primary key,
  report_id integer not null,
  cron_expression varchar(250) not null,
  params bytea,
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

-- create t_report_group
create table t_report_group (
  report_group_id serial primary key,
  report_group_name varchar(250) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

-- create r_report_report_group
create table r_report_report_group (
  report_id integer not null,
  report_group_id integer not null,
  primary key (report_id, report_group_id)
);

-- create r_group_report_group
create table r_group_report_group (
  group_id integer not null,
  report_group_id integer not null,
  primary key (group_id, report_group_id)
);

-- create t_report_param
create table t_report_param (
  param_id serial primary key,
  param_name varchar(250) not null,
  param_type char not null default '0',
  created_at timestamp not null,
  updated_at timestamp not null,
  versions bigint not null default 0
);

-- create r_report_report_param
create table r_report_report_param (
  report_id integer not null,
  param_id integer not null,
  primary key (report_id, param_id)
);

insert into t_group (group_name, created_at, updated_at) values ('admin', now(), now());
insert into t_member (email_address, password, is_admin, created_at, updated_at) values ('ijufumi@gmail.com', 'password', '1', now(), now());
insert into r_member_group values (1, 1);

insert into t_report (report_name, template_path, created_at, updated_at) values ('テストレポート', 'sample.xlsx', now(), now());
insert into t_report_group (report_group_name, created_at, updated_at) values ('テストグループ', now(), now());
insert into r_report_report_group values (1, 1);
insert into r_group_report_group values (1, 1);