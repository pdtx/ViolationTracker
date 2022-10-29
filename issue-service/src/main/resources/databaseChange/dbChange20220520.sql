#荣耀5月版本更新
alter table raw_issue drop primary key;
alter table raw_issue drop index uuid;
alter table raw_issue
    add id int auto_increment primary key first;
