-- 초기 데이터 insert
-- user
insert into user (name, email, password)
values ('admin', 'admin@gmail.com', '2345');

-- currency
insert into currency (code, name, exchange_rate)
values ("KRW", "Korean won", 1350);
insert into currency (code, name, exchange_rate)
values ("USD", "US Dollar", 1);
insert into currency (code, name, exchange_rate)
values ("EUR", "European Euro", 0.87497);

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

-- room
insert into room (host_id, place_type_id, room_type, country, city, street, max_number_of_guests, bedrooms, beds, title,
                  description, currency_code, base_price, base_price_in_usd, is_deleted)
values (1, 1, 'ENTIRE_PLACE', 'South Korea', 'city', 'street', 2, 2, 2, 'title', 'description', 'KRW', 300000, 0,
        false);