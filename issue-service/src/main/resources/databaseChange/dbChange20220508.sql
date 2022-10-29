#
荣耀5月版本更新
alter table raw_issue_match_info
    add solve_way varchar(256);
alter table raw_issue
    add raw_issue_hash varchar(36);