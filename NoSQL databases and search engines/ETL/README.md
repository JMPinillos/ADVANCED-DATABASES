# Laboratorio - Actividad 01
Título: `Actividad grupal: Bases de datos NoSQL y motores de búsqueda`

Autor: 
- `Alberto de la Serna Parada`
- `José Manuel Pinillos Rubio`
- `Luis Díaz Madero`

Asignatura: `Bases de datos avanzadas`

Fecha: `13 de Enero de 2024`

## Descripción del proyecto
En una primera actividad, se ha creado una base de datos relacional en MySQL con los datos de precios de combustibles de las estaciones de servicio de España. 

En esta segunda actividad, se propone exportar la base de datos relacional a una base de datos NoSQL y realizar consultas sobre ella.
Como sabemos `Elasticsearch` no es una base de datos NoSQL, pero vemos muy interesante crear un índice en `Elasticsearch` para realizar consultas sobre él.

Si nos imaginamos un escenario en el que tenemos que proporcionar a la ciudadanía un servicio de consulta de precios de combustibles, dado que sólo se van a realizar consultas vemos muy adecuado crear un índice en `Elasticsearch` y realizar las consultas sobre él. 

Por lo tanto, esta actividad es una continuación de la actividad anterior. En esta actividad se propone realizar las siguientes tareas:
- Exportar la base de datos relacional a un fichero `JSON`
- Crear un índice en `Elasticsearch` con los datos del fichero `JSON`

Se ha creado un nuevo paquete `com.unir.app.elasticsearch` para realizar las tareas propuestas.


> NOTA: El código base es el de la primera actividad, en este caso desarrollada por Alberto de la Serna, se han implementado las sugerencias de mejora del profesor en la corrección de la actividad:
> - Eliminar la instrucción `continue` en los bucles `for` y `while` para mejorar la legibilidad del código.


## Paquetes del programa Java

Esta es la estructura de paquetes del proyecto:
```text
├── com.unir/
│   ├── com.unir.app/
│   │   ├── com.unir.app.elasticsearch
│   │   ├── com.unir.app.read
│   │   ├── com.unir.app.write
│   ├── com.unir.config/
│   ├── com.unir.models/
│   └── com.unir.types/
```
Detalles de los paquetes:
- `com.unir.config`: contiene la clase `MySqlConnector.java` se encarga de establecer la conexión con la base de datos MySQl.
- `com.unir.models`: contiene las clases que representan los objetos de la base de datos.
- `com.unir.types`: contiene los `enum` que son utilizados dentro de los modelos.
- `com.unir.app.read`: contiene la clase `ConsultarPrecios.java` lanza las consultas propuestas en la actividad.
- `com.unir.app.write`: contiene la clase `RegistrarPrecios.java` se encarga de leer los ficheros csv y almacenar los datos en la base de datos.
- `com.unir.app.elasticsearch`: contiene la clase `ExportarEstacionesElasticsearch.java` se encarga de crear un fichero `JSON` de la base de datos de `estaciones de servicio` e importarlo en `Elasticsearch (Bonsai.com)`.


## Pasos a seguir para utilizar el programa (He añadido al final el resto de pasos para importar los datos en Elasticsearch)

Antes de continuar leyendo, seguimos manteniendo los pasos de la primera actividad para poder crear de nuevo la base de datos en MySQL.

1. Abrir el fichero `basedatos.sql` y ejecutarlo en `MySQL`. Este fichero se encuentra en la raíz del proyecto. El Script también crea el esquema y se posiciona en él para que el resto del Script funcione bajo ese esquema. Este es el inicio del `Script` de la creación de la Base de datos:

   ```sql
   create schema laboratorio01 collate utf8mb4_0900_ai_ci;
   
   use laboratorio01;
   
   [...]
   ```

2. El programa utiliza dos ficheros `CSV` con los datos del Ministerio. Estos dos ficheros ya se encuentran en la raíz del proyecto.

3. Ejecutar el programa para registrar los datos en la base de datos. 
  - Para ello ejecutar la clase `RegistrarPrecios.java` que se encuentra en el paquete `com.unir.app.write`. Recuerda establecer las variables de entorno:

    ```
     MYSQL_USER={Tu usuario de MySQL};MYSQL_PASSWORD={Tu contraseña de MySQL}
    ```

  - El programa leerá los ficheros csv y almacenará los datos en la base de datos. Los csv se encuentran en la carpeta raíz del proyecto. 
    ```java
    // Se vacía la base de datos para que no haya datos duplicados
    vaciarBaseDatos(connection);
    log.info("Base de datos vaciada correctamente");
    
    // Leemos las estaciones terrestres y las cargamos en memoria
    leerCsvEstaciones(TipoEstacion.Terrestre);
    log.info("Se han cargado en memoria las estaciones terrestres, número total = {}", estaciones.size());
    
    // Leer las estaciones marítimas y las cargamos en memoria
    leerCsvEstaciones(TipoEstacion.Maritima);
    log.info("Se han cargado en memoria las estaciones marítimas, ahora el número número total de estaciones es = {}", estaciones.size());
    
    // Cargamos los datos en la base de datos
    cargarDatosEnBaseDatos(connection);
    
    log.info("Carga de información finalizada correctamente. Se han cargado las estaciones, número total = {}", estaciones.size());
    ```

4. Ejecutar el programa para realizar las consultas.

  - Para ello ejecutar la clase `ConsultarPrecios.java` que se encuentra en el paquete `com.unir.app.read`.
  - El programa lanzará las consultas propuestas en la actividad:
    ```java
    // Empresa con más estaciones de servicio terrestres
    getEmpresaConMasEstaciones(connection, TipoEstacion.Terrestre);
    
    // Empresa con más estaciones de servicio marítimas
    getEmpresaConMasEstaciones(connection, TipoEstacion.Maritima);
    
    // Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasolina 95 E5” en la Comunidad de Madrid.
    getEmpresaConCarburanteMasEconomico(connection, "Gasolina 95 E5", "Madrid");
    
    // Provincia en la que se encuentre la estación de servicio marítima con el combustible “Gasolina 95 E5” más caro.
    getProvinciaConEstacionMasCara(connection, TipoEstacion.Maritima, "Gasolina 95 E5");
    
    // Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasóleo A” si resido en el centro de Albacete y no quiero desplazarme más de 10 KM.
    // El centro de Albacete se encuentra en la ubicación [38.994349, -1.858542]
    getEstacionMasEconomicaSegunPuntoOrigenConDistanciaMaxima(connection,  BigDecimal.valueOf(38.994349), BigDecimal.valueOf(-1.858542), "Gasóleo A", 10);
    
    ```

5. Exportar la base de datos relacional a un fichero `JSON` y crear un índice en `Elasticsearch` con los datos del fichero `JSON`.

  - Para ello ejecutar la clase `ExportarEstacionesElasticsearch.java` que se encuentra en el paquete `com.unir.app.elasticsearch`.
  - El programa exportará los datos de la base de datos a un fichero `JSON` y lo importará en `Elasticsearch (Bonsai.com)`.
  - Para poder ejecutar el programa, es necesario establecer las siguientes variables de entorno:
    ```
    MYSQL_USER={Tu usuario de MySQL};MYSQL_PASSWORD={Tu contraseña de MySQL};HOST_BONSAI_ELASTICSEARCH={Tu Host de bonsai.io };
    ```
  - El programa realiza las siguientes tareas:
    - Exporta los datos de la base de datos a un fichero `JSON`:
      ```java
     
         // Crear JSON para empresas
         crearJsonEstaciones(connection);
      
      ```
    - Crea el índice en `Elasticsearch (Bonsai.com)`:
      ```java
            // Como es necesario la variable de entorno de Bonsai, si no viene cargada lanzamos una excepción
            HOST_BONSAI_ELASTICSEARCH = System.getenv("HOST_BONSAI_ELASTICSEARCH");
            log.debug("HOST_BONSAI_ELASTICSEARCH:{}",HOST_BONSAI_ELASTICSEARCH);
            if(HOST_BONSAI_ELASTICSEARCH == null){
                throw new RuntimeException("No se ha encontrado la variable de entorno HOST_BONSAI_ELASTICSEARCH");
            }

            // Crear índice en Elasticsearch
            crearIndiceEnElasticsearch();
      ```
    - Importa los datos del fichero `JSON` en `Elasticsearch (bonsai.io)`:
    
        ```JAVA
                // Importar JSON a Elasticsearch
            importarJson();
      ```
  
## Ficheros `JSON` y `SQL` de la carpeta `raíz` del proyecto
Por si hubiera algún problema con la exportación de los datos a `Elasticsearch`, se han añadido los ficheros `JSON` y `SQL` en la carpeta `raíz` del proyecto.
- `consultasExportarMySQL.sql`: contiene las consultas que hemos utilizado en el proceso para crear el índice en `Elasticsearch`.
- `basedatos.sql`: contiene el `Script` de creación de la base de datos en `MySQL`.
- `estaciones.json`: contiene los datos de las estaciones de servicio en formato `JSON`.
- `estacionesMapping.json`: contiene el `mapping` del índice de `Elasticsearch`.
