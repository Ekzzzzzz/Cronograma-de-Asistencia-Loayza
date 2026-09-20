---
titulo: ADR-004 — Despliegue en Azure con crédito de estudiante
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-06)
  - Archivos_de_clase/S08_s1-Patrones-Estructurales-AF.pptx
actualizado: 2026-09-06
tags: [adr, decision, azure, despliegue, costos, infraestructura]
---

# ADR-004 — Despliegue en Azure con crédito de estudiante

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

El usuario dispone de **100 USD de crédito en Azure** por su cuenta de estudiante.
[[adr-002-arquitectura-produccion]] había fijado "VPS en la nube" de forma genérica y "fotos
en el disco del servidor"; con un proveedor concreto y un presupuesto cerrado, las dos
decisiones se pueden afinar.

El dato que ordena todo: **el crédito es de 100 USD y vence a los 12 meses**, lo que sea que
ocurra primero. La cuenta de estudiante no lleva tarjeta asociada, así que al agotarse el
crédito **los recursos se detienen; no se factura de más**. Para un entregable de curso eso
es cómodo. Para el control de asistencia de una empresa real es un **acantilado con fecha**.

## Decisiones

### 1. Una sola máquina virtual con todo dentro

**VM Linux B1s** (1 vCPU, 1 GB RAM, Ubuntu LTS) ejecutando Nginx, la aplicación Spring Boot y
MySQL 8 en el mismo host.

Se descartó separar la base de datos: **Azure Database for MySQL Flexible Server**, en su SKU
más barato (Burstable B1ms), cuesta del orden de 12–14 USD al mes él solo. Consumiría el
crédito entero en siete meses sin haber pagado todavía ni el servidor de aplicación. A 400
marcaciones diarias ([[adr-002-arquitectura-produccion]]) no hay ninguna razón técnica para
separarlas.

> **Riesgo asumido: 1 GB de RAM es ajustado.** Ubuntu, MySQL, la JVM y Nginx conviviendo en
> 1 GB exige afinar: `-Xmx384m` en la JVM, `innodb_buffer_pool_size=192M` y
> `performance_schema=OFF` en MySQL, y 2 GB de swap. A esta carga funciona. Si diera
> problemas, la salida es **B1ms** (2 GB), que **duplica el costo de la VM**.

### 2. Las fotos van a Azure Blob Storage, no al disco de la VM

**Esto revisa la decisión 2 de [[adr-002-arquitectura-produccion]]**, que las ponía en el
disco del servidor. Lo esencial de aquella decisión se mantiene —**nunca como `BLOB` dentro de
MySQL**— pero en Azure el almacenamiento de objetos gana por tres motivos, y el primero es que
es más barato que el disco:

| Opción | Costo mensual aproximado |
|---|---|
| Disco administrado adicional, 64 GB Standard SSD | ~4,80 USD |
| **Blob Storage, nivel caliente, ~12 GB** | **~0,22 USD** |

Los otros dos motivos importan más que el dinero:

- **Sobrevive a la VM.** Las fotos son evidencia de jornada laboral. Si hay que recrear el
  servidor, borrar el disco no puede llevarse la evidencia por delante.
- **Elimina el modo de fallo "disco lleno"**, que en una VM de 1 GB de RAM y disco pequeño es
  el desenlace más probable de un descuido.

El volumen es bajo gracias a la retención de 3 meses que fijó ADR-002: en régimen estable son
~12 GB, no los ~30 GB anuales que crecerían sin purga. **La decisión de retención es la que
vuelve viable el presupuesto.**

> **El cambio no toca el diseño.** ADR-002 ya obligaba a pasar por una interfaz
> `AlmacenFotos` precisamente para esto. Cambia la implementación, no los llamadores.

### 3. HTTPS gratuito, sin comprar dominio

[[requisitos]] RNF-05 exige HTTPS o el navegador móvil no da acceso a la cámara. Se resuelve
sin costo con la **etiqueta DNS** que Azure asigna a la IP pública
(`<nombre>.<region>.cloudapp.azure.com`) y un certificado **Let's Encrypt** renovado
automáticamente.

Un dominio propio (~12 USD al año) queda como mejora opcional cuando el sistema pase a manos
de la empresa: es más presentable para las trabajadoras, pero no es un requisito técnico.

### 4. Respaldo a Blob, no Azure Backup

**Azure Backup** para la VM cuesta del orden de 5 USD al mes: la mitad del presupuesto mensual
disponible. Se descarta.

En su lugar, `mysqldump` nocturno comprimido y subido a un contenedor Blob en **nivel frío**
(~0,01 USD por GB). El respaldo de una base de decenas de MB cuesta céntimos. Las fotos ya
están en Blob, que replica tres copias por sí mismo.

### 5. Región: Este de EE. UU. 2

Brasil Sur está más cerca de Lima —unos 40 ms contra 120–140 ms— pero es notablemente más
caro. Para un formulario de cinco pasos que se envía una vez por marcación, esa latencia es
imperceptible; el sobrecosto no lo sería.

## El presupuesto, y por qué no llega al año

Precios aproximados de lista, Linux, pago por uso. **Hay que verificarlos en la calculadora de
Azure antes de comprometerse**: cambian y varían por región.

| Recurso | Costo mensual |
|---|---|
| VM B1s | ~7,59 USD |
| Disco de sistema, 32 GB | ~1,54 USD (HDD) a ~2,40 USD (SSD) |
| **IP pública estática estándar** | **~3,65 USD** |
| Blob Storage, fotos + respaldos | ~0,25 USD |
| Salida de datos (primeros 100 GB) | 0 USD |
| **Total** | **~13 a 14 USD al mes** |

**El crédito rinde unos 7 meses así.** Con la VM reservada a un año (~40 % de descuento) y
disco HDD baja a ~10 USD y rinde unos 10. Solo llega a superar los 12 meses si el nivel
gratuito de la cuenta cubre las 750 horas mensuales de la B1s.

> **Hueco:** no está verificado si la suscripción *Azure for Students* incluye los servicios
> gratuitos de 12 meses de la cuenta gratuita —750 h de B1s, discos, 5 GB de Blob— o si
> únicamente entrega el crédito. **Cambia el resultado por un factor de dos.** Hay que
> comprobarlo en el portal antes de dimensionar. Ver [[huecos-abiertos]].

**Dos consecuencias que no son técnicas:**

1. **Configurar alertas de presupuesto** en Cost Management al 25, 50 y 75 % del crédito. Un
   recurso mal dimensionado se come 100 USD mucho más rápido de lo que parece.
2. **Planificar el traspaso.** Antes de que el crédito venza, la empresa tiene que asumir la
   facturación en su propia suscripción, o el sistema hay que mudarlo. Un control de
   asistencia que se apaga sin aviso es peor que no tenerlo.

## Lo que esto le hace al diseño de patrones

**Aparece un uso legítimo de [[patron-adapter]], y no depende del alcance.**

Adapter estaba en suspenso desde [[adr-001-patrones-seleccionados]]: solo entraba si había que
importar los Excel históricos. Azure le da otro trabajo, este sí seguro.

El SDK de Azure Blob expone su propia interfaz —`BlobClient`, `BlobContainerClient`— que no se
parece a la que el dominio necesita. `AlmacenFotosBlob` **traduce** una en la otra: exactamente
lo que [[s08-adapter-facade]] define, con el mismo papel que el `OldInventorySystem` del
ejemplo del curso. Y hay una segunda implementación real, `AlmacenFotosLocal`, para desarrollar
sin tocar la nube.

> **Cuidado, y hay que decidirlo con honestidad.** Se puede objetar que esto no es Adapter sino
> simplemente *una implementación de una interfaz propia*, que es polimorfismo y no un patrón.
> La distinción está en si existe una interfaz ajena e incompatible que traducir: aquí existe,
> y es el SDK. **Pero si al escribirlo resulta que la clase es una delegación de tres líneas
> sin traducción real, entonces no es Adapter y no debe llamarse así en el informe.** Sería el
> Golden Hammer que [[s14-antipatrones]] describe.

Si Adapter entra, el proyecto pasa de 7 a **8 patrones**: los siete de PC-3 que sí encajan,
más Adapter, que PC-3 no pide. Los tres que faltan de su lista —Prototype, Bridge y Command—
tienen su descarte documentado con la cita del curso que lo respalda.

## Consecuencias

- Se revierte la decisión 2 de [[adr-002-arquitectura-produccion]]: las fotos van a Blob
  Storage, no al disco. La interfaz `AlmacenFotos` absorbe el cambio.
- Aparece un requisito operativo nuevo: **alertas de presupuesto y plan de traspaso**.
- Aparece una dependencia: el SDK de Azure Storage para Java.
- MySQL 8 se mantiene ([[adr-002-arquitectura-produccion]]), ahora explícitamente **en la
  misma VM**, no como servicio administrado.
- [[modelo-datos]] no cambia: `foto_ruta` y `foto_mini_ruta` pasan a guardar la clave del blob
  en vez de una ruta de disco. Es el mismo `VARCHAR`.
- El desarrollo local no necesita Azure: `AlmacenFotosLocal` escribe en una carpeta.

## Enlaces

[[adr-002-arquitectura-produccion]] · [[adr-003-login-y-cronograma-simple]] ·
[[modelo-datos]] · [[requisitos]] · [[s08-adapter-facade]] · [[huecos-abiertos]]
