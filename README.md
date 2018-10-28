# ncb-exchange-rate-client

Es una utileria cliente que consulta el servicio web SOAP de la tasa de cambio del Banco Central de Nicaragua (BCN) y retorna la tasa de cambio para la fecha o mes-año consultado.

Este proyecto se puede usar como una librería de terceros o una aplicación de consola. El corazón de la librería es la clase `ExchangeRateClient.java` que funciona como un `ServiceFacade` para hacer peticiones al servicio web del BCN. Esta clase accede las dos operaciones disponibles en el servicio web del BCN, lo cual permite obtener la tasa de cambio de una fecha determinada o la de un mes-año.

#### Observacion

El servicio web del BCN tiene una restricción con el año que se puede consultar: sólo se pueden obtener las tasas de cambio del año `2012` en adelante. Esta validación está contemplada dentro del proyecto, pero puede representar una ventaja o desventaja. Desventaja porque en el futuro este valor puede ser cambiado arbitrariamente y sin previo aviso por el BCN.

## Stack

- Java 8+.
- Maven 3+.
- JUnit 5+.
- El IDE de tu preferencia: el proyecto no incluye ningún archivo específico de un IDE, pero requiere el uso de un IDE con soporte `maven`.

Los pasos descritos para el uso de la librería requieren de tener `maven` instalado.

## Usar el proyecto como una librería

La razón de ser del proyecto es que sea usado como cualquier librería de `java`. El usuario puede importar el jar para que su fuente esté disponible para su uso dentro del proyecto en el que esté trabajando.

Si el proyecto en el que estás trabajando es un proyecto basado en `maven`, se deben seguir los siguientes pasos:

1. Clonar el proyecto o descargarlo como zip. Si se descarga el zip, descomprimirlo en una ruta específica.

        git clone https://github.com/jug-ni/ncb-exchange-rate-client.git
        cd ncb-exchange-rate-client
        mvn install
        # Si no se desean ejecutar los test unitarios durante la instalación del jar en el repositorio local
        mvn install -DskipTests

2. Incluir la librería como dependencia en el archivo pom.xml de tu proyecto:

        <dependency>
            <groupId>ni.jug</groupId>
            <artifactId>ncb-exchange-rate-client</artifactId>
            <version>${version.descargada}</version>
        </dependency>

Si no estás seguro sobre el número de versión una vez clonado el proyecto (el número de versión está en el archivo pom.xml), dirigirse a su repositorio local de `maven` y explorar la ruta `.m2/repository/ni/jug` en tu cuenta de usuario del SO y tomar nota de la versión del jar.

Si tu proyecto no usa `maven`, ejecutar el paso 1 e importar el jar desde tu repositorio local de `maven` en la ruta mencionada.

Ejemplo de uso:

        ExchangeRateClient client = new ExchangeRateClient();
        Assertions.assertEquals(new BigDecimal("31.9396"), client.getExchangeRate(LocalDate.of(2018, 10, 1)));

        MonthlyExchangeRate monthlyExchangeRate = client.getMonthlyExchangeRate(2018, 10);
        Assertions.assertEquals(31, monthlyExchangeRate.size());
        Assertions.assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        Assertions.assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        Assertions.assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        Assertions.assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        Assertions.assertFalse(monthlyExchangeRate.getThereIsAGap());

Referirse a los [test unitarios][test unitario] para más ejemplos.

## Uso del CLI (Línea de comandos o Terminal)

Si se prefiere usar el proyecto como una aplicación cli, se tiene la opción de solicitar la tasa de cambio para: una fecha, rango de fechas, lista de fechas; para un mes-año, rango de mes-año, lista de mes-año. El cli es un `wrapper` de la clase `ExchangeRateClient.java`. Para ejecutar el cli se requiere tener instalado maven y java con las versiones indicadas.

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

Clonar el proyecto o descargarlo como zip. Si se descarga el zip, descomprimirlo en una ruta específica.

        git clone https://github.com/jug-ni/ncb-exchange-rate-client.git
        cd ncb-exchange-rate-client
        mvn package
        # Si no se desean ejecutar los test unitarios durante el empaquetamiento del jar
        mvn package -DskipTests
        # Ejecutar la aplicación. Referirse a los ejemplos anteriores para mayor información
        cd target/
        java -jar ncb-exchange-rate-client-1.0-SNAPSHOT.jar -date=2018-10-23

## Disclaimer

Este utilería depende de la disponibilidad del servicio web ofrecido por el Banco Central de Nicaragua, el cual puede o no estar disponible al momento de realizar las consultas.

## Licencia

This software is covered under the MIT Licence (http://opensource.org/licenses/MIT).

Puedes leer el archivo de la licencia en [LICENSE][license]

Copyright (c) 2018-present, JUG Nicaragua Armando Alaniz

**Free Software, Hell Yeah!**

[license]: LICENSE.txt
[test unitario]: src/test/java/ni/jug/ncb/exchangerate/ExchangeRateClientTest.java
