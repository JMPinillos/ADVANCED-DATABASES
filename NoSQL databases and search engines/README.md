# Bases de datos NoSQL y motores de búsqueda

## Introducción

Este proyecto representa nuestra incursión en el mundo de la gestión de bases de datos, donde nos proponemos integrar y optimizar dos sistemas de gestión de bases de datos líderes en su clase: MongoDB y Elasticsearch. A través de este proyecto, buscamos no solo fortalecer nuestra comprensión de estas tecnologías avanzadas, sino también explorar su sinergia y aplicabilidad en escenarios del mundo real.

El proyecto comienza con una evaluación crítica y la selección de un esquema relacional óptimo. Esta etapa inicial es crucial, ya que establece la base sobre la cual se construirá todo el sistema. Después de considerar varios esquemas propuestos, seleccionamos uno que se destacaba por su capacidad para reducir la duplicación de datos y facilitar consultas eficientes, dos aspectos esenciales para el rendimiento de cualquier base de datos.

Posteriormente, nos aventuramos en la transición de nuestro esquema relacional elegido hacia una solución NoSQL, específicamente utilizando MongoDB. Este cambio estratégico está motivado por la necesidad de un sistema que maneje datos semi-estructurados o con estructuras jerárquicas de manera más eficaz que las bases de datos relacionales tradicionales. MongoDB, con su flexibilidad en el manejo de documentos JSON y su capacidad para simplificar relaciones complejas en documentos anidados, se presenta como la opción ideal para nuestras necesidades.

Además, abordamos un desafío en el desarrollo de un proceso ETL (*Extract*, *Transform*, *Load*) utilizando Elasticsearch a través de JPA (*Java Persistence API*). Esto nos permite extraer datos de una fuente relacional existente, transformarlos para ajustarse a las necesidades de un entorno de búsqueda y análisis avanzado, y finalmente cargarlos en Elasticsearch. Al utilizar JPA, facilitamos la extracción y transformación de datos, asegurando su compatibilidad y eficiencia en el proceso de carga en Elasticsearch.

Con este proyecto, pretendemos no solo alcanzar un entendimiento profundo de MongoDB y Elasticsearch, sino también sentar las bases para futuras exploraciones y aplicaciones en el campo de las bases de datos y el análisis de datos.



## Desarrollo

En las siguientes secciones, detallamos el proceso de integración y configuración de dos sistemas de gestión de bases de datos altamente eficientes: MongoDB y Elasticsearch. Abordaremos desde la elección del esquema de datos hasta la implementación práctica, asegurando una base sólida para el manejo y análisis de la información en nuestro proyecto.



### Elección del esquema relacional

En nuestra primera reunión, tuvimos la oportunidad de examinar y discutir los diversos esquemas relacionales propuestos por los miembros del equipo. Tras un análisis detallado, decidimos seleccionar el esquema desarrollado por Jose Manuel Pinillos y Alberto de la Serna. Esta elección se basó en su eficaz enfoque para minimizar la duplicación de datos, una característica clave que no solo simplifica la estructura de la base de datos, sino que también mejora significativamente su eficiencia. Además, este esquema se destacó por su capacidad para facilitar consultas rápidas y efectivas, un aspecto crucial para el rendimiento óptimo de nuestra base de datos.



### Elección de la base de datos no relacional

Tras haber seleccionado nuestro esquema relacional, el siguiente paso fue determinar la base de datos no relacional más adecuada para nuestras necesidades. Nuestra elección se centró entre tres tipos:

•    **MongoDB**, una base de datos documental.

•    **Amazon DynamoDB**, una base de datos clave-valor.

•    **Neo4j**, una base de datos orientada a grafos.

Nuestro criterio de selección se basó no solo en la capacidad de almacenamiento de la información, ya que todas las opciones son viables en este sentido, sino también en el tipo de consultas que necesitaríamos realizar.

Inicialmente, descartamos la opción de una base de datos clave-valor como Amazon DynamoDB. Aunque este tipo es altamente eficiente para búsquedas simples basadas en claves, nuestras necesidades de consulta involucran múltiples atributos, lo que hace que esta opción sea menos adecuada.

Por otro lado, Neo4j, siendo una base de datos orientada a grafos, sería ideal si nuestras conexiones fueran extremadamente interconectadas y las relaciones tan cruciales como los propios datos. Dado que nuestro esquema no exhibe relaciones tan complejas, Neo4j tampoco se alinea bien con nuestros requisitos.

Finalmente, nos inclinamos por MongoDB, una base de datos documental que se adapta perfectamente a datos semi-estructurados o con estructuras jerárquicas. MongoDB nos ofrece la flexibilidad necesaria para manejar documentos JSON con estructuras variadas, algo especialmente beneficioso considerando que nuestro esquema incluye varias relaciones uno a muchos y una relación muchos a muchos. Estas características pueden ser eficientemente simplificadas en documentos anidados en MongoDB, lo que facilita un modelo de datos más coherente y optimizado para nuestras necesidades.

Sin embargo, es importante señalar que, aunque MongoDB maneja bien las estructuras jerárquicas, las consultas y actualizaciones pueden volverse más complejas con documentos profundamente anidados o con estructuras de datos muy variadas. Esta complejidad adicional es un factor a tener en cuenta al diseñar la estructura de nuestros documentos y planificar las operaciones de consulta y actualización.



### Implementación de la base de datos

En esta sección detallaremos cómo hemos integrado dos soluciones de bases de datos, MongoDB y Elasticsearch, para optimizar el almacenamiento y la recuperación de datos en nuestro proyecto.



#### Integración con MongoDB

MongoDB, una base de datos NoSQL destacada por su rapidez y simplicidad, se diferencia notablemente de Elasticsearch en su lenguaje de consulta, que es más sencillo y directo. Optar por MongoDB implica asumir que no requerimos integridad referencial ni transaccionalidad en el tratamiento de nuestros datos, lo cual es adecuado para ciertos tipos de aplicaciones.



#### Creación de la Base de Datos en MongoDB Atlas

Para establecer nuestra base de datos en MongoDB para el proyecto, el primer paso es dirigirnos a la página web de MongoDB y registrar una cuenta en MongoDB Atlas, la plataforma en la nube de MongoDB. Este proceso es sencillo y constituye la base para nuestros siguientes pasos.

Una vez que hemos creado nuestra cuenta en MongoDB Atlas, procedemos con la configuración del despliegue de nuestra base de datos. Iniciamos seleccionando el plan gratuito. A continuación, asignamos un nombre a nuestro Cluster. Posteriormente, seleccionamos un proveedor de servicios en la nube y una región geográfica que mejor se adapte a nuestras necesidades, teniendo en cuenta aspectos como la latencia y la ubicación de los usuarios finales.

Tras haber configurado y creado el cluster, accedemos a él y nos dirigimos a la pestaña de “*Collections*”. Aquí, seleccionamos el botón “*+ Create Database*”, ubicado en la parte superior izquierda. Al hacer clic en este botón, se nos presenta una ventana donde debemos introducir el nombre de nuestra nueva base de datos y el de la colección inicial. Al pulsar el botón “*Create*”, la base de datos junto con la colección especificada se creará, tal como se visualiza en la siguiente imagen.

![image-20240126135756329](../../../../AppData/Roaming/Typora/typora-user-images/image-20240126135756329.png)

<center><i>Creación de nueva Base de Datos y Colección en MongoDB Atlas</i></center>



Una vez establecida, nuestra base de datos en MongoDB Atlas aún se encuentra sin datos. Para comenzar a interactuar con ella, es imprescindible configurar credenciales de acceso. Este proceso se inicia creando un usuario administrador, que se realiza desde la pestaña "*Database Access*" en el panel de control lateral. Aquí, seleccionamos la opción "*Add New Database User*", lo que nos permite definir el método de autenticación preferido, asignar un nombre de usuario y una contraseña segura, y establecer el rol correspondiente, que en este caso será el de Administrador. Con este usuario, tendremos los privilegios necesarios para gestionar la base de datos y sus colecciones.



![image-20240126135842972](../../../../AppData/Roaming/Typora/typora-user-images/image-20240126135842972.png)

<center><i>Configuración de Usuario Administrador en MongoDB Atlas</i></center>



Tras acceder al cluster en la pestaña "*Overview*" de MongoDB Atlas, presionamos el botón "*Connect*", ubicado a la derecha de la pantalla. Al hacerlo, aparecerá una ventana con varias opciones de conexión. Elegimos "*Connect your application*" y luego seleccionamos el driver apropiado para nuestro entorno de desarrollo, que en nuestro caso es Java, junto con la versión correspondiente, la 4.3. Esto nos proporcionará la cadena de conexión necesaria para interactuar con la base de datos desde nuestra aplicación.

Por razones de seguridad y para mantener la configuración organizada, esta cadena de conexión se manejará como una variable de entorno denominada **`MONGO_URL`** dentro de nuestro código. Esto es esencial, ya que la cadena incluye el nombre de usuario y la contraseña del administrador que establecimos previamente, y debe permanecer única y confidencial.

El próximo paso es establecer un conector en nuestro proyecto Java, similar a cómo nos conectaríamos a una base de datos MySQL. Comenzamos agregando las dependencias necesarias de MongoDB en nuestro archivo **`pom.xml`**. Luego, procedemos a crear una clase llamada **`MongoConnector`** dentro de la carpeta **`config`**. Esta clase será responsable de establecer y mantener la conexión con nuestra base de datos MongoDB.

Finalmente, dentro de la carpeta **`Write`**, implementamos una nueva clase llamada **`InsertarDocMongo`**. Esta clase tiene la tarea de introducir documentos, los cuales se crean siguiendo la estructura de documentos específica de MongoDB, permitiéndonos así empezar a poblar nuestra base de datos con datos relevantes.



#### Diseño de documentos

Dentro de MongoDB, cada base de datos alberga una o más colecciones, que son el equivalente a las tablas de las bases de datos relacionales. Estas colecciones están compuestas por documentos, que en conjunto, contienen los datos de la aplicación.

Una característica distintiva de MongoDB es su flexibilidad en la gestión de colecciones. No es necesario crear una colección de forma explícita antes de usarla; una colección se crea automáticamente la primera vez que insertamos datos en ella. Sin embargo, también tenemos la opción de crear colecciones de forma explícita si así lo deseamos, lo cual puede ser útil para definir opciones de configuración específicas desde el inicio, como la creación de índices o la configuración de validaciones de esquema.

Este enfoque flexible permite un desarrollo ágil, ya que podemos empezar a almacenar documentos sin necesidad de definir previamente toda la estructura de la base de datos, lo cual es particularmente ventajoso en las fases iniciales de un proyecto cuando los esquemas de datos aún están en proceso de ser definidos.



#### Estructura del documento

La estructura del documento en MongoDB se define de la siguiente manera, utilizando un mapeo que especifica los tipos de datos para cada campo:



```json
{
  "Empresa": "string",
  "Direccion": "string",
  "CodigoPostal": "string",
  "Latitud": "double",
  "Longitud": "double",
  "Localidad": "string",
  "Municipio": "string",
  "Provincia": "string",
  "FechaPrecios": "date",
  "Horario": "string",
  "TipoEstacion": "string",
  "TipoVenta": "string",
  "Margen": "string",
  "Precios": [
    {
      "carburante": "string",
      "precio": "double"
    }
  ]
}
```

<center><i>Mapping del documento JSON</i></center>



#### Creación del índice

La creación de índices es un paso crucial para optimizar las consultas en MongoDB. Si estamos trabajando directamente en la shell de MongoDB, podemos crear un índice utilizando el siguiente comando:

```shell
db.estaciones.createIndex({"Localidad": 1});  
```

Este comando añadirá un índice ascendente en el campo "Localidad" dentro de la colección "estaciones", lo que facilita búsquedas rápidas y eficientes por localidad.



Además, MongoDB Atlas ofrece una interfaz gráfica intuitiva para añadir índices sin necesidad de escribir comandos manualmente. Dentro de nuestro cluster, navegamos a la pestaña "*Collections*", seleccionamos la base de datos y la colección específica para la que deseamos crear un índice. A continuación, accedemos a la pestaña "*Indexes*" y hacemos clic en el botón "*Create Index*". Se nos presentará una interfaz como la que se muestra en la imagen adjunta, donde podemos especificar los campos del índice y sus propiedades.

El uso de esta interfaz gráfica facilita la creación de índices, permitiéndonos incorporar la estructura definida en nuestro documento JSON de mapeo. Con tan solo unos clics, establecemos los índices necesarios para garantizar el rendimiento y la agilidad en las operaciones de nuestra base de datos.



![image-20240126141032923](../../../../AppData/Roaming/Typora/typora-user-images/image-20240126141032923.png)

<center><i>Interfaz de Creación de Índices en MongoDB Atlas</i></center>



#### Carga de datos

La inserción de datos en MongoDB puede abordarse de dos maneras:



1. **Inserción individual**:

   Para cargar datos individualmente, utilizamos la operación **`insertOne()`**. Esta operación debe seguir la estructura del documento previamente definido. A continuación se muestra un ejemplo de cómo insertar un documento representando una estación, conforme a la estructura proporcionada:

   

   ```json
   Document nuevoDocumento = new Document("Empresa", "OIL EXPRESS")
           .append("Direccion", "AVENIDA PRINCIPAL, 123")
           .append("CodigoPostal", "12345")
           .append("Latitud", 40.123456)
           .append("Longitud", -3.987654)
           .append("Localidad", "CIUDAD DEL PETRÓLEO")
           .append("Municipio", "PETROVILLE")
           .append("Provincia", "PETROLANDIA")
           .append("FechaPrecios", "2023-11-15")
           .append("Horario", "L-D: 06:00 - 22:00")
           .append("TipoEstacion", "Urbana")
           .append("TipoVenta", "Gasolina y Diesel")
           .append("Margen", "S")
           .append("Precios", Document.parse("{ \"carburante\": \"gasolina 95\", \"precio\": 1.349 }, { \"carburante\": \"gasóleo A\", \"precio\": 1.199 }"));
   
   collection.insertOne(nuevoDocumento);
   ```

   <center><i>Código de inserción de un documento</i></center>

   

   En este código, hemos creado un nuevo documento con detalles específicos de una estación de servicio y posteriormente lo hemos insertado en la colección usando **`insertOne()`**.

   

   ![image-20240126141331026](../../../../AppData/Roaming/Typora/typora-user-images/image-20240126141331026.png)

   <center><i>Vista del documento subido a la BD</i></center>

2. **Inserción masiva de datos**:

   Para realizar una inserción masiva de documentos en MongoDB, debemos preparar un archivo JSON que contenga todos los registros a cargar. La operación **`insertMany()`** se utiliza para insertar múltiples documentos en una sola operación, lo que mejora la eficiencia y reduce el tiempo de ejecución en comparación con la inserción de documentos de manera individual. El proceso se lleva a cabo de la siguiente manera:

   

   ```json
   db.estaciones.insertMany([
       {/* documento 1 con su estructura completa */},
       {/* documento 2 con su estructura completa */},
       {/* documento 3 con su estructura completa */},
       /... más documentos
   ])
   ```

   

   Al utilizar **`insertMany()`**, cada objeto dentro del array representa un documento único que sigue la estructura definida por el mapeo del documento JSON proporcionado en apartados anteriores. Este método es particularmente útil cuando se necesitan cargar grandes volúmenes de datos de manera eficiente en nuestra colección de MongoDB.



### Integración con Elasticsearch

#### Diseño de documentos

En nuestro proyecto, cada documento dentro de la colección **`estaciones`** representará una estación de servicio. Los campos incluyen **`Empresa`**, **`Direccion`**, **`CodigoPostal`**, **`Latitud`**, **`Longitud`**, **`Localidad`**, **`Municipio`**, **`Provincia`**, **`FechaPrecios`**, **`Horario`**, **`TipoEstacion`**, **`TipoVenta`**, y **`Margen`**, todos mapeados directamente a atributos en el documento.

Además, el campo **`Precios`** se manejará como un array de subdocumentos dentro de cada documento de estación, donde cada subdocumento contendrá detalles específicos sobre **`carburante`** y **`precio`**.



#### Estructura del documento

La estructura del documento en Elasticsearch se define de la siguiente manera, utilizando un mapeo que especifica los tipos de datos para cada campo:



```json
{
  "mappings": {
    "properties": {
      "Empresa": {"type": "text"},
      "Direccion": {"type": "text"},
      "CodigoPostal": {"type": "text"},
      "Latitud": {"type": "float"},
      "Longitud": {"type": "float"},
      "Localidad": {"type": "text"},
      "Municipio": {"type": "text"},
      "Provincia": {"type": "text"},
      "FechaPrecios": {"type": "date","format": "yyyy-MM-dd"},
      "Horario": {"type": "text"},
      "TipoEstacion": {"type": "text"},
      "TipoVenta": {"type": "text"},
      "Margen": {"type": "text"},
      "Precios": {
        "type": "nested",
        "properties": {
          "carburante": {"type": "text"},
          "precio": {"type": "float"}
        }
      }
    }
  }
}
```

<center><i>Mapping del documento JSON</i></center>



#### Creación del índice

Para crear el índice denominado ***service_stations***, utilizaremos una petición **`HTTP PUT`**, seguida de la especificación del mapeo del documento:

```http
PUT {{elasticsearch-host}}/service_stations       
```

  

#### Carga de datos

La inserción de datos en el índice ***service_stations\*** puede realizarse de dos maneras:



1. **Inserción individual de datos**:

   Cada documento se introduce individualmente siguiendo la estructura definida en el mapeo:

   ```http
   POST {{elasticsearch-host}}/service_stations/_doc
   ```

   ```json
   {
     "Empresa":"SAN ISIDRO",
     "Direccion":"CALLE DEL VINO, 10",
     "CodigoPostal":"02636",
     "Latitud":39.293500,
     "Longitud":-2.059556,
     "Localidad":"VILLALGORDO DEL JUCAR",
     "Municipio":"VILLALGORDO DEL JÚCAR",
     "Provincia":"ALBACETE",
     "FechaPrecios":"2023-10-30",
     "Horario":"L-D: 24H",
     "TipoEstacion":"Terrestre",
     "TipoVenta":"Vehículos terrestres",
     "Margen":"N",
     "Precios":[
         {"carburante":"gasóleo B",
         "precio":1.144}
     ]
   }
   ```

   <center><i>Ejemplo de un documento</i></center>

   

2. **Inserción masiva de datos**:

   Utilizamos el comando **`_bulk`** para la inserción masiva de datos. Esta operación requiere un archivo JSON previamente preparado con todos los documentos a insertar. El comando a ejecutar sería:

   ```cmd
   curl –XPUT 'NUESTRA_URL_DE_BONSAI/_bulk' ––data–binary @NUESTRO_ARCHIVO.json –H 'Content-Type: application/json'
   ```

   



## Proceso ETL (*Extract, Transform, Load*)

En la primera actividad, desarrollamos una base de datos relacional en MySQL para almacenar los precios de los combustibles de las estaciones de servicio en España. Esta base de datos fue diseñada para capturar y organizar los datos de manera estructurada y eficiente.

Avanzando hacia una segunda fase, nuestro objetivo es trascender las limitaciones de las bases de datos relacionales al migrar nuestra base de datos existente a una solución NoSQL, específicamente a Elasticsearch. Aunque Elasticsearch se conoce principalmente como un motor de búsqueda y análisis, su capacidad para manejar grandes volúmenes de datos lo convierte en una opción atractiva para nuestras necesidades de almacenamiento y consulta de datos.

La idea central de este paso es aprovechar la eficiencia de Elasticsearch en la gestión y recuperación de datos para proporcionar un servicio accesible y rápido de consulta de precios de combustibles. La elección de este enfoque se basa en la premisa de que nuestro sistema se centrará principalmente en operaciones de consulta, donde Elasticsearch destaca por su rendimiento.

Como continuación de nuestra primera actividad, hemos optado por utilizar Java y JPA para implementar el proceso ETL completo. Esta decisión se fundamenta en la familiaridad y el éxito previo con estas tecnologías en la fase inicial del proyecto.

Para llevar a cabo esta migración y establecer un sistema de consulta eficiente, hemos introducido un nuevo paquete denominado com.unir.app.elasticsearch. Las tareas clave en esta fase incluyen:

1. **Exportar la Base de Datos Relacional a un Fichero JSON**: Convertiremos los datos estructurados de nuestra base de datos MySQL en un formato JSON, que es más adecuado para trabajar con Elasticsearch.
2. **Crear un Índice en Elasticsearch con los Datos del Fichero JSON**: Para la creación del índice en Elasticsearch, utilizamos un fichero específico, **estacionesMapping.json**, que define la estructura del índice. Este fichero es esencial para asegurar que el índice se construya de acuerdo con nuestras necesidades específicas, lo que permite una organización y búsqueda eficiente de los datos. A través de este mapeo, adaptamos la estructura del índice para que se alinee con los formatos de los datos contenidos en el fichero JSON generado.
3. **Carga de Datos en Elasticsearch Utilizando el Fichero JSON**: El último paso implica la carga efectiva de los datos en Elasticsearch, utilizando el fichero JSON previamente creado. Este proceso garantiza que los datos estén completamente integrados en la plataforma Elasticsearch, listos para ser consultados y analizados.

Esta metodología nos permite combinar lo mejor de ambos mundos: la estructura y organización de una base de datos relacional y la agilidad y escalabilidad de una solución basada en Elasticsearch.



### Extracción de los datos del esquema relacional

En el proceso de integración de datos con Elasticsearch a través de [Bonsai](https://bonsai.io/), se destaca la clase **`ExportarEstacionesElasticsearch`** en el paquete `com.unir.app.elasticsearch`. Esta clase es central en la gestión de datos de estaciones de servicio españolas, abarcando la exportación, transformación y carga de datos. En esta fase, nos enfocamos en la exportación, donde se extraen datos de una base de datos relacional y se convierten a un formato JSON compatible con Elasticsearch. Este paso es crucial para asegurar que la estructura y formato de los datos sean idóneos para su eficiente carga y consulta en Elasticsearch, aprovechando las capacidades del servicio de [Bonsai](https://bonsai.io/).

El código proporcionado se divide en dos partes principales:

- **Función `crearJsonEstaciones()`**: Esta función se encarga de generar un archivo JSON que contiene información sobre estaciones de servicio. El proceso es el siguiente:
  - **Consulta de Datos**: Se prepara y ejecuta una consulta SQL para obtener datos de estaciones de servicio. La consulta recoge información detallada sobre cada estación, como el nombre de la empresa, dirección, coordenadas geográficas, localidad, municipio, provincia, y otros detalles.
  - **Generación de Archivo JSON**: Utiliza la librería Gson para convertir los datos obtenidos en objetos JSON. Cada estación se representa como un objeto JSON con varios campos como `Empresa`, `Direccion`, `CodigoPostal`, etc. También se obtienen y añaden los precios de los diferentes tipos de combustible para cada estación, llamando a la función **`obtenerPreciosPorEstacion()`**.
  - **Escritura en Archivo**: Los objetos JSON creados se escriben en un archivo, creando así un fichero JSON que representa todas las estaciones y sus datos relevantes.
- **Función** **`obtenerPreciosPorEstacion()`**: Esta función obtiene los precios de los diferentes tipos de combustibles para una estación específica.
  - **Consulta de Precios**: Realiza una consulta SQL para obtener los precios de los diferentes tipos de carburantes en una estación dada, identificada por estacionId.
  - **Lista de Precios**: Crea y devuelve una lista de objetos Precio, cada uno representando un tipo de combustible y su precio en la estación especificada.



### Creación de índice

La clase **`ExportarEstacionesElasticsearch`** incluye un método denominado **`crearIndiceEnElasticsearch()`**, que es esencial para configurar nuestra base de datos en Elasticsearch usando [Bonsai](https://bonsai.io/). Su función principal es crear un índice con el mapeo adecuado, asegurando una organización y accesibilidad óptimas de los datos en el entorno de Elasticsearch.

1. **Construcción de la URL de Elasticsearch**: Primero, el método construye la URL necesaria para crear el índice en Elasticsearch, utilizando la dirección del host de [Bonsai](https://bonsai.io/). Esta URL especifica el nombre del índice que se va a crear, en este caso, estaciones.

2. **Lectura del Archivo de Mapeo**: El método continúa leyendo el contenido del archivo de mapeo **`estacionesMapping.json`**. Este archivo contiene la configuración y estructura (*mapping*) que se desea para el índice en Elasticsearch. El mapeo define cómo se deben almacenar e indexar los datos en el índice, incluyendo los tipos de datos de cada campo y cómo deben ser tratados por Elasticsearch.

3. **Creación del Cliente HTTP**: Se crea un cliente HTTP para realizar la comunicación con el servidor de Elasticsearch. Esto es necesario para enviar peticiones HTTP a Elasticsearch y manejar las respuestas.

4. **Configuración y Envío de la Petición HTTP**: El método configura una petición HTTP de tipo PUT, que se utiliza para crear o reemplazar recursos en un servidor. Esta petición se dirige a la URL construida previamente y lleva como contenido el mapeo del índice en formato JSON. La petición se envía al servidor de Elasticsearch.

5. **Manejo de la Respuesta**: Tras enviar la petición, el método recibe y procesa la respuesta del servidor. Esto incluye verificar el código de estado HTTP para determinar si la petición fue exitosa o no. El código de estado y otros detalles de la respuesta pueden ser utilizados para la depuración o confirmación de que el índice se ha creado correctamente.

   ```json
   {
     "mappings": {
       "properties": {
         "Empresa": {"type": "text"},
         "Direccion": {"type": "text"},
         "CodigoPostal": {"type": "text"},
         "Latitud": {"type": "float"},
         "Longitud": {"type": "float"},
         "Localidad": {"type": "text"},
         "Municipio": {"type": "text"},
         "Provincia": {"type": "text"},
         "FechaPrecios": {"type": "date","format": "yyyy-MM-dd"},
         "Horario": {"type": "text"},
         "TipoEstacion": {"type": "text"},
         "TipoVenta": {"type": "text"},
         "Margen": {"type": "text"},
         "Precios": {
           "type": "nested",
           "properties": {
             "carburante": {"type": "text"},
             "precio": {"type": "float"}
           }
         }
       }
     }
   }
   ```

   <center><i>Mapping del documento estacionesMapping.json</i></center>



### Carga de datos

El último método en el proceso ETL dentro de la clase **ExportarEstacionesElasticsearch** es **importarJson()**. Este método se centra en la importación de datos desde un archivo JSON a Elasticsearch.

1. **Construcción de la URL para la Importación Masiva**: El método comienza estableciendo la URL para la operación de carga masiva **`_bulk`** en Elasticsearch, utilizando el *endpoint* proporcionado por el servicio [Bonsai](https://bonsai.io/). Esta URL es crucial para asegurar que los datos se envíen al lugar correcto y de la manera adecuada.
2. **Preparación del Archivo JSON**: Se prepara el archivo JSON que contiene los datos a ser importados. Este archivo ya ha sido generado previamente y contiene la información estructurada de las estaciones de servicio en el formato requerido por Elasticsearch.
3. **Creación del Cliente HTTP y Configuración de la Petición**: El método procede con la creación de un cliente HTTP, necesario para la comunicación con el servidor de Elasticsearch/Bonsai. Se configura una petición HTTP de tipo PUT, que incluye el archivo JSON como entidad. Esta configuración es esencial para enviar correctamente los datos al servidor.
4. **Envío de la Petición y Manejo de Respuestas**: La petición se envía y el método espera una respuesta del servidor. Se captura y registra el código de estado de la respuesta HTTP para verificar si la operación de carga fue exitosa o si hubo algún error.
5. **Manejo de Excepciones**: En caso de errores durante el proceso, como problemas de conexión o fallos en el servidor, el método captura la excepción y lanza una **`RuntimeException`**, lo que ayuda en la identificación y el manejo de posibles problemas durante la carga de datos.



## Prueba de inserción con MongoDB

En la sección "Desarrollo" de nuestro documento, exploramos la integración con MongoDB como una extensión natural de nuestro proyecto. Animados por esta posibilidad, decidimos llevar a cabo una prueba práctica para demostrar la creación de documentos en MongoDB desde Java. Este ejercicio no solo sirve para validar nuestra comprensión teórica, sino también para reforzar nuestras habilidades prácticas en la integración de bases de datos NoSQL en aplicaciones Java. Los pasos seguidos para esta prueba fueron:



### Instalación del Driver de MongoDB:

Comenzamos incluyendo el driver de MongoDB en nuestro proyecto. Para ello, añadimos la dependencia necesaria en el archivo **`pom.xml`**. Esta adición asegura que todas las funcionalidades y operaciones específicas de MongoDB estén disponibles para su uso en nuestro entorno de desarrollo Java.



### Creación de la Clase MongoConnector.java:

Desarrollamos la clase MongoConnector.java dentro del paquete **`com.unir.config`**. Esta clase es fundamental, ya que establece la conexión con nuestra base de datos MongoDB, actuando como un puente entre nuestra aplicación Java y MongoDB.



### Configuración de la Variable de Entorno MONGO_URL:

Configuramos la variable de entorno **`MONGO_URL`** en nuestro IDE. Esta variable contiene la cadena de conexión a nuestra base de datos MongoDB Atlas, asegurando un acceso seguro y autenticado. La utilización de una variable de entorno para la cadena de conexión es una práctica recomendada para mantener la seguridad de las credenciales.

 

### Inserción de Documentos con la Clase InsertarDocMongo:

Implementamos la clase **`InsertarDocMongo`** en el paquete **`com.unir.app.write`**. Esta clase es crucial, ya que es donde se lleva a cabo la inserción de documentos en MongoDB. Para mantener la coherencia con el tema de nuestro proyecto, insertamos datos de una estación de servicio en la base de datos. Hemos nombrado la base de datos como “Gasolina” y la colección de estaciones como “LaGasolinera”. La elección de estos nombres no solo sigue la temática del proyecto, sino que también facilita la identificación y el manejo de los datos relacionados con estaciones de servicio.



## Configuración del entorno JAVA

Para facilitar la integración efectiva con Elasticsearch a través del servicio [Bonsai](https://bonsai.io/) y la gestión de datos de estaciones de servicio, se ha configurado un entorno Java específico. Esta configuración implica la instalación de librerías esenciales y la definición de variables de entorno críticas.

### Instalación de librerías

En el desarrollo de este proyecto, se han empleado dos librerías fundamentales:

- **`com.google.gson`**: Esta librería es utilizada extensamente para manejar operaciones relacionadas con JSON, como la creación, manipulación y conversión de datos en este formato. Su uso es clave para el proceso de transformación de datos de la base de datos relacional a JSON, y posteriormente para su manipulación antes de la carga en Elasticsearch.

- **`org.apache.http`**: Se emplea para realizar peticiones HTTP al servicio [Bonsai](https://bonsai.io/). Esta librería es esencial para la creación del índice en Elasticsearch y para la carga de datos. Facilita la comunicación entre nuestra aplicación Java y el servidor de Elasticsearch alojado en [Bonsai](https://bonsai.io/).



> [!NOTE]
>
> <p style=color:blue>Aunque las referencias a estas librerías se incluyeron en el archivo pom.xml, fue necesario realizar una instalación adicional en el equipo local. El IDE utilizado alertó sobre esta necesidad y proporcionó un asistente (<i>wizard</i>) para facilitar este proceso de instalación.</p>



### Variables de entorno

Para la correcta configuración del entorno y la seguridad de las credenciales, se han definido tres variables de entorno importantes:

- **`MYSQL_USER`**: Se utiliza para almacenar el nombre de usuario de MySQL, permitiendo así la conexión segura y autenticada a la base de datos relacional.
- **`MYSQL_PASSWORD`**: Contiene la contraseña del usuario de MySQL, asegurando que el acceso a la base de datos sea seguro y esté protegido.
- **`HOST_BONSAI_ELASTICSEARCH`**: Esta variable almacena la URL del host de Bonsai.io, que es fundamental para la conexión con el servicio de Elasticsearch en [Bonsai](https://bonsai.io/), tanto para la creación del índice como para la carga de datos.



## Conclusiones

Tras concluir este proyecto, nos encontramos con un sistema robusto y flexible de gestión de datos, que integra las fortalezas de MongoDB y Elasticsearch. La elección inicial de un esquema relacional adecuado y su posterior transformación a un modelo NoSQL en MongoDB han establecido una base sólida para nuestro almacenamiento de datos. La integración con Elasticsearch ha añadido una capa de eficiencia y poder en la búsqueda y análisis de datos, lo que nos permite manejar consultas complejas con facilidad y precisión.

Este proyecto no solo ha sido un testimonio de la integración exitosa de diferentes tecnologías de bases de datos, sino que también ha sido una valiosa experiencia de aprendizaje. Nos ha enseñado la importancia de adaptar la elección de la tecnología a las necesidades específicas de los datos y las consultas, y no al revés. Además, ha reforzado la idea de que la eficiencia en la gestión de datos no se logra solo a través de la elección de una buena tecnología, sino también a través de una buena planificación, diseño y ejecución.

Mirando hacia el futuro, vemos este proyecto como un punto de partida para exploraciones más profundas en el campo de las bases de datos. Las lecciones aprendidas aquí serán fundamentales para futuros proyectos y desarrollos, y los conocimientos adquiridos sobre MongoDB y Elasticsearch serán herramientas valiosas en nuestro arsenal tecnológico.





<center>by <strong>Jose Manuel Pinillos</strong></center>
