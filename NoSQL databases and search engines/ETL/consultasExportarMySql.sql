-- Consulta principal para exportar a MySql
select e.id as estacion_id,
       r.nombre as empresa,
       e.codigopostal, e.latitud, e.longitud, e.localidad_id, e.margen, e.direccion,
       l.nombre as localidad,
       m.nombre as municipio,
       p.nombre as provincia,
       e.fechaprecios, e.horario, e.tipoestacion,
       tv.nombre as tipo_venta
from estaciones e
         inner join localidades l on e.localidad_id = l.id
         inner join municipios m on l.municipio_id = m.id
         inner join provincias p on m.provincia_id = p.id
         inner join rotulos r on e.rotulo_id = r.id
         inner join tipo_ventas tv on e.tipoventa_id = tv.id;

-- Por cada estación, se obtienen los precios de los carburantes
select  c.nombre as carburante,
        pr.precio
from precios pr
         inner join carburantes c on pr.carburante_id = c.id
where pr.estacion_id = ?;