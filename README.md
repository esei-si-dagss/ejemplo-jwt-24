## Ejemplo de securización de endpoints REST con JWT



* Versión simplificada del tutorial (https://www.bezkoder.com/spring-boot-jwt-authentication/)
* Pendiente: lado cliente con REACT (ver (https://www.bezkoder.com/react-hooks-jwt-auth/))

### Estructura del proyecto

* Fichero `pom.xml`: con dependencia `spring-boot-starter-security` [habilita soporte de Spring Security en Spring Boot] y `io.jsonwebtoken.*` [libreria JJWT ([JSON Web Token for Java and Android](https://github.com/jwtk/jjwt))]
* Fichero `application.properties`: define dos atributos propios (`ejemlojwt.clave` [clave secreta para firmas HMAC] y `ejemplojwt.caducidad` [caducidad del token JWT])
* Paquetes `entidades` y  `daos`:  definen la entidad `Usuario`(que tiene vinculada una lista de `Roles`) y el DAO para gestonarla (`UsuarioDAO`)
* Paquete `controllers`: 
  - `AutenticacionController`: define la URI para registro de _Usuarios_ (`/api/auth/registro`) y para _login_ de un _Usuario_ (`/api/auth/login`)
     - El método `login(...)` verifica "manualmente" las credenciales recibidas (`login` y `password` recibidos en un DTO `DatosLogin`)
        1. Comprueba que el `login` aportado existe y, usando el `PasswordEncoder`,  que el `password` proporcionado coincide con el almacenado. 
        2. Si la autenticación tiene éxito devuelve el TOKEN JWT correspondiente al _Usuario_ logueado (firmado usando HMAC con SHA256 y la clase secreta del lado servidor). 
  - `PruebaController`: define una serie de URIs con el acceso limitado a _Usuarios_ autenticados con  _Roles_ concretos (métodos anotados con `@PreAutorize(...)`)
  - Paquete `dtos`: definición de los DTOs usados por los `@RestController` anteriores.
* Paquete `seguridad`: elementos para configurar la securización del API REST mediante _Spring Security_.
  - Paquete `autenticacion`: implementaciones propias de los _interfaces_ `UserDetailsService` y `UserDetails`que usan la entidad `Usuario` y delegan las consulta sobre la BD en `UsuarioDAO`
  - Paquete `jwt`:
      - `UtilidadesJWT`: clase de utilidades para gestionar [crear y validar] TOKENS JWT (delega en la librería [JJWT](https://github.com/jwtk/jjwt) incluida en `pom.xml`) 
      - `FiltroAutenticacionJWT`: _filtro_ a incluir en la cadena de _filtros_ de _Spring Security_
         1. Captura el TOKEN JWT de la cabecera de la petición HTTP recibido y  lo valida (en el ejemplo sólo se valida la firma HMAC)
         2. Con la información de _Usuario_ extraida del TOKEN se actualiza el _SecurityContext_ para que _Spring Security_ trate esta petición HTTP como una petición autenticada y le aplique las restricciones de autorización que correspondan al _Usuario_ identificado por el TOKEN recibido
         3. Deja seguir el procesamiento de la petición HTTP sobre la cadena de _filtros_ de _Spring Security_ para que el resto de componentes procesen la petición HTTP
  - `WebSecurityConfig`: clase de configuración de _Spring Security_ (anotada con`@EnableWebSecurity`)
      - Expone los `@Bean` con el `PasswordEncoder` a usar, el `FiltroAutenticacionJWT` y el `SecurityFilterChain` configurado
      - Mediante el objeto `HttpSecurity` de `filterChainConfig` se definen las características del `SecurityFilterChain`  a crear :
         1. Implementación propia del _interface_ `UserDetailsService` (comprobación de la autenticación)
         2. Autorizaciones de acceso para las URI del proyecto (`requestMatchers(...)`)
         3. Registro del `FiltroAutenticacionJWT`, ubicándolo justo antes del _filtro_  por defecto `UsernamePasswordAuthenticationFilter`, que captura los TOKENs JWT recibidos y los  procesa
         


### Ejemplo de uso


En un terminal: arrancar la aplicación
```sh
cd ejemplo-jwt-24
mvn spring-boot:run
```

En otro terminal: autenticarse con `pedro/pedro` y comprobar el acceso
```sh
curl -v --header 'Content-Type: application/json' --data '{"login":"pedro", "password":"pedro"}' --request POST  http://localhost:8080/api/auth/login

Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /api/auth/login HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.74.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 37
> 
* upload completely sent off: 37 out of 37 bytes
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Thu, 16 Dec 2021 15:38:02 GMT
< 
* Connection #0 to host localhost left intact
{"token":"XXXXXXXXXXXXXXXX.YYYYYYYYYYYYYYYYYYYYYYY.ZZZZZZZZZZZZ","id":3,"login":"pedro","roles":["ROLE_GERENTE","ROLE_USUARIO"]}

export TOKEN=XXXXXXXXXXXXXXXX.YYYYYYYYYYYYYYYYYYYYYYY.ZZZZZZZZZZZZ

# Accesos autenticado y con permiso
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/prueba/usuario
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/prueba/gerente

# Acceso autenticado sin permiso
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/prueba/administrador

# Acceso no autenticado
curl -v http://localhost:8080/api/prueba/usuario
```

Comprobar en [jwt.io](https://jwt.io/) el contenido del Token JWT recibido.
