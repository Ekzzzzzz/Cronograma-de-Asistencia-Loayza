---
titulo: Huecos abiertos
tipo: sintesis
estado: borrador
fuentes:
  - brief del usuario (2026-09-05)
  - docs/Cronograma_Ejemplo.xlsx
actualizado: 2026-09-05
tags: [pendientes, decisiones, bloqueos]
---

# Huecos abiertos

Registro vivo de lo que falta saber, decidir o desbloquear. Se revisa en cada lint. Cuando
un hueco se cierra, se mueve a una página de [[decisiones]] y aquí se marca como resuelto.

## Bloqueos del entorno

**Actualizado 2026-09-05:** el toolchain quedó completo. Ya no hay bloqueos de entorno.

| Bloqueo | Estado | Nota |
|---|---|---|
| No hay Python 3 | ✅ **resuelto** | Python 3.14.7. Los tres scripts de `tools/` ya corren. |
| PDF ilegibles | ✅ **resuelto** | `python tools/extraer_pdf.py` extrae ~105 000 caracteres de PC-2 y PC-3. |
| No hay JDK | ✅ **resuelto** | Java y javac 26.0.2.1. |
| No hay Maven | ✅ **resuelto** | Maven 3.9.16 instalado a nivel de usuario, verificado con `mvn -v`. |
| `JAVA_HOME` sin definir | ✅ **resuelto** | Apunta a `C:\Program Files\Java\jdk-26.0.2.1`. |
| JDK 26 vs Spring Boot | ✅ **descartado** | Spring Initializr ofrece Java 26 con Boot 4.1.1. No hace falta otro JDK. |
| `CASO DE EJEMPLO 2.pdf` | ⚠️ **parcial** | Solo 510 caracteres extraíbles: es un PDF de imágenes. Necesitaría OCR. |

Un aviso que no bloquea pero muerde después: **`extraer_pdf.py` pierde tildes y ligaturas**
en algunos PDF (`presentacin`, `cient!co`). No citar literalmente de esos archivos sin
corregir el texto.

Todas las fuentes de `Archivos_de_clase/` son legibles salvo la excepción anotada, así que la
ingesta puede avanzar con el material completo.

## Preguntas para el usuario

1. **Identidad de la trabajadora.** ¿El nombre se escribe libre o se elige de una lista?
   Texto libre garantiza duplicados y rompe el agrupado del cronograma
   ([[formato-cronograma-excel]]).
2. **Acceso de la administradora.** ¿Lleva usuario y contraseña? El brief solo dice que la
   trabajadora entra por un enlace general.
3. **Descanso / No turno / Inasistencia.** El Excel distingue tres estados que no se pueden
   deducir de las marcaciones. ¿Existe un cronograma planificado previo, o la administradora
   los marca a mano en el dashboard?
4. **Geolocalización.** `tools/pluscode.py` tiene las coordenadas de las 7 sedes. ¿Se quiere
   validar que la marcación ocurra en la sede, o el script era solo exploratorio?
5. **Entrada sin salida.** Si una trabajadora marca entrada y nunca la salida, ¿qué muestra
   el cronograma?
6. **Alcance de la exportación.** El ejemplo es de una semana (lunes a domingo). ¿La
   exportación es siempre semanal, o hace falta también mensual?

## Preguntas nuevas tras ingerir PC-3

Ver [[pc3-entregable]]. Estas condicionan el informe, no solo el código:

7. **¿A qué ODS responde el proyecto?** PC-3 exige declararlo en la introducción (RC-07) y
   el brief no lo menciona.

   > **Inferencia:** el encaje natural es el **ODS 8 — Trabajo decente y crecimiento
   > económico**: un registro de asistencia verificable protege las horas trabajadas de las
   > podólogas y hace auditable la jornada. Es la respuesta a confirmar, no una decisión
   > tomada.

8. **¿El proyecto es en equipo de tres?** Los entregables se nombran `Apellidos1-2-3` y las
   conclusiones piden lecciones "por estudiante". El brief está en singular.
9. **¿Qué motor de base de datos?** RC-03 exige stored procedures. MySQL, PostgreSQL y SQL
   Server sirven; hay que elegir uno antes de modelar.
10. **¿Vista renderizada en servidor o SPA?** Decide dónde viven Proxy, Bridge, Observer y
    Command, que PC-3 ubica en la capa Vista. Ver [[cuatro-capas]].
11. **¿Dónde se ubican Decorator y Composite?** PC-3 los exige por familia pero no les
    asigna capa. Propuesta en [[mapa-patron-requisito]].
12. **¿Hay que importar los cronogramas viejos, o el sistema arranca en blanco?** De esto
    depende que [[patron-adapter]] entre o no: si no hay Excel histórico que cargar, no hay
    nada que adaptar. Ver [[adr-001-patrones-seleccionados]].

## Huecos de contenido del wiki

- Ninguna fuente del curso ha sido ingerida: `wiki/fuentes/` está vacío y `wiki/patrones/`
  también. Hasta que se ingiera el material, [[mapa-patron-requisito]] no se puede escribir
  con fundamento.
- No existe todavía página de arquitectura ni modelo de datos. Depende de cerrar las
  preguntas 1, 3 y 4.
