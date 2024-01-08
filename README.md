Návod na spuštění programu
==========================
Požadavky
---------
- Java 17
- Maven 3.6.3

Spuštění
--------
1. V souboru /src/main/resources/connections.txt je potřeba nakonfigurovat všechny (i lokální) uzly systému. 
    Každý uzel musí být na samostatném řádku a musí obsahovat 
    následující informace oddělené mezerou:
    - ID uzlu
    - IP adresa uzlu
    - Port uzlu 

   Příklad:
   ```
    1 192.168.56.105 8080
    2 192.168.56.101 8080
    3 192.168.56.103 8080
    4 192.168.56.102 8080
    5 192.168.56.104 8080
    6 192.168.1.132 8080
    ```
2. Spustit uzel pomocí příkazu:
    ```
    mvn spring-boot:run -Dspring-boot.run.arguments="id"
    ```
   kde id je ID uzlu