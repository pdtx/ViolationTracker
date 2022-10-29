#
msg:  基于MyISAM 缓存 sonarqube数据   表结构创建和插入读取速度测试
create table `raw_issue_cache`
(
    id             int auto_increment,
    repo_uuid      char(36)                        not null,
    commit_id      varchar(64)                     not null,
    analyze_result longtext null,
    invoke_result  tinyint     default 1 null,
    tool           varchar(32) default 'sonarqube' not null,
    constraint `raw_issue_ cache_pk`
        primary key (id)
) ENGINE = MyISAM
  CHARSET = utf8mb4;

insert into raw_issue_cache
    (repo_uuid, commit_id, analyze_result, invoke_result, tool)
select repo_uuid, commit_id, analyze_result, invoke_result, tool
from issue_analyzer;

alter table raw_issue_cache
    add index idx_repo_uuid_commit_id (repo_uuid, commit_id);

drop table issue_analyzer;