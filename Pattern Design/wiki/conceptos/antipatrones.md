---
tipo: concepto
titulo: Antipatrones — los 16 del curso, evaluados contra el proyecto
tags: [concepto, antipatron, entregable]
creado: 2026-09-05
actualizado: 2026-09-05
estado: activo
fuentes: ["[[fuente-s14-antipatrones]]"]
---

# Antipatrones — los 16 del curso, evaluados contra el proyecto

## Qué son

Soluciones a problemas de diseño que **parecen válidas al principio pero resultan ineficaces
o perjudiciales a largo plazo**. El término existe para señalar que algo es una mala
práctica, no una solución ([[fuente-s14-antipatrones]], diapositiva 10).

El propósito de estudiarlos es **identificar y evitar** prácticas que ya demostraron ser
problemáticas, aprendiendo de ejemplos de lo que no se debe hacer (diapositiva 11).

El curso distingue cuatro tipos (diapositiva 12): de **diseño de software** (la clase gorda
con toda la lógica de negocio), de **diseño orientado a objetos** (la *singletonitis*, abuso
del Singleton), de **programación** (el código espagueti) y **metodológicos** (programar
copiando y pegando en vez de generalizar).

> [!important] Esto es un entregable, no sólo teoría
> `S14-GUIA-TALLER.xlsx` pide **evaluar los 16 antipatrones sobre el propio proyecto**, con
> descripción, problema, solución alternativa, **evidencia** (captura del código) y un
> puntaje: completo (5), parcial (3), no aplicado (1). Se calcula un nivel alcanzado por
> categoría y uno global.
>
> Esta página es el borrador de ese entregable. Cada fila lleva **cómo se aborda en este
> proyecto** *(propuesta del agente)*, lista para rellenar con la evidencia cuando exista
> código.

## Antipatrones de desarrollo de software

### 1. Big Ball of Mud (Bola de Barro Gigante)
**Descripción:** sistema sin estructura clara, diseño caótico, todo entrelazado.
**Problema:** imposible de mantener, entender y extender.
**Solución del curso:** arquitectura bien definida, diseño modular, patrones de diseño.

**En este proyecto:** la tubería de seis etapas de [[adr-001-stack-y-arquitectura]] fija
responsabilidades separadas, y [[patron-facade]] da el punto único de entrada que impide que
el controlador REST acumule lógica. Cada decisión estructural queda en un ADR, así que la
arquitectura está escrita, no implícita.

### 2. Shotgun Surgery (Cirugía con Escopeta)
**Descripción:** un cambio obliga a modificaciones dispersas por todo el sistema.
**Problema:** falta de cohesión; los cambios son difíciles y propensos a errores.
**Solución del curso:** cohesión y acoplamiento — agrupar responsabilidades relacionadas.

**En este proyecto:** el caso de prueba es **añadir una verificación antifraude**. Con
[[patron-composite]] es una clase nueva y una línea de configuración; sin él, sería tocar el
servicio de marcación, la validación y el cálculo del veredicto. Lo mismo con
[[patron-factory]] para los exportadores: un `Map` inyectado por Spring en vez de un
`if/else` que hay que editar cada vez.

### 3. Vendor Lock-In (Dependencia del Proveedor)
**Descripción:** dependencia excesiva de una tecnología o proveedor concreto.
**Problema:** limita la flexibilidad y la capacidad de adaptación.
**Solución del curso:** estándares abiertos e interfaces bien definidas.

**En este proyecto — es el más pertinente de los ocho.** El despliegue del backend está
**deliberadamente sin decidir** *(decisión del usuario, 2026-09-05)*, así que el diseño no
puede atarse a ningún proveedor:

- el **almacenamiento de fotos** vive detrás de una interfaz propia; que debajo haya disco
  local, S3 o cualquier otro no lo sabe nadie más;
- persistencia con **JPA y PostgreSQL**, estándares portables;
- el **motor de reconocimiento facial** entra por [[patron-adapter]], que es precisamente la
  «interfaz bien definida» que pide la solución alternativa del curso.

El riesgo real y consciente: la PWA se aloja en **Vercel**, pero al ser un frontend estático
mudarlo es barato.

### 4. Not Invented Here (No Inventado Aquí)
**Descripción:** rechazar soluciones existentes para desarrollar las propias.
**Problema:** duplicación de esfuerzo y soluciones menos robustas.
**Solución del curso:** evaluar herramientas probadas antes de construir.

**En este proyecto:** dos decisiones ya tomadas lo evitan explícitamente. No se reimplementa
[[patron-singleton]] porque el contenedor de Spring ya lo da, ni [[patron-observer]] porque
Spring lo ofrece con eventos de aplicación. Igual con Apache POI para Excel y con un motor de
reconocimiento facial existente en vez de escribir uno.

> [!note] Tensión con la rúbrica del curso
> Este antipatrón y la exigencia de «aplicar patrones» empujan en direcciones opuestas.
> Reimplementar a mano un patrón que el marco ya provee, sólo para demostrarlo, **es** Not
> Invented Here. Por eso [[patrones-moc]] documenta los descartes con su justificación en vez
> de esconderlos.

### 5. Overengineering (Sobreingeniería)
**Descripción:** complejidad innecesaria que los requisitos actuales no piden.
**Problema:** más costo y tiempo sin valor añadido.
**Solución del curso:** **YAGNI** — *You Aren't Gonna Need It*.

**En este proyecto:** se descartan a propósito [[patron-abstract-factory]] (no hay familias
de productos que deban ser coherentes) y [[patron-bridge]] (la explosión de clases que lo
justifica todavía no existe). Ambos quedan anotados con el criterio exacto para
reconsiderarlos. El propio curso respalda esto: se empieza por Factory Method y se progresa
*cuando el diseñador se da cuenta de que se requiere más flexibilidad*
([[fuente-s07-factory-builder]], diapositiva 18).

### 6. Lava Flow (Flujo de Lava)
**Descripción:** código antiguo acumulado que no se eliminó ni refactorizó.
**Problema:** el código obsoleto dificulta entender y mantener el sistema.
**Solución del curso:** limpiezas y refactorizaciones periódicas.

**En este proyecto:** ya hay un precedente en la propia wiki. Cuando se descartó el enfoque
WhatsApp, el ADR viejo **se eliminó** y su decisión quedó registrada en [[log]] y en el
encabezado del nuevo, en vez de dejarlo como página muerta que confunde. La misma regla vale
para el código: lo que se abandona se borra, y el porqué vive en la bitácora.

### 7. Magic Numbers (Números Mágicos)
**Descripción:** valores literales en el código en lugar de constantes con nombre.
**Problema:** difíciles de entender y mantener; errores cuando el valor cambia.
**Solución del curso:** constantes con nombres descriptivos.

**En este proyecto — el de riesgo más concreto.** El diseño ya acumula números que **no
pueden acabar incrustados en el código**:

| Valor | Qué es | Dónde vive |
|---|---|---|
| 200 m | Radio de la geocerca | Configuración **por sede** ([[sedes]]) |
| 500 m | Precisión de GPS que se considera inservible | Configuración |
| 90 s | Vida del token de captura | Configuración |
| ¿? | Umbral de confianza del reconocimiento facial | Configuración, y hay que calibrarlo |
| 2 | Marcaciones esperadas por día | Regla de negocio con nombre |

[[sedes]] ya establece que el radio es configuración por sede, no una constante global.

### 8. Golden Hammer (Martillo de Oro)
**Descripción:** usar la misma herramienta para todo sin evaluar si es la adecuada.
**Problema:** soluciones inadecuadas y mal uso de recursos.
**Solución del curso:** elegir según los requisitos y el contexto de cada problema.

**En este proyecto:** el riesgo evidente es **aplicar patrones porque el curso los pide**.
La defensa está en [[patrones-moc]]: cada patrón lleva su evaluación, y 2 de 15 están
descartados con argumento. Un catálogo donde los 15 salieran «candidatos» sería la señal de
alarma.

## Antipatrones organizacionales

El curso los presenta como antipatrones de equipo y gestión. En un proyecto de un solo
desarrollador la mayoría no aplica al equipo — pero **varios sí describen el proceso actual
de Podología Loayza**, que es lo que el sistema viene a cambiar *(propuesta del agente)*.

| # | Antipatrón | Descripción del curso | Relación con este proyecto |
|---|---|---|---|
| 1 | **Silo Mentality** | Cada equipo trabaja aislado y no comparte información | **Aplica al proceso actual.** La programación de turnos vive en la memoria de una persona ([[adr-004-llenado-del-cronograma]]); si falta, nadie sabe quién debía venir. El sistema convierte ese conocimiento en datos consultables |
| 2 | **Top-Down Management** | Decisiones impuestas desde arriba sin consultar | El diseño se decide con el usuario y cada decisión queda en un ADR con su justificación, no impuesta por el desarrollador |
| 3 | **Micromanagement** | Controlar cada detalle sin dejar autonomía | **Riesgo del propio producto.** Un sistema de control de asistencia puede volverse una herramienta de vigilancia. Se acota: se registra entrada y salida, no ubicación continua ni actividad |
| 4 | **Blame Culture** | Culpar a personas en vez de buscar soluciones | **El más relevante de los ocho.** Por eso el sistema **nunca escribe `INASISTENCIA` por su cuenta** ([[adr-004-llenado-del-cronograma]]): acusar automáticamente a alguien de faltar, sin saberlo, es construir cultura de culpa dentro del software |
| 5 | **Management by Objectives** | Perseguir métricas sin mirar el contexto | **Aplica al proceso actual.** Los conteos `MAÑANA`/`TARDE` llevan 22 semanas congelados ([[formato-cronograma-actual]]): una métrica que nadie recalcula y todos citan |
| 6 | **Overloaded Teams** | Carga excesiva sin considerar la capacidad | La administradora transcribe a mano más de 100 horarios por semana y sede. El sistema le quita el 81,7 % de ese trabajo |
| 7 | **Lack of Vision** | Sin dirección clara ni objetivos a largo plazo | [[sintesis]] mantiene la tesis viva del proyecto y [[decisiones-moc]] el registro de por qué se llegó aquí |
| 8 | **Ineffective Communication** | La información no fluye entre niveles | **Aplica al proceso actual.** Las excepciones se comunican por WhatsApp y acaban como notas sueltas dentro de celdas de Excel. El sistema les da un canal con estructura |

## Cómo usar esta página para el entregable

Las columnas de `S14-GUIA-TALLER.xlsx` se rellenan así:

- **descripción, problema, solución alternativa** → el texto del curso, citado arriba.
- **evidencia** → captura del código cuando exista. Hoy, para varios, la evidencia es la
  propia wiki: [[patrones-moc]] justifica los descartes (Overengineering, Golden Hammer,
  Not Invented Here) y [[sedes]] la configuración por sede (Magic Numbers).
- **puntaje** → lo asigna el estudiante según lo aplicado.

**Ocho de los dieciséis ya tienen evidencia documental antes de escribir la primera línea de
código.** El resto la tendrá cuando haya `src/`.

## Enlaces

[[fuente-s14-antipatrones]] · [[patrones-moc]] · [[conceptos-moc]] · [[sintesis]]
