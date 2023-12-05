# Índices en bases de datos

## Introducción

En la evolución constante de las bases de datos, la eficiencia en el acceso y manipulación de datos se ha convertido en un aspecto esencial para garantizar un rendimiento óptimo en los sistemas informáticos. Los índices, estructuras que agilizan la búsqueda y recuperación de información, desempeñan un papel crucial en esta optimización. En esta actividad, profundizaré en la comprensión de los índices en bases de datos, explorando sus tipos y evaluando estrategias para mejorar el rendimiento de las consultas.

En la primera fase, se abordarán conceptos fundamentales, como la diferencia entre índices densos y dispersos. Además, se analizará por qué no es recomendable tener índices para todas las columnas de una tabla, destacando las razones fundamentales detrás de esta decisión.

En la segunda fase, se aplicarán estos conocimientos al esquema relacional desarrollado en la actividad anterior. Investigando los índices creados por defecto tras la construcción del esquema y las tablas.

Posteriormente, se realizará un análisis de rendimiento de las consultas previamente ejecutadas, identificando posibles mejoras mediante la creación estratégica de nuevos índices.

Después, se procederá a implementar estos índices y evaluar el impacto en el rendimiento de las consultas.



## Conceptos fundamentes

En este apartado se tratarán algunos conceptos fundamentales sobre índices.



### Tipos de índices

Los índices en bases de datos se clasifican en diversos tipos, entre ellos, encontramos: **índices ordenados**, **índices asociados** e **índices multinivel**.

En la categoría de índices ordenados, se pueden clasificar según la cantidad de información almacenada en **índices densos** o **índices dispersos**.

Los índices densos mantienen una entrada para cada valor único de la clave de búsqueda en la tabla, lo que significa que cada posible valor tiene su propia entrada en la estructura de índice. Esto resulta en un acceso rápido a los datos, pero puede requerir más espacio de almacenamiento debido a la cantidad de entradas generadas, lo que puede ser un factor limitante en tablas con grandes cantidades de datos.

Por otro lado, los índices dispersos, a diferencia de los densos, no mantienen una entrada para cada valor de la clave de búsqueda. En lugar de eso, almacenan entradas solo para un subconjunto de valores, lo que puede requerir recorrer un rango de memoria más amplio para encontrar la información deseada. Aunque los índices dispersos ocupan menos espacio de almacenamiento, pueden dar como resultado tiempos de acceso más largos, especialmente al buscar valores que no se encuentran en las entradas específicas del índice.

En conclusión, los índices densos ofrecen un acceso más rápido a los datos, pero ocupan más espacio, mientras que los índices dispersos requieren menos espacio, pero pueden implicar tiempos de búsqueda más largos al no tener una entrada para cada valor de la clave de búsqueda. La elección entre uno u otro depende de las necesidades específicas de rendimiento y uso de la base de datos.



### Uso de índices

Sabemos que el uso de índices agiliza el procesamiento de consultas. Este conocimiento nos puede llevar a pensar que crear índices en todas las columnas de las tablas de un esquema relacional sería aconsejable, pero esto es un error.

Un efecto negativo es que los índices deben ser almacenados en algún lugar. Para ello, se empleará espacio de disco. Por ello, el uso de índices en cada columna aumentará significativamente el tamaño de la base de datos.

Otro efecto negativo ocurre en las operaciones de inserción, actualización y eliminación que se realicen sobre tablas que tengan algún tipo de índice, pues verán aumentado su tiempo de ejecución. Esto es debido a que, después de la ejecución de cada una de estas operaciones, es necesario actualizar los índices presentes en la tabla sobre la que se ha realizado alguna de las operaciones anteriores.

También se puede producir un desperdicio de recursos en algunas consultas, pues no todos los índices serán utilizados. Mantener índices innecesarios para ciertas consultas consume recursos sin proporcionar beneficios significativos.

Los sistemas de gestión de bases de datos utilizan estadísticas para determinar cómo ejecutar consultas de manera eficiente. Mantener demasiados índices puede hacer que la recopilación y actualización de estadísticas sea más costosa. [*(Microsoft, 2023)*](https://learn.microsoft.com/en-us/sql/relational-databases/statistics/statistics?view=sql-server-ver15)



## Análisis de índices

En esta sección, revisaré los índices predeterminados y evaluaré el rendimiento inicial de las consultas antes de crear nuevos índices. Identificaré áreas de mejora, crearé índices específicos y analizaré su impacto en el rendimiento. Este análisis proporcionará una visión detallada de cómo los índices afectan la eficiencia operativa en nuestro esquema relacional.



### Índices creados por defecto

En nuestro esquema relacional, se crean varios índices automáticamente en ciertos tipos de columnas por varias razones.

Por otro lado, en las columnas que contienen claves foráneas, los índices se generan automáticamente para mejorar la eficiencia de las consultas de unión. Al crear un índice en la columna que contiene la clave foránea, se acelera la búsqueda de valores relacionados en la tabla referenciada.

En situaciones donde se tienen claves compuestas (`prices`) que constan de múltiples columnas, la creación automática de índices tiene como objetivo mejorar la velocidad de las consultas, especialmente a las que involucran varias columnas en condiciones `WHERE` o `JOIN`.





- **provinces**:
  - Clave primaria: *pro_id*
- **municipalities**:
  - Clave primaria: *mun_id*
  - Clave foránea: pro_id
- **localities**:
  - Clave primaria: *loc_id*
  - Clave foránea: mun_id
- **operators**:
  - Clave primaria: *op_id*
- **fuels**:
  - Clave primaria: *fuel_id*
- **prices**:
  - Claves primarias: *fuel_id*, *st_id*
- **stations**:
  - Clave primaria: *st_id*
  - Claves foráneas: loc_id, op_id



### Análisis de rendimiento de las consultas

Para analizar el rendimiento de las consultas antes de crear nuevos índices, se realizará un estudio detallado de las consultas existentes. Para ello utilizaré la instrucción `EXPLAIN ANALYZE` de SQL, la cual nos proporciona detalles sobre cómo se accede a las tablas, qué operaciones se realizan y cuánto tiempo se gasta en cada parte del proceso. Esto nos dará una visión completa del rendimiento de una consulta, desde la planificación hasta la ejecución real.



1. #### Nombre de la empresa con más estaciones de servicio terrestres

   ```sql
   EXPLAIN ANALYZE SELECT count(*) as total, name as empresa
   FROM operators, stations
   WHERE stations.op_id = operators.op_id and
         type = 'T'
   GROUP BY empresa
   ORDER BY total DESC
   LIMIT 1
   ```

   

   Se lanza la primera consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=38.8..38.8 rows=1 loops=1)
   -> Sort: total DESC, limit input to 1 row(s) per chunk (actual time=38..38 rows=1 loops=1)
   -> Table scan on <temporary> (actual time=36.9..37.5 rows=4008 loops=1)
   -> Aggregate using temporary table (actual time=36.9..36.9 rows=4008 loops=1)
   -> Nested loop inner join (cost=3100 rows=5592) (actual time=0.284..24 rows=11818 loops=1)
   -> Filter: (stations.`type` = 'T') (cost=1143 rows=5592) (actual time=0.221..8.57 rows=11818 loops=1)
   -> Table scan on stations (cost=1143 rows=11184) (actual time=0.207..6.14 rows=11954 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00106..0.00109 rows=1 loops=11818)
   ```

   

   Se puede observar como el resultado obtenido realiza un *full scan* en la tabla `stations` recorriendo 11954 filas para posteriormente realizar el filtrado de `stations.type = 'T'`, de las que encuentra 11818 filas.

   Con este dato, ya sabemos que podemos mejorar la consulta creando un nuevo índice en el atributo `type` de la tabla `stations`.

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX station_type_index ON stations (type)
   ```

   

   Una vez creado el índice, se vuelve a lanzar la consulta para analizarla.

   ```
   -> Limit: 1 row(s) (actual time=53.2..53.2 rows=1 loops=1)
   -> Sort: total DESC, limit input to 1 row(s) per chunk (actual time=53.2..53.2 rows=1 loops=1)
   -> Table scan on <temporary> (actual time=51.9..52.5 rows=4008 loops=1)
   -> Aggregate using temporary table (actual time=51.9..51.9 rows=4008 loops=1)
   -> Nested loop inner join (cost=4323 rows=11184) (actual time=0.321..39.9 rows=11818 loops=1)
   -> Table scan on operators (cost=408 rows=4051) (actual time=0.0542..1.83 rows=4051 loops=1)
   -> Filter: (stations.`type` = 'T') (cost=0.69 rows=2.76) (actual time=0.0036..0.009 rows=2.92 loops=4051)
   -> Index lookup on stations using stations_operators_op_id_fk (op_id=operators.op_id) (cost=0.69 rows=2.76) (actual time=0.00335..0.0083 rows=2.95 loops=4051)
   ```

   

   Ahora el resultado obtenido realiza un *full scan* en la tabla `operators` recorriendo 4051 filas para posteriormente realizar el producto cartesiano con las 11184 filas de tipo “T” de la tabla `stations`.

   Como no existe ningún atributo más en la condición `WHERE`, lo único que se puede intentar, es crear un índice en el atributo `name` de la tabla `operators`, pues es donde realiza el *full scan*.

   ```sql
   CREATE INDEX operators_name_index ON operators (name)
   ```

   

   Una vez modificada la consulta se vuelve a lanzar para su análisis, obteniendo los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=53.2..53.2 rows=1 loops=1)
   -> Sort: total DESC, limit input to 1 row(s) per chunk (actual time=53.2..53.2 rows=1 loops=1)
   -> Table scan on <temporary> (actual time=51.9..52.5 rows=4008 loops=1)
   -> Aggregate using temporary table (actual time=51.9..51.9 rows=4008 loops=1)
   -> Nested loop inner join (cost=4323 rows=11184) (actual time=0.321..39.9 rows=11818 loops=1)
   -> Table scan on operators (cost=408 rows=4051) (actual time=0.0542..1.83 rows=4051 loops=1)
   -> Filter: (stations.`type` = 'T') (cost=0.69 rows=2.76) (actual time=0.0036..0.009 rows=2.92 loops=4051)
   -> Index lookup on stations using stations_operators_op_id_fk (op_id=operators.op_id) (cost=0.69 rows=2.76) (actual time=0.00335..0.0083 rows=2.95 loops=4051)
   ```

   

   Se observa cómo desaparece el *full scan*, pero no se obtiene ninguna mejora de rendimiento respecto al paso anterior, con lo que, indexar el atributo `name` de la tabla `operators`, ocupará más espacio de memoria sin mejorar el rendimiento de esta consulta. Por lo tanto, esta consulta solo necesitaría de la indexación del atributo `type` de la tabla `stations` para estar optimizada.

   

2. #### Nombre de la empresa con más estaciones de servicio marítimas

   ```sql
   EXPLAIN ANALYZE SELECT count(*) as total, name as empresa
   FROM operators, stations
   WHERE stations.op_id = operators.op_id and
         type = 'M'
   GROUP BY empresa
   ORDER BY total DESC
   LIMIT 1
   ```

   

   Se lanza la primera consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=6.58..6.58 rows=1 loops=1)
   -> Sort: total DESC, limit input to 1 row(s) per chunk (actual time=6.58..6.58 rows=1 loops=1)
   -> Table scan on <temporary> (actual time=6.55..6.56 rows=59 loops=1)
   -> Aggregate using temporary table (actual time=6.55..6.55 rows=59 loops=1)
   -> Nested loop inner join (cost=3100 rows=5592) (actual time=0.11..6.34 rows=136 loops=1)
   -> Filter: (stations.`type` = 'M') (cost=1143 rows=5592) (actual time=0.0982..6.13 rows=136 loops=1)
   -> Table scan on stations (cost=1143 rows=11184) (actual time=0.0531..5.08 rows=11954 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00131..0.00135 rows=1 loops=136)
   ```

   

   Se puede observar como el resultado obtenido realiza un *full scan* en la tabla `stations` recorriendo 11954 filas para posteriormente realizar el filtrado de `stations.type = 'M'`, de las que encuentra 136 filas.

   Con este dato, ya sabemos que podemos mejorar la consulta creando un nuevo índice en el atributo `type` de la tabla `stations`.

   

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX station_type_index ON stations (type)
   ```

   

   Se vuelve a lanzar consulta y obtengo los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=0.561..0.561 rows=1 loops=1)
   -> Sort: total DESC, limit input to 1 row(s) per chunk (actual time=0.56..0.56 rows=1 loops=1)
   -> Table scan on <temporary> (actual time=0.534..0.542 rows=59 loops=1)
   -> Aggregate using temporary table (actual time=0.532..0.532 rows=59 loops=1)
   -> Nested loop inner join (cost=95.2 rows=136) (actual time=0.142..0.406 rows=136 loops=1)
   -> Index lookup on stations using stations_type_index (type='M'), with index condition: (stations.`type` = 'M') (cost=47.6 rows=136) (actual time=0.134..0.261 rows=136 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.251 rows=1) (actual time=842e-6..877e-6 rows=1 loops=136)
   ```

   

   Ahora el resultado obtenido solo realiza un *full scan* en una tabla temporal recorriendo 59 filas para posteriormente ordenar los resultados en orden descendente.

   Se concluye, al igual que en la consulta anterior, que esta consulta solo necesitaría de la indexación del atributo `type` de la tabla `stations` para estar optimizada.

   

3. #### Localización, nombre de empresa y margen de la estación con el precio más bajo para el combustible «Gasolina 95 E5» en la Comunidad de Madrid

   ```sql
   EXPLAIN ANALYZE SELECT longitude as longitud, latitude as latitud, operators.name as empresa,  margin as margen
   FROM stations, operators, fuels, prices, localities
   WHERE operators.op_id = stations.op_id AND
         stations.loc_id = localities.loc_id AND
         stations.st_id = prices.st_id AND
         fuels.fuel_id = prices.fuel_id AND
         localities.name = 'MADRID' AND
         fuels.name = 'Gasolina 95 E5'
   ORDER BY amount ASC
   LIMIT 1
   ```

   

   Se lanza la consulta y obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=4.4..4.4 rows=1 loops=1)
   -> Sort: prices.amount, limit input to 1 row(s) per chunk (actual time=4.4..4.4 rows=1 loops=1)
   -> Stream results (cost=1779 rows=193) (actual time=0.164..4.3 rows=227 loops=1)
   -> Nested loop inner join (cost=1779 rows=193) (actual time=0.159..4.14 rows=227 loops=1)
   -> Nested loop inner join (cost=1277 rows=193) (actual time=0.149..3.11 rows=242 loops=1)
   -> Nested loop inner join (cost=956 rows=193) (actual time=0.143..2.82 rows=242 loops=1)
   -> Inner hash join (no condition) (cost=454 rows=67.6) (actual time=0.112..2.29 rows=1 loops=1)
   -> Filter: (localities.`name` = 'MADRID') (cost=267 rows=423) (actual time=0.0559..2.24 rows=1 loops=1)
   -> Table scan on localities (cost=267 rows=4226) (actual time=0.0327..1.81 rows=4226 loops=1)
   -> Hash
   -> Filter: (fuels.`name` = 'Gasolina 95 E5') (cost=1.85 rows=1.6) (actual time=0.0341..0.0432 rows=1 loops=1)
   -> Table scan on fuels (cost=1.85 rows=16) (actual time=0.0316..0.0375 rows=16 loops=1)
   -> Index lookup on stations using stations_localities_loc_id_fk (loc_id=localities.loc_id) (cost=0.714 rows=2.85) (actual time=0.0307..0.501 rows=242 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.156 rows=1) (actual time=964e-6..995e-6 rows=1 loops=242)
   -> Single-row index lookup on prices using PRIMARY (st_id=stations.st_id, fuel_id=fuels.fuel_id) (cost=0.25 rows=1) (actual time=0.00395..0.00398 rows=0.938 loops=242)
   ```

   

   Se puede observar como el resultado obtenido realiza varios *full scan*. El primero en la tabla `fuels` recorriendo 16 filas para posteriormente realizar el filtrado de `fuels.name = 'Gasolina 95 ES'`, de las que encuentra 1 sola fila. El segundo lo realiza en la tabla `localities` recorriendo 4226 filas para posteriormente realizar el filtrado de `localities.name = 'MADRID'`, de las que encuentra 1 sola fila. 

   Con estos datos, ya sabemos que podemos mejorar la consulta creando dos nuevos índices en los atributos `name` de la tabla `fuels` y `name` de la tabla `localities`.

   

   Para realizar un análisis más progresivo e ir comparando las mejoras que realiza cada indexación, se crearán los índices uno a uno.

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX fuels_name_index ON fuels (name)
   ```

   

   Se lanza de nuevo la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=4.42..4.42 rows=1 loops=1)
   -> Sort: prices.amount, limit input to 1 row(s) per chunk (actual time=4.42..4.42 rows=1 loops=1)
   -> Stream results (cost=1526 rows=112) (actual time=0.116..4.33 rows=227 loops=1)
   -> Nested loop inner join (cost=1526 rows=112) (actual time=0.113..4.18 rows=227 loops=1)
   -> Nested loop inner join (cost=1009 rows=112) (actual time=0.106..3.07 rows=242 loops=1)
   -> Nested loop inner join (cost=718 rows=112) (actual time=0.101..2.75 rows=242 loops=1)
   -> Inner hash join (no condition) (cost=427 rows=42.3) (actual time=0.0788..2.14 rows=1 loops=1)
   -> Filter: (localities.`name` = 'MADRID') (cost=427 rows=423) (actual time=0.0559..2.12 rows=1 loops=1)
   -> Table scan on localities (cost=427 rows=4226) (actual time=0.033..1.65 rows=4226 loops=1)
   -> Hash
   -> Covering index lookup on fuels using fuels_name_index (name='Gasolina 95 E5') (cost=0.35 rows=1) (actual time=0.0117..0.0135 rows=1 loops=1)
   -> Index lookup on stations using stations_localities_loc_id_fk (loc_id=localities.loc_id) (cost=0.662 rows=2.65) (actual time=0.0214..0.585 rows=242 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00109..0.00112 rows=1 loops=242)
   -> Single-row index lookup on prices using PRIMARY (st_id=stations.st_id, fuel_id=fuels.fuel_id) (cost=0.453 rows=1) (actual time=0.00435..0.00439 rows=0.938 loops=242)
   ```

   

   Ahora se observa como el resultado obtenido solo realiza *full scan* en la tabla `localities` recorriendo 4226 filas para posteriormente realizar el filtrado de `localities.name = 'MADRID'`, de las que encuentra 1 sola fila. 

   

   El siguiente paso es anular el *full scan* de la tabla `localities`, para lo que se creará el nuevo índice en el atributo `name` con la instrucción:

   ```sql
   CREATE INDEX localities_name_index ON localities (name)
   ```

   

   Volvemos a lanzar la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=1.97..1.97 rows=1 loops=1)
   -> Sort: prices.amount, limit input to 1 row(s) per chunk (actual time=1.97..1.97 rows=1 loops=1)
   -> Stream results (cost=4.32 rows=2.85) (actual time=0.0702..1.86 rows=227 loops=1)
   -> Nested loop inner join (cost=4.32 rows=2.85) (actual time=0.0663..1.71 rows=227 loops=1)
   -> Nested loop inner join (cost=3.32 rows=2.85) (actual time=0.0609..1.05 rows=242 loops=1)
   -> Nested loop inner join (cost=2.32 rows=2.85) (actual time=0.0554..0.699 rows=242 loops=1)
   -> Nested loop inner join (cost=1.33 rows=1) (actual time=0.0285..0.041 rows=1 loops=1)
   -> Covering index lookup on fuels using fuels_name_index (name='Gasolina 95 E5') (cost=0.35 rows=1) (actual time=0.0197..0.0241 rows=1 loops=1)
   -> Covering index lookup on localities using localities_name_index (name='MADRID') (cost=0.975 rows=1) (actual time=0.0075..0.0155 rows=1 loops=1)
   -> Index lookup on stations using stations_localities_loc_id_fk (loc_id=localities.loc_id) (cost=0.999 rows=2.85) (actual time=0.0266..0.635 rows=242 loops=1)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.285 rows=1) (actual time=0.00121..0.00124 rows=1 loops=242)
   -> Single-row index lookup on prices using PRIMARY (st_id=stations.st_id, fuel_id=fuels.fuel_id) (cost=0.285 rows=1) (actual time=0.00252..0.00256 rows=0.938 loops=242)
   ```

   

   Finalmente, se observa cómo se han conseguido eliminar todos los *full scan* de la consulta mediante la indexación de los atributos `name` de las tablas `fuels` y `localities`, con lo que se concluye que la consulta está optimizada.

   

4. #### Localización, nombre de empresa y margen de la estación con el precio más bajo para el combustible «Gasóleo A» si resido en el centro de Albacete y no quiero desplazarme más de 10 KM

   ```sql
   EXPLAIN ANALYZE SELECT stations.longitude as longitud, stations.latitude as latitud, operators.name as empresa, stations.margin as margen, amount
   FROM stations, operators, fuels, prices
   WHERE stations.op_id = operators.op_id AND
         stations.st_id = prices.st_id AND
         fuels.fuel_id = prices.fuel_id AND
         fuels.name = 'Gasoleo A' AND
         (6371 * acos(cos(radians(38.9942400)) * cos(radians(stations.latitude)) * cos(radians(stations.longitude) - radians(-1.8564300)) + sin(radians(38.9942400)) * sin(radians(stations.latitude)))) <= 10
   ORDER BY (6371 * acos(cos(radians(38.9942400)) * cos(radians(stations.latitude)) * cos(radians(stations.longitude) - radians(-1.8564300)) + sin(radians(38.9942400)) * sin(radians(stations.latitude)))) ASC
   LIMIT 1
   ```

   

   Se lanza la consulta y obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=45.1..45.1 rows=1 loops=1)
   -> Sort: (6371 * acos((((cos(radians(38.9942400)) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - radians(-(1.8564300))))) + (sin(radians(38.9942400)) * sin(radians(stations.latitude)))))), limit input to 1 row(s) per chunk (actual time=45.1..45.1 rows=1 loops=1)
   -> Stream results (cost=4152 rows=5042) (actual time=1.77..45 rows=39 loops=1)
   -> Nested loop inner join (cost=4152 rows=5042) (actual time=1.76..44.7 rows=39 loops=1)
   -> Nested loop inner join (cost=2387 rows=5042) (actual time=1.75..44.6 rows=39 loops=1)
   -> Nested loop inner join (cost=622 rows=5042) (actual time=0.248..17.8 rows=11551 loops=1)
   -> Filter: (fuels.`name` = 'Gasoleo A') (cost=1.85 rows=1.6) (actual time=0.0231..0.0307 rows=1 loops=1)
   -> Table scan on fuels (cost=1.85 rows=16) (actual time=0.019..0.025 rows=16 loops=1)
   -> Index lookup on prices using prices_fuels_fuel_id_fk (fuel_id=fuels.fuel_id) (cost=270 rows=3152) (actual time=0.223..16.8 rows=11551 loops=1)
   -> Filter: ((6371 * acos((((<cache>(cos(radians(38.9942400))) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - <cache>(radians(-(1.8564300)))))) + (<cache>(sin(radians(38.9942400))) * sin(radians(stations.latitude)))))) <= 10) (cost=0.25 rows=1) (actual time=0.00218..0.00218 rows=0.00338 loops=11551)
   -> Single-row index lookup on stations using PRIMARY (st_id=prices.st_id) (cost=0.25 rows=1) (actual time=0.00148..0.00151 rows=1 loops=11551)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00274..0.00279 rows=1 loops=39)
   ```

   

   Se observa como el resultado obtenido realiza un *full scan* en la tabla `fuels` recorriendo 16 filas para posteriormente realizar el filtrado de `fuels.name = 'Gasoleo A'`, encontrando 1 sola fila.

   Con este dato, ya sabemos que podemos mejorar la consulta creando un nuevo índice en el atributo `name` de la tabla `fuels`.

   

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX fuels_name_index ON fuels (name)
   ```

   

   Se lanza de nuevo la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=45.4..45.4 rows=1 loops=1)
   -> Sort: (6371 * acos((((cos(radians(38.9942400)) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - radians(-(1.8564300))))) + (sin(radians(38.9942400)) * sin(radians(stations.latitude)))))), limit input to 1 row(s) per chunk (actual time=45.4..45.4 rows=1 loops=1)
   -> Stream results (cost=2594 rows=3152) (actual time=1.87..45.4 rows=39 loops=1)
   -> Nested loop inner join (cost=2594 rows=3152) (actual time=1.86..45.2 rows=39 loops=1)
   -> Nested loop inner join (cost=1491 rows=3152) (actual time=1.85..45 rows=39 loops=1)
   -> Nested loop inner join (cost=388 rows=3152) (actual time=0.169..17.8 rows=11551 loops=1)
   -> Covering index lookup on fuels using fuels_name_index (name='Gasoleo A') (cost=0.35 rows=1) (actual time=0.0161..0.0206 rows=1 loops=1)
   -> Index lookup on prices using prices_fuels_fuel_id_fk (fuel_id=fuels.fuel_id) (cost=388 rows=3152) (actual time=0.152..16.8 rows=11551 loops=1)
   -> Filter: ((6371 * acos((((<cache>(cos(radians(38.9942400))) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - <cache>(radians(-(1.8564300)))))) + (<cache>(sin(radians(38.9942400))) * sin(radians(stations.latitude)))))) <= 10) (cost=0.25 rows=1) (actual time=0.00221..0.00221 rows=0.00338 loops=11551)
   -> Single-row index lookup on stations using PRIMARY (st_id=prices.st_id) (cost=0.25 rows=1) (actual time=0.00151..0.00155 rows=1 loops=11551)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00283..0.00287 rows=1 loops=39)
   ```

   

   Se observa cómo se ha conseguido eliminar los *full scan* de la consulta mediante la indexación del atributo `name` de la tabla `fuels`.

   Sabemos que para optimizar las consultas, conviene actuar en los atributos contenidos en las cláusulas `WHERE` y `JOIN`, que en este caso son: `fuels.name`, `stations.latitude` y `stations.longitude`, pero no se observa que la consulta realice nada especial con los atributos `longitude` y `latitude` de la tabla `stations`. No obstante, se decide probar a indexar estos dos atributos y comprobar los resultados obtenidos.

   

   Se crean los nuevos índices con las instrucciones:

   ```sql
   CREATE INDEX stations_longitude_index ON stations (longitude)
   CREATE INDEX stations_latitude_index ON stations (latitude)
   ```

   

   Volvemos a lanzar la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=45.4..45.4 rows=1 loops=1)
   -> Sort: (6371 * acos((((cos(radians(38.9942400)) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - radians(-(1.8564300))))) + (sin(radians(38.9942400)) * sin(radians(stations.latitude)))))), limit input to 1 row(s) per chunk (actual time=45.4..45.4 rows=1 loops=1)
   -> Stream results (cost=2594 rows=3152) (actual time=1.75..45.4 rows=39 loops=1)
   -> Nested loop inner join (cost=2594 rows=3152) (actual time=1.74..45.1 rows=39 loops=1)
   -> Nested loop inner join (cost=1491 rows=3152) (actual time=1.73..45 rows=39 loops=1)
   -> Nested loop inner join (cost=388 rows=3152) (actual time=0.153..17.6 rows=11551 loops=1)
   -> Covering index lookup on fuels using fuels_name_index (name='Gasoleo A') (cost=0.35 rows=1) (actual time=0.00987..0.0139 rows=1 loops=1)
   -> Index lookup on prices using prices_fuels_fuel_id_fk (fuel_id=fuels.fuel_id) (cost=388 rows=3152) (actual time=0.142..16.5 rows=11551 loops=1)
   -> Filter: ((6371 * acos((((<cache>(cos(radians(38.9942400))) * cos(radians(stations.latitude))) * cos((radians(stations.longitude) - <cache>(radians(-(1.8564300)))))) + (<cache>(sin(radians(38.9942400))) * sin(radians(stations.latitude)))))) <= 10) (cost=0.25 rows=1) (actual time=0.00223..0.00223 rows=0.00338 loops=11551)
   -> Single-row index lookup on stations using PRIMARY (st_id=prices.st_id) (cost=0.25 rows=1) (actual time=0.00152..0.00155 rows=1 loops=11551)
   -> Single-row index lookup on operators using PRIMARY (op_id=stations.op_id) (cost=0.25 rows=1) (actual time=0.00382..0.00387 rows=1 loops=39)
   ```

   

   Comprobamos como el resultado devuelto es idéntico al anterior, no obteniendo ninguna mejora de rendimiento, con lo que, indexar los atributos `longitude` y `latitude` de la tabla `stations`, ocupará más espacio de memoria sin mejorar el rendimiento de esta consulta.

   Esto puede ser debido a que la consulta utiliza los atributos `longitude` y `latitude` para la clasificación de los resultados utilizando la fórmula *haversine*, la cual se utiliza para calcular la distancia entre dos puntos en la superficie de una esfera, en nuestro caso la Tierra.

   Por lo tanto, esta consulta solo necesitaría de la indexación del atributo `name` de la tabla `fuels` para estar optimizada.

   

5. #### Provincia en la que se encuentre la estación de servicio marítima con el combustible «Gasolina 95 E5» más caro

   ```sql
   EXPLAIN ANALYZE SELECT provinces.name as nombre
   FROM stations, provinces, municipalities, localities, fuels, prices
   WHERE stations.loc_id = localities.loc_id AND
         localities.mun_id = municipalities.mun_id AND
         municipalities.pro_id = provinces.pro_id AND
         stations.st_id = prices.st_id AND
         fuels.fuel_id = prices.fuel_id AND
         fuels.name = 'Gasolina 95 E5' AND
         stations.type = 'M'
   ORDER BY amount DESC
   LIMIT 1
   ```

   

   Se lanza la consulta y obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=39.3..39.3 rows=1 loops=1)
   -> Sort: prices.amount DESC, limit input to 1 row(s) per chunk (actual time=39.3..39.3 rows=1 loops=1)
   -> Stream results (cost=5035 rows=2521) (actual time=0.697..39.2 rows=81 loops=1)
   -> Nested loop inner join (cost=5035 rows=2521) (actual time=0.694..39.1 rows=81 loops=1)
   -> Nested loop inner join (cost=4152 rows=2521) (actual time=0.69..39 rows=81 loops=1)
   -> Nested loop inner join (cost=3270 rows=2521) (actual time=0.685..38.9 rows=81 loops=1)
   -> Nested loop inner join (cost=2387 rows=2521) (actual time=0.68..38.7 rows=81 loops=1)
   -> Nested loop inner join (cost=622 rows=5042) (actual time=0.2..17.9 rows=10763 loops=1)
   -> Filter: (fuels.`name` = 'Gasolina 95 E5') (cost=1.85 rows=1.6) (actual time=0.0167..0.0263 rows=1 loops=1)
   -> Table scan on fuels (cost=1.85 rows=16) (actual time=0.0141..0.0201 rows=16 loops=1)
   -> Index lookup on prices using prices_fuels_fuel_id_fk (fuel_id=fuels.fuel_id) (cost=270 rows=3152) (actual time=0.182..16.9 rows=10763 loops=1)
   -> Filter: ((stations.`type` = 'M') and (stations.loc_id is not null)) (cost=0.25 rows=0.5) (actual time=0.0018..0.0018 rows=0.00753 loops=10763)
   -> Single-row index lookup on stations using PRIMARY (st_id=prices.st_id) (cost=0.25 rows=1) (actual time=0.00147..0.00151 rows=1 loops=10763)
   -> Single-row index lookup on localities using PRIMARY (loc_id=stations.loc_id) (cost=0.25 rows=1) (actual time=0.00159..0.00162 rows=1 loops=81)
   -> Filter: (municipalities.pro_id is not null) (cost=0.25 rows=1) (actual time=0.00171..0.0018 rows=1 loops=81)
   -> Single-row index lookup on municipalities using PRIMARY (mun_id=localities.mun_id) (cost=0.25 rows=1) (actual time=0.00154..0.00157 rows=1 loops=81)
   -> Single-row index lookup on provinces using PRIMARY (pro_id=municipalities.pro_id) (cost=0.25 rows=1) (actual time=0.00122..0.00126 rows=1 loops=81)
   ```

   

   Se observa como el resultado obtenido realiza un *full scan* en la tabla `fuels` recorriendo 16 filas para posteriormente realizar el filtrado de `fuels.name = ' Gasolina 95 E5'`, encontrando 1 sola fila.

   Procedemos a mejorar la consulta creando un nuevo índice en el atributo `name` de la tabla `fuels`.

   

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX fuels_name_index ON fuels (name)
   ```

   

   Se lanza de nuevo la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=37.3..37.3 rows=1 loops=1)
   -> Sort: prices.amount DESC, limit input to 1 row(s) per chunk (actual time=37.3..37.3 rows=1 loops=1)
   -> Stream results (cost=3146 rows=1576) (actual time=0.491..37.2 rows=81 loops=1)
   -> Nested loop inner join (cost=3146 rows=1576) (actual time=0.488..37.2 rows=81 loops=1)
   -> Nested loop inner join (cost=2594 rows=1576) (actual time=0.484..37 rows=81 loops=1)
   -> Nested loop inner join (cost=2043 rows=1576) (actual time=0.481..36.8 rows=81 loops=1)
   -> Nested loop inner join (cost=1491 rows=1576) (actual time=0.477..36.7 rows=81 loops=1)
   -> Nested loop inner join (cost=388 rows=3152) (actual time=0.151..16.6 rows=10763 loops=1)
   -> Covering index lookup on fuels using fuels_name_index (name='Gasolina 95 E5') (cost=0.35 rows=1) (actual time=0.0103..0.014 rows=1 loops=1)
   -> Index lookup on prices using prices_fuels_fuel_id_fk (fuel_id=fuels.fuel_id) (cost=388 rows=3152) (actual time=0.14..15.4 rows=10763 loops=1)
   -> Filter: ((stations.`type` = 'M') and (stations.loc_id is not null)) (cost=0.25 rows=0.5) (actual time=0.00172..0.00172 rows=0.00753 loops=10763)
   -> Single-row index lookup on stations using PRIMARY (st_id=prices.st_id) (cost=0.25 rows=1) (actual time=0.00139..0.00143 rows=1 loops=10763)
   -> Single-row index lookup on localities using PRIMARY (loc_id=stations.loc_id) (cost=0.25 rows=1) (actual time=0.00169..0.00173 rows=1 loops=81)
   -> Filter: (municipalities.pro_id is not null) (cost=0.25 rows=1) (actual time=0.00186..0.00196 rows=1 loops=81)
   -> Single-row index lookup on municipalities using PRIMARY (mun_id=localities.mun_id) (cost=0.25 rows=1) (actual time=0.0017..0.00173 rows=1 loops=81)
   -> Single-row index lookup on provinces using PRIMARY (pro_id=municipalities.pro_id) (cost=0.25 rows=1) (actual time=0.00125..0.00128 rows=1 loops=81)
   ```

   

   Se observa cómo se ha conseguido eliminar los *full scan* de la consulta mediante la indexación del atributo `name` de la tabla `fuels`.

   Esta consulta tiene además el atributo `type` de la tabla `stations` dentro de la cláusula `WHERE`, lo que es posible que mejore la consulta si se indexa este atributo.

   

   Se crea el nuevo índice con la instrucción:

   ```sql
   CREATE INDEX stations_type_index ON stations (type)
   ```

   

   Se lanza de nuevo la consulta y se obtienen los siguientes resultados:

   ```
   -> Limit: 1 row(s) (actual time=1.63..1.63 rows=1 loops=1)
   -> Sort: prices.amount DESC, limit input to 1 row(s) per chunk (actual time=1.63..1.63 rows=1 loops=1)
   -> Stream results (cost=238 rows=136) (actual time=0.177..1.6 rows=81 loops=1)
   -> Nested loop inner join (cost=238 rows=136) (actual time=0.175..1.56 rows=81 loops=1)
   -> Nested loop inner join (cost=191 rows=136) (actual time=0.157..1.01 rows=136 loops=1)
   -> Nested loop inner join (cost=143 rows=136) (actual time=0.153..0.857 rows=136 loops=1)
   -> Nested loop inner join (cost=95.6 rows=136) (actual time=0.148..0.628 rows=136 loops=1)
   -> Nested loop inner join (cost=48 rows=136) (actual time=0.142..0.319 rows=136 loops=1)
   -> Covering index lookup on fuels using fuels_name_index (name='Gasolina 95 E5') (cost=0.35 rows=1) (actual time=0.0159..0.0188 rows=1 loops=1)
   -> Filter: (stations.loc_id is not null) (cost=47.6 rows=136) (actual time=0.125..0.288 rows=136 loops=1)
   -> Index lookup on stations using stations_type_index (type='M'), with index condition: (stations.`type` = 'M') (cost=47.6 rows=136) (actual time=0.124..0.272 rows=136 loops=1)
   -> Single-row index lookup on localities using PRIMARY (loc_id=stations.loc_id) (cost=0.251 rows=1) (actual time=0.00202..0.00206 rows=1 loops=136)
   -> Filter: (municipalities.pro_id is not null) (cost=0.251 rows=1) (actual time=0.00134..0.00144 rows=1 loops=136)
   -> Single-row index lookup on municipalities using PRIMARY (mun_id=localities.mun_id) (cost=0.251 rows=1) (actual time=0.00119..0.00122 rows=1 loops=136)
   -> Single-row index lookup on provinces using PRIMARY (pro_id=municipalities.pro_id) (cost=0.251 rows=1) (actual time=894e-6..927e-6 rows=1 loops=136)
   -> Single-row index lookup on prices using PRIMARY (st_id=stations.st_id, fuel_id=fuels.fuel_id) (cost=0.251 rows=1) (actual time=0.00381..0.00383 rows=0.596 loops=136)
   ```

   

   Al añadir este último índice, se observa como el tiempo de ejecución de la consulta se ha reducido considerablemente, pues en vez de tener que comparar en los `JOIN` entre 1576 y 3152 filas, se reduce esta comparación a 136 filas.

   Por lo tanto, esta consulta solo necesitaría de la indexación de los atributos `name` de la tabla `fuels` y `type` de la tabla `stations` para estar optimizada.



### Conclusión de indexación para mejorar el rendimiento

Después de llevar a cabo el análisis exhaustivo de las cinco consultas realizadas en la actividad anterior, con el objetivo de identificar oportunidades de optimización y mejorar el rendimiento de las operaciones en la base de datos, se decide que los índices que más impactaran positivamente en la velocidad de ejecución de las consultas son:

- `stations.type`
- `localities.name`
- `fuels.name`



La elección de indexar estos tres atributos está relacionada con las consultas realizadas y los patrones de acceso a los datos, que en este caso se debe a tres factores:

1. **Frecuencia de uso en las consultas**: Los tres atributos (`stations.type`, `localities.name`, y `fuels.name`) se utilizan con frecuencia en las consultas analizadas. Al indexar estos atributos, se acelera el proceso de búsqueda y recuperación de datos en las operaciones que los involucran.
2. **Balance entre rendimiento y mantenimiento**: Cada índice añadido implica un costo de mantenimiento, especialmente en términos de espacio de almacenamiento y posiblemente en la velocidad de inserciones y actualizaciones. La creación de índices adicionales debe equilibrarse cuidadosamente con la mejora real en el rendimiento de las consultas.
3. **Complejidad de las consultas**: La complejidad y naturaleza de las consultas también influyen en la elección de qué atributos indexar. En este caso, los índices creados abordan eficazmente las condiciones de filtrado más críticas en las consultas.



### Indexación de otros posibles atributos

En esta sección se justificará la creación de otros posibles índices para otras posibles consultas que se pudieran realizar en la base de datos.



#### operators.name

Cuando realicemos consultas para obtener información sobre una estación de servicio asociada a un operador particular, por ejemplo: Repsol o Petronor, el índice en `operators.name` facilitará una búsqueda rápida y eficiente. Sin este índice, la búsqueda requeriría un escaneo completo de la tabla `operators` para encontrar coincidencias, lo que podría resultar costoso en términos de tiempo, especialmente a medida que la cantidad de datos aumenta.



<u>Ejemplo de consulta</u>:

##### Mostrar todas las estaciones de servicio de Repsol en Logroño

```sql
SELECT operators.name as nombre, address as dirección, localities.name as localidad
FROM stations, operators, localities
WHERE stations.loc_id = localities.loc_id and
      stations.op_id = operators.op_id and
      operators.name = 'repsol' and
      localities.name = 'logroño'
```



#### provinces.name

Al indexar la columna `name` en la tabla `provinces`, se agiliza significativamente la ejecución de consultas que buscan estaciones de servicio en una provincia particular. Se justificaría la creación de este índice en escenarios donde las consultas frecuentes estuvieran orientadas a recuperar información específica sobre estaciones de servicio ubicadas en una provincia concreta, como en el caso de buscar las estaciones de servicio en La Rioja.



<u>Ejemplo de consulta</u>:

##### Mostrar todas las estaciones de servicio de La Rioja

```sql
SELECT address as dirección, schedule as horario, localities.name as localidad
FROM stations, provinces, municipalities, localities
WHERE stations.loc_id = localities.loc_id and
      localities.mun_id = municipalities.mun_id and
      municipalities.pro_id = provinces.pro_id and
      provinces.name = 'RIOJA (LA)'
```



#### municipalities.name

Al igual que en la creación del índice anterior, la creación del índice `municipalities.name` se justifica en situaciones en las que las consultas frecuentes se centran en recuperar información específica sobre estaciones de servicio ubicadas en un municipio particular. 



<u>Ejemplo de consulta</u>:

##### Mostrar todas las estaciones de servicio de Logroño

```sql
SELECT address as dirección, schedule as horario, localities.name as localidad
FROM stations, municipalities, localities
WHERE stations.loc_id = localities.loc_id and
      localities.mun_id = municipalities.mun_id and
      municipalities.name = 'LOGROÑO'
```



#### prices.amount

Este caso es más específico, pues habitualmente se suelen consultar los precios más baratos, pero crear el índice `prices.amount` se justificaría en situaciones en las que las consultas frecuentes se centraran en consultas que buscan información específica sobre los precios de los combustibles en las estaciones de servicio.



<u>Ejemplo de consulta</u>:

##### Encontrar estaciones de servicio con precios de combustible inferiores a cierto umbral

```sql
SELECT address as dirección, name as carburante, amount as precio
FROM stations, fuels, prices
WHERE stations.st_id = prices.st_id and
      prices.fuel_id = fuels.fuel _id and
      prices.amount < 0.7
```



## Conclusiones

A lo largo de este trabajo, se ha realizado un exhaustivo proceso de indexación y análisis del rendimiento en la base de datos. Desde la creación automática de índices iniciales hasta la evaluación de consultas y la justificación de índices adicionales, explorando diferentes aspectos para optimizar la eficiencia en el acceso a los datos.

Se observó que la base de datos, al ser creada, establece índices automáticamente en las claves primarias y foráneas, garantizando la integridad referencial y mejorando la eficiencia en la ejecución de consultas relacionadas con la unión de tablas.

Utilizando la herramienta `EXPLAIN ANALYZE`, se ha analizado el rendimiento de consultas antes de la creación de nuevos índices. Este análisis ha proporcionado información valiosa para identificar áreas de mejora en términos de velocidad de ejecución y eficiencia.

La creación de índices adicionales fue la estrategia empleada para optimizar consultas específicas. Se destacaron índices como `stations.type`, `localities.name` y `fuels.name`, seleccionados en función de la frecuencia y complejidad de las consultas, así como de la mejora significativa del rendimiento esperada.

En cuanto a la creación de índices adicionales, cabe destacar que no todas las consultas necesitaban esta optimización. La decisión se tomó considerando la frecuencia y complejidad de las consultas, dando prioridad a aquellos casos donde se evidenció una mejora significativa en el rendimiento.

En conjunto, este trabajo ha proporcionado una visión integral del proceso de indexación y su impacto en el rendimiento de las consultas. Destaca la importancia de seleccionar cuidadosamente los atributos clave y justificar la creación de índices en función de las necesidades específicas de consultas en la base de datos. La eficacia de esta estrategia se reflejó en un análisis comparativo del rendimiento antes y después de la indexación, subrayando la relevancia de la optimización continua en la gestión de bases de datos.



<center>by <strong>Jose Manuel Pinillos</strong></center>

