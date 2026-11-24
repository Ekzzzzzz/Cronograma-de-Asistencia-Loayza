---
tipo: moc
titulo: Decisiones — mapa
tags: [moc, adr]
creado: 2026-09-04
actualizado: 2026-09-04
estado: activo
---

# Decisiones — mapa

Registro de decisiones de arquitectura (ADR). **Ningún código entra en `src/` sin un ADR
aceptado** (`CLAUDE.md` §5.4).

## Decisiones

| ADR | Título | Patrón | Estado |
|---|---|---|---|
| [[adr-001-stack-y-arquitectura]] | Stack y arquitectura general | — (plataforma) | **aceptada** |
| [[adr-002-canal-de-marcacion]] | Canal de marcación: PWA propia, se descarta WhatsApp | — (dominio) | **aceptada** |
| [[adr-003-autenticacion-jwt]] | Autenticación y sesiones (Spring Security + JWT) | — (plataforma) | **aceptada** |
| [[adr-004-llenado-del-cronograma]] | Llenado del cronograma sin programación previa | — (dominio) | propuesta |
| [[adr-005-interfaz-de-marcacion]] | Interfaz de la app de marcación — pantallas, principios y casos límite | — (usabilidad) | propuesta, revisada por el 006 |
| [[adr-006-acceso-sin-sesion]] | Enlace global sin sesión | — (identidad) | aceptada en parte por el 007 |
| [[adr-007-flujo-consolidado-v1]] | **Flujo consolidado de la v1**: sede → nombre → foto → enviar; QR de dispositivo; panel con link secreto + login; un despliegue; patrones por capa | **13 patrones, por capa** | **aceptada — manda sobre 001–006** |

> [!important] Fuente de verdad del flujo: [[adr-007-flujo-consolidado-v1]]
> Los ADR 001–006 se conservan como historia de cómo se llegó aquí. Cada uno lleva al
> principio un aviso, y la tabla «Qué reemplaza este ADR» del 007 dice qué partes siguen
> en pie.

## Decisiones resueltas por el ADR-007

- ~~Cómo se componen las verificaciones antifraude~~ → Composite + Decorator
- ~~Cómo se identifica a las trabajadoras~~ → lista del padrón con buscador; sin rostro en la v1
- ~~Cómo se emparejan ingreso y salida~~ → «¿tiene jornada abierta?» ([[jornadas-multiples]])
- ~~Cómo se exporta al Excel~~ → Bridge (`Reporte` × `FormatoSalida`) + Factory
- ~~Cómo funciona la cola de revisión~~ → State + Command + Memento

## Pendientes para la v2

- Reconocimiento facial (volvería por [[patron-adapter]])
- Modo sin conexión con envío diferido
- Segundo factor para la administradora
- Celda `SIN MARCACIÓN` y la cola de dos opciones de [[adr-004-llenado-del-cronograma]]

## Plantilla

```markdown
---
tipo: decision
titulo: ADR-001 — ...
numero: 001
estado_adr: propuesta | aceptada | reemplazada
tags: [adr]
creado: YYYY-MM-DD
actualizado: YYYY-MM-DD
---

## Contexto
## Opciones
## Decisión
## Justificación
## Consecuencias
## Estado
```

Relacionado: [[patrones-moc]] · [[sintesis]] · [[index]]
