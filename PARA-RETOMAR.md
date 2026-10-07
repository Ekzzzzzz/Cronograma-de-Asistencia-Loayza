# Para retomar el proyecto

Estado a **2026-10-06**. El trabajo se paró el 2026-09-20 y se retoma en otro ordenador.

Este archivo es el punto de entrada. Si eres un asistente abriendo el proyecto en frío, lee
esto y después `Pattern Design/CLAUDE.md`, que es el contrato de trabajo y manda sobre
cualquier cosa que diga este archivo.

---

## 1. Qué es esto

Un sistema de control de asistencia para **Podología Loayza**, una cadena real de 7 sedes en
Lima. Las trabajadoras marcan entrada y salida desde el móvil con una foto; la administradora
ve un dashboard y exporta el cronograma a Excel.

Es a la vez el **entregable PC-3 del curso de Diseño de Patrones (UTP)**, lo que impone dos
cosas que no se negocian:

- **Arquitectura de cuatro capas** `vista / control / modelo / bd` — no `controller/service/
  repository`. Vale 2 puntos de la rúbrica.
- **Patrones de diseño repartidos entre esas capas.** Vale 6 puntos, el criterio más gordo.
- **Carpetas `MVC/` y `SQL/`** con esos nombres exactos: los exige el anexo.

Equipo: dos personas. Entrega: cuestión de semanas.

## 2. Lo primero que hay que leer

| Archivo | Qué es |
|---|---|
| `Pattern Design/CLAUDE.md` | **El contrato.** Reglas de trabajo, límites del entorno, trampas. Leer entero |
| `Pattern Design/wiki/log.md` | Bitácora. `grep "^## \[" wiki/log.md \| tail -5` para ver lo último |
| `Pattern Design/wiki/index.md` | Catálogo del wiki por contenido |
| `Pattern Design/wiki/decisiones/adr-012-sin-cargos-y-sin-rechazos.md` | **La decisión más reciente y la que más manda** |

## 3. Estado real del código

```
MVC/                              Spring Boot 4.1.1 · Java 25 · Maven Wrapper
├─ src/main/java/pe/loayza/asistencia/
│  ├─ modelo/      ✅ 14 clases, COMPLETO y probado (Composite + Singleton)
│  ├─ vista/       ❌ solo package-info.java
│  ├─ control/     ❌ solo package-info.java
│  └─ bd/          ❌ solo package-info.java
└─ src/test/       ✅ 22 pruebas, todas pasan

SQL/               ✅ COMPLETO: 6 tablas, 17 procedimientos, evento de purga, semillero
```

**Compila y las pruebas pasan.** Verificar con:

```bash
cd MVC && ./mvnw -B test        # Windows: .\mvnw.cmd -B test
```

### Lo que NO existe todavía

- Ninguna pantalla. Ni una plantilla Thymeleaf, ni un `@Controller`.
- Ningún `RowMapper`: el modelo Java **nunca ha visto una fila real** de MySQL.
- El generador de Excel (`.xlsx`). Falta la dependencia de Apache POI.
- El sellado de la foto, el almacén de fotos, la cola sin señal.

## 4. Puesta en marcha en un ordenador nuevo

**No hay ninguna credencial que traer de la otra máquina.** Los scripts las crean.

### 4.1 Requisitos

| Herramienta | Nota |
|---|---|
| **JDK 25 o superior** | El proyecto compila a `release 25` (LTS). Un JDK 26 también sirve |
| **MySQL 8.0** | El cliente `mysql` suele NO quedar en el PATH; buscarlo en `C:\Program Files\MySQL\MySQL Server 8.0\bin` |
| **Maven** | **NO se instala.** Se usa el wrapper: `./mvnw` o `.\mvnw.cmd`. Nunca `mvn` |

### 4.2 Crear la base de datos

En orden, como `root` y con la contraseña **de esa máquina**:

```bash
mysql -u root -p < SQL/01_esquema.sql          # base, usuario de la app y 6 tablas
mysql -u root -p asistencia_loayza < SQL/02_procedimientos.sql
mysql -u root -p asistencia_loayza < SQL/03_datos_prueba.sql
mysql -u root -p asistencia_loayza < SQL/04_purga_fotos.sql
```

El tercero tarda ~25 s: genera 99 trabajadoras y ~3 900 marcaciones. Imprime un resumen al
final; si sale, fue bien.

> En Windows, pasar el archivo por tubería va mejor que con `<`, y conviene forzar el juego de
> caracteres para que las tildes no se rompan:
> ```powershell
> Get-Content SQL\01_esquema.sql -Raw -Encoding UTF8 |
>   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p --default-character-set=utf8mb4
> ```

### 4.3 Credenciales

| Para qué | Usuario | Clave | Quién la crea |
|---|---|---|---|
| MySQL administrador | `root` | *la de esa máquina* | Quien instala MySQL |
| La aplicación | `asistencia` | `asistencia` | `01_esquema.sql`, línea 51 |
| Login del dashboard | `admin` | `loayza2026` | `03_datos_prueba.sql` (hash BCrypt real) |

`application.yml` usa `${DB_USER:asistencia}` / `${DB_PASS:asistencia}`: **sin variables de
entorno funciona solo**. Las tres son de desarrollo y están en un repositorio público a
propósito; la del servidor irá por variables de entorno y nunca a un archivo.

### 4.4 Comprobar que todo está bien

```bash
cd MVC && ./mvnw -B test
```

Tienen que pasar **22 pruebas**. Las 21 del modelo no necesitan MySQL; `contextLoads()` sí, así
que si falla solo esa, el problema es la base de datos.

## 5. Las cinco reglas que no se pueden romper

Si solo se leen cinco cosas antes de escribir código, son estas.

1. **El sistema NO RECHAZA NINGUNA MARCACIÓN.** Ninguna. No hay tope diario, ni cooldown, ni
   validación de secuencia: doble entrada, doble salida y veinte marcaciones seguidas se
   aceptan todas. *Que a alguien se le invalide una marcación es el peor fallo posible del
   sistema.* Lo raro se guarda y se anota en la tabla `incidencia` para que una persona lo
   mire. Ver ADR-012.

2. **La foto nunca bloquea la marcación.** Si la cámara falla o la imagen viene rota, **la
   marcación se guarda igual, sin foto**, y queda una incidencia. Corolario de diseño: hay que
   guardar la marcación **antes** de procesar la foto, nunca después — si no, una cola de
   fotos es una cola de personas esperando.

3. **Las horas se suman turno a turno, jamás restando los extremos.** Una jornada de 9–1 y 2–6
   son 8 horas, no 9. Está implementado y probado en `Jornada`; si alguna vez alguien calcula
   horas con la primera entrada y la última salida, el sistema empieza a pagar almuerzos.

4. **El emparejamiento de entradas y salidas vive SOLO en Java**, en `Jornada.de(...)`.
   `sp_registrar_marcacion` devuelve el día entero ordenado y no empareja nada. Escribir esa
   regla también en SQL es *Shotgun Surgery*.

5. **La capa `modelo` no importa Spring.** Ni un `@Component`. Es lo que hace que la separación
   de capas sea comprobable y que las pruebas corran en milisegundos sin base de datos.
   Spring se usa a fondo en `vista`, `control` y `bd`. Ver ADR-011.

Y una de dominio: **no existe `cargo` ni `tipo de jornada`.** Se quitaron en ADR-012. Si
aparecen en alguna página del wiki, esa página está desactualizada.

## 6. Trampas del entorno, ya pagadas una vez

- **Maven desapareció del disco** entre dos sesiones en la máquina anterior. Por eso se usa el
  wrapper. No reinstalar Maven, no usar `mvn`.
- **Spring Initializr genera `<version>4.1.1.RELEASE</version>`, y ese artefacto NO EXISTE** en
  Maven Central. La versión real es `4.1.1`, sin sufijo. Si el build falla con
  *«Non-resolvable parent POM»*, es esto.
- **En Boot 4 el starter web se llama `spring-boot-starter-webmvc`** y los starters de test van
  **partidos por función**: ya no hay un único `spring-boot-starter-test`.
- **Los heredocs de Bash fallan** en el entorno Windows del equipo (finales de línea CRLF).
  Para escribir archivos, usar la herramienta de escritura, no `cat <<EOF`.
- **Los PDF del curso** se leen con `python tools/extraer_pdf.py <ruta>`, no con el lector
  nativo.

## 7. Qué toca ahora

En este orden. El primero es el que desbloquea todo lo demás.

1. **La capa `bd`.** `RowMapper` y `SimpleJdbcCall` para los 17 procedimientos. Es donde el
   modelo Java ve su primera fila real. Cerrar el circuito
   `sp_registrar_marcacion` → `Jornada` → `sp_actualizar_jornada` dentro de un
   `@Transactional`, y probarlo contra MySQL.
2. **La capa `control`.** La fachada «registrar marcación» completa, y el generador de Excel
   con Apache POI — **diario y semanal, por sede**. Los dos procedimientos devuelven las mismas
   columnas a propósito para que el generador sea uno solo.
3. **La capa `vista`.** El formulario público de marcación y el dashboard con una pestaña por
   sede. Hay un prototipo de interfaz que al cliente le gustó mucho; está referenciado en
   ADR-008.
4. **Pruebas de integración por capa.** El informe las exige **con capturas por caso de
   prueba**. No dejarlas para el final.

## 8. Dos deudas pendientes que conviene no olvidar

- **El wiki contiene nombres reales de trabajadoras**, tomados del Excel del cliente. El
  repositorio es público. Hay que reemplazarlos por ficticios. El SQL ya está limpio: los 99
  nombres de `03_datos_prueba.sql` son inventados.
- **Pregunta abierta y la más importante de todas:** ¿cada cuánto va a revisar la
  administradora las incidencias pendientes? Si nadie las mira, toda la decisión de «aceptar
  todo y avisar» se queda a medias.

## 9. Ramas

El trabajo va en **`dev`**. Si `main` parece no tener `MVC/` ni `SQL/`, es que se quedó atrás:
`git log --oneline main..dev` lo dice en una línea.
