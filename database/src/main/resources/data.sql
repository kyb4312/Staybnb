-- 초기 데이터 insert
-- user
insert into user (user_id, name, email, password)
values ('admin', 'admin', 'admin@gmail.com', '2345');

-- currency
insert into currency (code, name, symbol, exchange_rate)
values ("KRW", "Korean won", "₩", 1350);
insert into currency (code, name, symbol, exchange_rate)
values ("USD", "US Dollar", "$", 1);
insert into currency (code, name, symbol, exchange_rate)
values ("EUR", "European Euro", "€", 0.87497);

-- place_type
insert into place_type (name) values ('house');
insert into place_type (name) values ('apartment');
insert into place_type (name) values ('hotel');
insert into place_type (name) values ('guest house');

-- amenity
insert into amenity (name) values ('wifi');
insert into amenity (name) values ('tv');
insert into amenity (name) values ('kitchen');
insert into amenity (name) values ('air conditioner');
