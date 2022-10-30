
create table violationTracker.file_exclude
(
    id           int auto_increment
        primary key,
    file_path    varchar(512) not null comment '忽略的文件路径',
    repo_uuid    char(36)     not null comment '仓库id',
    create_time  datetime     not null comment '创建时间',
    account_uuid char(36)     not null comment '设置这一忽略文件路径的账户id'
)
    comment '对用户指定路径文件的忽略' engine = MyISAM
                           charset = utf8mb4;

create table violationTracker.issue
(
    id                int auto_increment
        primary key,
    type              varchar(512)                  null,
    tool              varchar(45)                   null,
    start_commit      varchar(64)                   null,
    start_commit_date datetime                      null,
    end_commit        varchar(64)                   null,
    end_commit_date   datetime                      null,
    repo_uuid         char(36)                      not null,
    file_name         varchar(512)                  null,
    create_time       datetime                      null,
    update_time       datetime                      null,
    priority          tinyint                       null,
    status            varchar(20)                   null,
    manual_status     varchar(45) default 'Default' null,
    resolution        varchar(20)                   null,
    issue_category    varchar(50)                   null,
    producer          varchar(64)                   null,
    solver            varchar(64)                   null,
    solve_commit      varchar(64)                   null,
    solve_commit_date datetime                      null,
    uuid              char(36)                      not null
)
    charset = utf8mb4;

create table violationTracker.issue_repo
(
    id                   int auto_increment
        primary key,
    repo_uuid            varchar(36) not null,
    branch               varchar(32) not null,
    tool                 varchar(16) not null,
    status               varchar(16) not null,
    scanned_commit_count int         not null,
    scan_time            mediumtext  null,
    total_commit_count   int         not null,
    start_commit         varchar(64) not null,
    start_scan_time      datetime    not null,
    end_scan_time        datetime    null
)
    charset = utf8mb4;

create index issue_repo_repo_uuid_index
    on violationTracker.issue_repo (repo_uuid);

create table violationTracker.issue_scan
(
    uuid           varchar(36) not null comment '主键',
    tool           varchar(45) null comment '扫描工具类型',
    start_time     datetime    null comment '扫描开始时间',
    end_time       datetime    null comment '扫描结束时间',
    status         varchar(32) not null comment '扫描完成状态',
    result_summary mediumtext  null comment '扫描结果内容总结',
    repo_uuid      varchar(36) null comment '项目仓库id',
    commit_id      varchar(64) null comment '本次commit id',
    commit_time    datetime    null comment '本次commit时间',
    constraint uuid
        unique (uuid)
)
    charset = utf8mb4;

create index idx_scan_repo_id
    on violationTracker.issue_scan (repo_uuid);

create index idx_scan_tool
    on violationTracker.issue_scan (tool);

alter table violationTracker.issue_scan
    add primary key (uuid);

create table violationTracker.issue_type
(
    uuid                 varchar(36)  not null comment '主键',
    type                 varchar(255) null comment 'issue的具体类型',
    specification_source varchar(36)  null comment '规则来源',
    category             varchar(128) null comment 'issue所属的类别',
    description          mediumtext   null comment 'issue的描述',
    language             varchar(45)  null,
    severity             varchar(45)  null comment 'issue 严重程度',
    status               varchar(32)  null comment 'issue 状态，启用/弃用/测试等',
    constraint uuid
        unique (uuid)
)
    charset = utf8mb4;

alter table violationTracker.issue_type
    add primary key (uuid);

create table violationTracker.location
(
    id            int auto_increment
        primary key,
    uuid          varchar(36)   not null comment '主键',
    start_line    mediumint     null comment 'bug所在上下文的开始行',
    end_line      mediumint     null comment 'bug所在上下文的结尾行',
    bug_lines     varchar(4096) null comment '表示这个bug在文件中具体体现在哪些行',
    start_token   mediumint     null,
    end_token     mediumint     null,
    file_name     varchar(512)  not null comment 'bug所在文件路径',
    class_name    varchar(256)  null comment 'bug所在类名',
    method_name   text          null comment 'bug所在方法名',
    rawIssue_uuid varchar(36)   not null comment 'bug所属rawissueID',
    code          text          null comment 'bug源代码',
    offset        int default 0 not null,
    repo_uuid     varchar(36)   null
)
    charset = utf8mb4;

create index raw_issue_id_index
    on violationTracker.location (rawIssue_uuid);

create table violationTracker.raw_issue
(
    id             int auto_increment
        primary key,
    uuid           varchar(36)  not null comment '主键',
    type           varchar(512) not null comment '缺陷类型',
    tool           varchar(45)  null comment 'rawissue类别',
    detail         mediumtext   null,
    file_name      varchar(512) null comment 'rawissue文件名',
    scan_uuid      varchar(36)  not null comment 'rawissue扫描id',
    issue_uuid     varchar(36)  null,
    commit_id      varchar(64)  not null comment '本次commit id',
    repo_uuid      varchar(36)  not null comment 'rawissue所属仓库id',
    code_lines     int          null,
    developer      varchar(64)  null,
    version        int          not null,
    raw_issue_hash varchar(36)  null
)
    charset = utf8mb4;

create index idx_category_repoId
    on violationTracker.raw_issue (tool, repo_uuid);

create index idx_rawIssue_category
    on violationTracker.raw_issue (tool);

create index idx_rawIssue_issue_id
    on violationTracker.raw_issue (issue_uuid);

create index idx_rawIssue_repo_id
    on violationTracker.raw_issue (repo_uuid);

create index idx_rawIssue_repo_id_category
    on violationTracker.raw_issue (repo_uuid, tool);

create index idx_uuid_commit_status
    on violationTracker.raw_issue (uuid, tool, repo_uuid);

create table violationTracker.raw_issue_cache
(
    id             int auto_increment
        primary key,
    repo_uuid      char(36)                        not null,
    commit_id      varchar(64)                     not null,
    analyze_result longtext                        null,
    invoke_result  tinyint     default 1           null,
    tool           varchar(32) default 'sonarqube' not null,
    raw_issue_num  int         default 0           null
)
    engine = MyISAM
    charset = utf8mb4;

create index idx_repo_uuid_commit_id
    on violationTracker.raw_issue_cache (repo_uuid, commit_id);

create table violationTracker.raw_issue_match_info
(
    id                int auto_increment
        primary key,
    cur_rawIssue_uuid varchar(36)  null,
    cur_commit_id     varchar(64)  null,
    pre_rawIssue_uuid varchar(36)  null,
    pre_commit_id     varchar(64)  null,
    issue_uuid        varchar(36)  null,
    status            varchar(64)  null,
    repo_uuid         varchar(36)  null,
    solve_way         varchar(256) null
);

create index raw_issue_match_info_issue_uuid_index
    on violationTracker.raw_issue_match_info (issue_uuid);

create index raw_issue_match_info_repo_uuid_index
    on violationTracker.raw_issue_match_info (repo_uuid);

create table violationTracker.scan_result
(
    id               int auto_increment comment '主键
'
        primary key,
    category         varchar(45)                            not null comment '扫描结果类型',
    repo_uuid        varchar(36)                            not null comment '扫描的repo文件id',
    scan_date        date                                   not null comment '扫描日期',
    commit_id        varchar(64)                            not null comment '本次commit id',
    commit_date      datetime                               not null comment '本次commit时间',
    developer        varchar(64) collate utf8mb4_unicode_ci null comment '本次commit的提交者',
    new_count        int(10)     default 0                  null comment '新增缺陷总数',
    eliminated_count int(10)     default 0                  null comment '消除缺陷总数',
    remaining_count  int         default 0                  null comment '剩余缺陷总数 ',
    parent_commit_id varchar(64) default 'empty'            null,
    reopen_count     int         default 0                  null
)
    charset = latin1;

create table violationTracker.solved_record
(
    id         int auto_increment
        primary key,
    repo_uuid  char(36)                   not null,
    match_id   int                        not null,
    issue_uuid char(36) charset utf8      not null,
    type       varchar(64) default 'null' null
)
    engine = MyISAM
    collate = utf8mb4_unicode_ci;

create index idx_repo_uuid_type
    on violationTracker.solved_record (repo_uuid, type);

