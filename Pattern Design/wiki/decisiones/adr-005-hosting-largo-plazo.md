---
titulo: ADR-005 — Hosting a largo plazo, ODS y tamaño del equipo
tipo: decision
estado: estable
fuentes:
  - decisiones del usuario (2026-09-06)
  - https://azure.microsoft.com/en-us/pricing/offers/ms-azr-0170p
  - https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
  - https://www.infoq.com/news/2026/07/oracle-cloud-free-tier-limits/
actualizado: 2026-09-06
tags: [adr, decision, hosting, costos, ods, equipo]
---

# ADR-005 — Hosting a largo plazo, ODS y tamaño del equipo

**Fecha:** 2026-09-06 · **Estado:** aceptada

## Contexto

Tres cosas que [[adr-004-despliegue-azure]] dejó abiertas y que el usuario cerró o mandó
investigar el 2026-09-06:

1. Si los 100 USD de Azure se renuevan cada año con el correo de la Universidad Tecnológica
   del Perú.
2. Si existen alternativas más baratas.
3. El ODS al que responde el proyecto y el tamaño del equipo.

## Lo que se averiguó sobre el crédito

**Azure for Students sí se renueva.** No hay límite de veces: mientras se siga siendo
estudiante verificado de una institución acreditada, se puede renovar cada 12 meses y recibir
**100 USD nuevos**. La renovación **no es automática** — Microsoft avisa unos 30 días antes del
vencimiento y hay que volver a verificar el estatus académico.

**Pero eso no resuelve el problema, y la aritmética explica por qué.** El crédito se agota por
lo que ocurra primero: 12 meses **o** 100 dólares. A ~13 USD al mes, los 100 dólares se acaban
en el mes **7,7**, y la renovación no llega hasta el mes 12.

> **El resultado son más de cuatro meses de sistema apagado en mitad del año.** Para que las
> dos fechas coincidan, el gasto tiene que quedar **por debajo de 8,33 USD al mes**, y con la
> VM, la IP pública y el disco de Azure eso no se alcanza sin recortar algo necesario.

Ese, y no el precio, es el motivo por el que Azure no sirve como destino final.

## Comparación de alternativas

| | Azure B1s | **Oracle Always Free** | Hetzner CX22 | Contabo |
|---|---|---|---|---|
| CPU / RAM | 1 vCPU · 1 GB | 2 OCPU ARM · **12 GB** | 2 vCPU · 4 GB | 4 vCPU · 8 GB |
| Bloque | 32 GB (pago) | **200 GB** | 40 GB | 100+ GB |
| Objetos | Blob, ~0,22 USD | **20 GB** | — | — |
| Salida | 100 GB/mes | **10 TB/mes** | 20 TB/mes | 32 TB/mes |
| Costo | ~13 USD/mes | **0** | ~5,50 USD/mes | ~5 USD/mes |
| Vencimiento | 12 meses, renovable | **ninguno** | ninguno | ninguno |

Cifras de septiembre de 2026; conviene reverificarlas antes de contratar.

## Decisiones

### 1. Azure se mantiene para el curso; Oracle es el destino de producción

**Para PC-3, el piloto y la demo: Azure**, como fija [[adr-004-despliegue-azure]]. Está
decidido, documentado y 100 USD cubren de sobra los meses hasta la entrega. Cambiar de
proveedor a mitad del trabajo académico no compra nada.

**Para el despliegue real de la empresa: Oracle Cloud Always Free.** Elimina el problema de
raíz — no hay crédito que se agote, ni renovación que recordar, ni ventana de meses con el
sistema caído. Y con 12 GB de RAM desaparece todo el afinado incómodo que ADR-004 documentó:
el heap de 384 MB, el buffer pool de 192 MB y los 2 GB de swap dejan de ser necesarios.

Sus 20 GB de almacenamiento de objetos cubren justo los ~12 GB de fotos en régimen estable, así
que la implementación de `AlmacenFotos` y el papel de [[patron-adapter]] se conservan igual.

> **Tres riesgos anotados, no ocultados.** Oracle **redujo el nivel gratuito a la mitad el 15
> de junio de 2026** —de 4 OCPU y 24 GB a 2 y 12— **sin anuncio público**, solo actualizando la
> documentación: puede repetirlo. Provisionar instancias ARM falla con frecuencia por
> *out of capacity* en muchas regiones. Y es ARM: Java y MySQL funcionan bien en aarch64, pero
> hay que saberlo antes de empezar.

**Si Oracle no da capacidad, la salida es Hetzner** (~5,50 USD al mes, con centros en Ashburn y
Hillsboro, mejor latencia hacia Lima que Europa). Es una cifra que una cadena de siete sedes
paga sin notarlo.

### 2. Las cuentas de estudiante no se encadenan para producción

El usuario trabaja con una compañera que también tiene crédito. Sumar las dos suscripciones
funciona técnicamente, pero **para el registro de asistencia de una empresa real es frágil**:
la segunda cuenta la controla otra persona, y si pierde el estatus de estudiante el sistema cae
con los datos dentro.

**Se destina a otra cosa:** la cuenta de la compañera monta un **entorno de pruebas separado**,
donde se puede romper todo sin tocar marcaciones reales. Eso además produce el material de
datos de prueba que [[pc3-entregable]] exige para el video (RC-08).

### 3. ODS 8 — Trabajo decente y crecimiento económico

**Confirmado por el usuario.** Cierra RC-07 y la pregunta 7 de [[huecos-abiertos]], que era el
último hueco que bloqueaba la introducción del informe.

El argumento: hoy el control de horas se lleva a mano en un Excel que alguien llena observando
([[formato-cronograma-excel]]). Un registro con evidencia fotográfica y hora puesta por el
servidor **hace auditable la jornada y protege las horas efectivamente trabajadas** de las
podólogas. La meta 8.8 del ODS 8 —proteger los derechos laborales y promover entornos de
trabajo seguros— es el encaje directo.

### 4. El equipo es de dos personas

**Confirmado por el usuario**, y con reparto de trabajo **a la par**: programando juntas en vez
de dividir por capas o por módulos. Es más lento, pero es lo que premia la **entrevista
individual** de PC-3, donde cada estudiante responde por todo el sistema.

> **Riesgo abierto, y conviene resolverlo pronto.** [[pc3-entregable]] nombra los archivos como
> `Apellidos1-2-3` y pide "lecciones aprendidas por cada estudiante", lo que apunta a grupos de
> **tres**. El usuario confirmó que son dos, pero **no que el docente lo haya autorizado**. Es
> el tipo de detalle que cuesta puntos de forma tonta y se cierra con una pregunta en clase.
> Ver [[huecos-abiertos]].

## Consecuencias

- [[adr-004-despliegue-azure]] sigue vigente para el curso; su horizonte pasa a ser explícito:
  **es el despliegue del entregable, no el definitivo**.
- Aparece una tarea nueva antes de terminar: **migrar a Oracle** y verificar que el sistema
  corre en ARM.
- El objetivo del informe queda cerrado: **ODS 8**, con la meta 8.8 como referencia.
- La página de [[patron-adapter]] pasa a ser necesaria: su justificación ya no depende del
  alcance, y sobrevive al cambio de proveedor.
- Hay una tarea administrativa con fecha: **confirmar con el docente el grupo de dos**.

## Enlaces

[[adr-004-despliegue-azure]] · [[adr-002-arquitectura-produccion]] · [[pc3-entregable]] ·
[[huecos-abiertos]] · [[requisitos]]
