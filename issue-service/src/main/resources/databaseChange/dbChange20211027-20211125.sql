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
    add index idx_repo_uuid_type(repo_uuid, type);
#
data

insert into solved_record(repo_uuid, match_id, issue_uuid)
select i.repo_uuid, r.id, r.issue_uuid
from raw_issue_match_info as r
         inner join issue_scan as i
                    on r.cur_commit_id = i.commit_id
where r.status = 'solved';


ALTER TABLE solved_record CHANGE issue_uuid issue_uuid char (36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;


#
####################### ######################
#
# raw_issue_match_info
#
# ####################### ######################

alter table raw_issue_match_info
    add column repo_uuid char(36) null;

alter table raw_issue_match_info modify cur_rawIssue_uuid char (36) null;

alter table raw_issue_match_info modify cur_commit_id char (40) null;

alter table raw_issue_match_info modify pre_rawIssue_uuid char (36) null;

alter table raw_issue_match_info modify pre_commit_id char (40) null;

alter table raw_issue_match_info modify issue_uuid char (36) null;


alter table raw_issue_match_info
    add index idx_issue_uuid_cur_commit_id(issue_uuid, cur_commit_id);

#
UPDATE raw_issue_match_info r
    #
SET repo_uuid = (
    # SELECT repo_uuid
    # FROM commit
    # WHERE commit_id = r.cur_commit_id);

#
####################### ######################
#
# raw_issue_match_info
#
# ####################### ######################



# ####################### ######################
#
# location
#
# ####################### ######################


alter table location
    add column repo_uuid char(36) null;
alter table location
    add index idx_repo_uuid(repo_uuid);

#
####################### ######################
#
# location
#
# ####################### ######################


# ####################### ######################
#
# issue
#
# ####################### ######################

alter table issue modify repo_uuid char (36) not null;

alter table issue modify uuid char (36) not null;

#
####################### ######################
#
# issue
#
# ####################### ######################



# ####################### ######################
#
# scan_status
#
# ####################### ######################
alter table scan_result modify commit_date datetime not null comment '本次commit时间';

alter table scan_result modify new_count INT UNSIGNED default 0 null comment '新增缺陷总数';

alter table scan_result modify eliminated_count INT UNSIGNED default 0 null comment '消除缺陷总数';

alter table scan_result modify remaining_count INT UNSIGNED default 0 null comment '剩余缺陷总数 ';

#
####################### ######################
#
# scan_status
#
# ####################### ######################