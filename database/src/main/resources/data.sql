-- 초기 데이터 insert
-- user
insert into "user" (name, email, password)
values ('admin', 'admin@gmail.com', '2345');
insert into "user" (name, email, password)
values ('guest1', 'guest1@gmail.com', '2345');

-- exchange_rate
insert into exchange_rate (currency, rate)
values ('KRW', 1350);
insert into exchange_rate (currency, rate)
values ('USD', 1);

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
                  description, currency, base_price, base_price_in_usd, is_deleted)
values (2, 1, 'ENTIRE_PLACE', 'South Korea', 'city', 'street', 2, 2, 2, 'title', 'description', 'KRW', 300000, 0,
        false);

insert into room (host_id, place_type_id, room_type, country, city, street, max_number_of_guests, bedrooms, beds, title,
                  description, currency, base_price, base_price_in_usd, is_deleted)
values (1, 1, 'ENTIRE_PLACE', 'South Korea', 'city', 'street', 2, 2, 2, 'title', 'description', 'KRW', 300000, 0,
        false);

-- booking
insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE, current_DATE + 2), 2, 550000, 'KRW', 'REQUESTED');

insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE + 2, current_DATE + 4), 2, 550000, 'KRW', 'RESERVED');

insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE + 4, current_DATE + 6), 2, 550000, 'KRW', 'CANCELLED');

insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE + 6, current_DATE + 8), 2, 550000, 'KRW', 'REJECTED');

insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE + 8, current_DATE + 10), 2, 550000, 'KRW', 'ENDED');

-- booking (to be updated)
insert into booking (room_id, guest_id, date_range, number_of_guests, booking_price, currency, status)
values (2, 2, daterange(CURRENT_DATE - 2, current_DATE - 1), 2, 550000, 'KRW', 'RESERVED');

-- availability
insert into availability (room_id, date_range, is_available)
values (2, daterange(CURRENT_DATE, CURRENT_DATE + 10, '[]'), false);

insert into availability (room_id, date_range, is_available)
values (1, daterange(CURRENT_DATE, CURRENT_DATE + 30), true);
