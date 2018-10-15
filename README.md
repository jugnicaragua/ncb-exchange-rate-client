# ncb-exchange-rate-client

Es una utileria cliente que consulta el servicio web SOAP de la tasa de cambio del Banco Central de Nicaragua (BCN) y retorna la tasa de cambio para la fecha o mes-año consultado.

Este proyecto se puede usar como una librería de terceros o una aplicación de consola. El corazón de la librería es la clase `ExchangeRateClient.java` que funciona como un `ServiceFacade` para hacer peticiones al servicio web del BCN. Esta clase accede las dos operaciones disponibles en el servicio web del BCN: lo cual permite obtener la tasa de cambio de una fecha determinada o la de un mes-año.

El servicio web del BCN tiene una restricción con el año que se puede consultar: sólo se pueden obtener las tasas de cambio del año `2012` en adelante. Esta validación está contemplada dentro del proyecto, lo cual puede representar una ventaja o desventaja. Desventaja porque en el futuro este valor puede ser cambiado arbitrariamente y sin previo aviso por el BCN.

## Stack

- Java 8+.
- Maven 3+.
- El IDE de tu preferencia: el proyecto no incluye ningún archivo específico de un IDE, pero requiere el uso de un IDE con soporte Maven.


## Instalación

Si el proyecto en el que estás trabajando es un proyecto basado en `maven`, se deben seguir los siguientes pasos:

1. Clonar el proyecto o descargarlo como zip. Si se descarga el zip, descomprimirlo en una ruta específica.

        cd <ruta-proyecto>
        mvn install
        # Si no se desean ejecutar los test unitarios
        mvn install -DskipTests

2. Incluir la librería como dependencia en el archivo pom.xml de tu proyecto:

        <dependency>
            <groupId>ni.jug</groupId>
            <artifactId>ncb-exchange-rate-client</artifactId>
            <version>${version.descargada}</version>
        </dependency>

Para todos aquellos proyectos que no usan `maven`, ejecutar el paso 1 del apartado anterior e importar el jar desde tu repositorio local de `maven`.


## Uso del CLI

Si se prefiere usar el proyecto como una aplicación cli, se tiene la opción de solicitar la tasa de cambio para: una fecha, rango de fechas, lista de fechas; para un mes-año, rango de mes-año, lista de mes-año. El cli es un `wrapper` de la clase `ExchangeRateClient.java`. Para ejecutar el cli se requiere tener instalado Java 8+.

Opciones disponibles:

- date: una fecha, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM-dd.
- ym: un año-mes, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM.
- help: Muestra las opciones disponibles y ejemplos de uso.

Ejemplos:

        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16,2018-10-31

        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-10
        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-09:2018-10
        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-01,2018-09:2018-10

        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14: -ym=2018-10

        java -jar ncb-exchange-rate-client-<version>.jar --help


## Licencia

[MIT](http://opensource.org/licenses/MIT)

Copyright (c) 2018-present, JUG Nicaragua Armando Alaniz

**Free Software, Hell Yeah!**
