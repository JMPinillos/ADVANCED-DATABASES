CREATE SCHEMA laboratorio_EESS COLLATE utf8b4_0900_ai_ci;

use laboratorio_EESS;

create table fuels
(
    fuel_id int          not null
        primary key,
    name    varchar(150) not null
);

create table operators
(
    op_id int          not null
        primary key,
    name  varchar(150) not null
);

create table provinces
(
    name   varchar(150) not null,
    pro_id int          not null
        primary key
);

create table municipalities
(
    mun_id int          not null
        primary key,
    pro_id int          null,
    name   varchar(150) not null,
    constraint municipalities_provinces_pro_id_fk
        foreign key (pro_id) references provinces (pro_id)
);

create table localities
(
    loc_id int          not null
        primary key,
    mun_id int          not null,
    name   varchar(150) not null,
    constraint localities_municipalities_mun_id_fk
        foreign key (mun_id) references municipalities (mun_id)
);

create table stations
(
    st_id      int                              not null
        primary key,
    cp         varchar(5)                       not null,
    latitude   float                            null,
    longitude  float                            null,
    loc_id     int                              null,
    margin     enum ('I', 'D', 'N') default 'N' not null,
    address    varchar(250)                     not null,
    price_date datetime                         null,
    schedule   varchar(250)                     not null,
    op_id      int                              not null,
    type       enum ('T', 'M')                  not null,
    constraint stations_localities_loc_id_fk
        foreign key (loc_id) references localities (loc_id),
    constraint stations_operators_op_id_fk
        foreign key (op_id) references operators (op_id)
);

create table prices
(
    st_id   int   not null,
    fuel_id int   not null,
    amount  float null,
    primary key (st_id, fuel_id),
    constraint prices_fuels_fuel_id_fk
        foreign key (fuel_id) references fuels (fuel_id),
    constraint prices_stations_st_id_fk
        foreign key (st_id) references stations (st_id)
);


