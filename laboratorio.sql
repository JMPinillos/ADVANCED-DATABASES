create table carburantes
(
    id     int auto_increment
        primary key,
    nombre varchar(150) not null
);

create table operadoras
(
    id     int auto_increment
        primary key,
    nombre varchar(150) null
);

create table provincias
(
    nombre varchar(150) not null,
    id     int auto_increment
        primary key
)
    comment 'Colección de las provincias';

create table municipios
(
    id      int auto_increment
        primary key,
    prov_id int          null,
    nombre  varchar(150) not null,
    constraint municipios_provincias_id_fk
        foreign key (prov_id) references provincias (id)
);

create table localidades
(
    id     int auto_increment
        primary key,
    mun_id int          not null,
    nombre varchar(150) not null,
    constraint localidades_municipios_id_fk
        foreign key (mun_id) references municipios (id)
);

create table estaciones
(
    id           int auto_increment
        primary key,
    nombre       varchar(150)                     null,
    codigopostal varchar(5)                       null,
    latitud      float                            null,
    longitud     float                            null,
    loc_id       int                              null,
    margen       enum ('I', 'D', 'N') default 'N' not null,
    direccion    varchar(250)                     not null,
    fechaprecios datetime                         null,
    horario      varchar(250)                     not null,
    ope_id       int                              not null,
    constraint estaciones_localidades_id_fk
        foreign key (loc_id) references localidades (id),
    constraint estaciones_operadoras_id_fk
        foreign key (ope_id) references operadoras (id)
);

create table precios
(
    est_id   int    not null,
    carbu_id int    not null,
    precio   double null,
    primary key (est_id, carbu_id),
    constraint precios_carburantes_id_fk
        foreign key (carbu_id) references carburantes (id),
    constraint precios_estaciones_id_fk
        foreign key (est_id) references estaciones (id)
);


