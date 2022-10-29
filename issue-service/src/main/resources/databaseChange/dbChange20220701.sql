alter table location drop primary key;
alter table location drop index uuid;
alter table location
    add id int auto_increment primary key first;