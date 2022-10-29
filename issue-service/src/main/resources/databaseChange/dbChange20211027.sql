#
table structure
use issueTracker;
create table solved_record
(
    id         int auto_increment,
    repo_uuid  char(36) not null,
    match_id   int      not null,
    issue_uuid char(36) not null,
    type       varchar(64) default 'null' null,
    constraint solved_record_pk
        primary key (id)
) engine = MyISAM;

alter table solved_record
    add index idx_repo_uuid_type (repo_uuid, type);
#
data

insert into solved_record(repo_uuid, match_id, issue_uuid)
select i.repo_uuid, r.id, r.issue_uuid
from raw_issue_match_info as r
         inner join issue_scan as i
                    on r.cur_commit_id = i.commit_id
where r.status = "solved";