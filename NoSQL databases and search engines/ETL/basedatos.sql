create schema laboratorio01 collate utf8mb4_0900_ai_ci;

use laboratorio01;

create table carburantes
(
    id     varchar(36)  not null
        primary key,
    nombre varchar(150) not null
);

create index idx_carburante_nombre
    on carburantes (nombre);

create table provincias
(
    id     varchar(36)  not null
        primary key,
    nombre varchar(150) not null
)
    comment 'Colección de las provincias';

create table municipios
(
    id           varchar(36)  not null
        primary key,
    provincia_id varchar(36)  null,
    nombre       varchar(150) not null,
    constraint municipios_provincias_id_fk
        foreign key (provincia_id) references provincias (id)
);

create table localidades
(
    id           varchar(36)  not null
        primary key,
    municipio_id varchar(36)  not null,
    nombre       varchar(150) not null,
    constraint localidades_municipios_id_fk
        foreign key (municipio_id) references municipios (id)
);

create index idx_localidades_nombre
    on localidades (nombre);

create index idx_provincia_nombre
    on provincias (nombre);

create table rotulos
(
    id     varchar(36)  not null
        primary key,
    nombre varchar(250) not null
);

create table tipo_ventas
(
    id     varchar(36)  not null
        primary key,
    nombre varchar(150) null
);

create table estaciones
(
    id           varchar(36)                      not null
        primary key,
    codigopostal varchar(5)                       null,
    latitud      decimal(10, 6)                   null,
    longitud     decimal(10, 6)                   null,
    localidad_id varchar(36)                      null,
    margen       enum ('I', 'D', 'N') default 'N' not null,
    direccion    varchar(250)                     not null,
    fechaprecios datetime                         null,
    horario      varchar(250)                     not null,
    rotulo_id    varchar(36)                      null,
    tipoestacion enum ('Terrestre', 'Maritima')   null,
    tipoventa_id varchar(36)                      not null,
    constraint estaciones_localidades_id_fk
        foreign key (localidad_id) references localidades (id),
    constraint estaciones_rotulos_id_fk
        foreign key (rotulo_id) references rotulos (id),
    constraint estaciones_tipo_ventas_id_fk
        foreign key (tipoventa_id) references tipo_ventas (id)
);

create index idx_estaciones_codigopostal_margen
    on estaciones (codigopostal, margen);

create index idx_localidad_rotulo
    on estaciones (localidad_id, rotulo_id);

create index idx_tipoestacion
    on estaciones (tipoestacion);

create index idx_tipoestacion_rotuloid
    on estaciones (tipoestacion, rotulo_id);

create table precios
(
    estacion_id   varchar(36) not null,
    carburante_id varchar(36) not null,
    precio        double      null,
    primary key (estacion_id, carburante_id),
    constraint precios_carburantes_id_fk
        foreign key (carburante_id) references carburantes (id),
    constraint precios_estaciones_id_fk
        foreign key (estacion_id) references estaciones (id)
);