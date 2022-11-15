--
-- worker_node
drop table if exists worker_node;
create table worker_node
(
    id              bigint      not null primary key auto_increment comment 'id',
    instance        varchar(64) not null comment '服务名',
    pid             varchar(8)  null comment 'pid',
    active_profiles varchar(64) null comment '环境',
    host_name       varchar(64) not null comment 'host name',
    port            varchar(64) not null comment 'port',
    type            tinyint     not null comment '节点类型：1-容器（docker 等），2-真实机器',
    create_time     timestamp   not null default current_timestamp comment '创建日期',
    update_time     timestamp   not null default current_timestamp on update current_timestamp comment '更新日期'
)
    engine = INNODB, comment = 'DB WorkerID Assigner for UID Generator';