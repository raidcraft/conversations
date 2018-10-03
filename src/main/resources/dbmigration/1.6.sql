-- apply changes
create table rc_conversation_persistent_hosts (
  id                            integer auto_increment not null,
  host                          varchar(40),
  creator                       varchar(40),
  conversation                  varchar(255),
  host_type                     varchar(255),
  world                         varchar(255),
  x                             integer not null,
  y                             integer not null,
  z                             integer not null,
  constraint pk_rc_conversation_persistent_hosts primary key (id)
);

create table rc_conversation_persistent_host_options (
  id                            integer auto_increment not null,
  conf_key                      varchar(255),
  conf_value                    varchar(255),
  host_id                       integer,
  constraint pk_rc_conversation_persistent_host_options primary key (id)
);

create table rc_conversation_saved_conversations (
  id                            integer auto_increment not null,
  player                        varchar(40),
  host                          varchar(40),
  conversation                  varchar(255),
  stage                         varchar(255),
  timestamp                     datetime(6),
  constraint pk_rc_conversation_saved_conversations primary key (id)
);

create table rc_conversation_player_variables (
  id                            integer auto_increment not null,
  player                        varchar(40) not null,
  conversation                  varchar(255),
  stage                         varchar(255),
  name                          varchar(255) not null,
  value                         varchar(255),
  last_update                   datetime(6),
  constraint uq_rc_conversation_player_variables_player_name unique (player,name),
  constraint pk_rc_conversation_player_variables primary key (id)
);

create index ix_rc_conversation_persistent_host_options_host_id on rc_conversation_persistent_host_options (host_id);
alter table rc_conversation_persistent_host_options add constraint fk_rc_conversation_persistent_host_options_host_id foreign key (host_id) references rc_conversation_persistent_hosts (id) on delete restrict on update restrict;

