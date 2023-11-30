/* 1. Nombre de la empresa con más estaciones de servicio terrestres */
SELECT count(*) as total, name as empresa FROM operators, stations
WHERE stations.op_id=operators.op_id and
type = 'T' GROUP BY empresa
ORDER BY total DESC LIMIT 1

/* 2. Nombre de la empresa con más estaciones de servicio marítimas */
SELECT count(*) as total, name as empresa FROM operators, stations
WHERE stations.op_id=operators.op_id and
type = 'M' GROUP BY empresa
ORDER BY total DESC LIMIT 1

/* 3. Localización, nombre de empresa y margen de la estación con el precio más bajo para el combustible «Gasolina 95 E5» en la Comunidad de Madrid */
SELECT longitude as longitud, latitude as latitud, operators.name as empresa, margin as margen
FROM stations, operators, fuels, prices, localities
WHERE operators.op_id = stations.op_id AND
stations.loc_id = localities.loc_id AND stations.st_id = prices.st_id AND fuels.fuel_id = prices.fuel_id AND
localities.name = 'MADRID' AND
fuels.name = 'Gasolina 95 E5' ORDER BY amount ASC
LIMIT 1

/* 4. Localización, nombre de empresa y margen de la estación con el precio más bajo para el combustible «Gasóleo A» si resido en el centro de Albacete y no quiero desplazarme más de 10 KM */
SELECT stations.longitude as longitud, stations.latitude as latitud, operators.name as empresa, stations.margin as margen, amount
FROM stations, operators, fuels, prices
WHERE stations.st_id = operators.op_id AND stations.st_id = prices.st_id AND fuels.fuel_id = prices.fuel_id AND fuels.name = 'Gasoleo A' AND
(6371 * acos(cos(radians(38.9942400)) * cos(radians(stations.latitude)) * cos(radians(stations.longitude) - radians(-1.8564300)) + sin(radians(38.9942400)) * sin(radians(stations.latitude)))) <= 10
ORDER BY (6371 * acos(cos(radians(38.9942400)) * cos(radians(stations.latitude)) * cos(radians(stations.longitude) - radians(-1.8564300)) + sin(radians(38.9942400)) * sin(radians(stations.latitude)))) ASC
LIMIT 1

/* 5. Provincia en la que se encuentre la estación de servicio marítima con el combustible «Gasolina 95 E5» más caro */
SELECT provinces.name as nombre
FROM stations, provinces, municipalities, localities, fuels, prices
WHERE
stations.loc_id = localities.loc_id AND localities.mun_id = municipalities.mun_id AND municipalities.pro_id = provinces.pro_id AND stations.st_id = prices.st_id AND fuels.fuel_id = prices.fuel_id AND
fuels.name = 'Gasolina 95 E5' AND stations.type = 'M'
ORDER BY amount DESC LIMIT 1