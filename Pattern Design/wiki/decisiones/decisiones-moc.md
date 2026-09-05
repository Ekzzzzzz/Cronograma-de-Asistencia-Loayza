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
| [[adr-006-acceso-sin-sesion]] | Enlace global sin sesión; el rostro pasa a ser la verificación principal | — (identidad) | propuesta |

## Decisiones que el proyecto va a necesitar

Una por eslabón de la tubería descrita en [[sintesis]] *(propuesta del agente)*. Todas
bloqueadas hasta ingerir S06–S13: sin catálogo de patrones no hay opciones que comparar.

- Cómo se componen las verificaciones antifraude ([[antifraude]]) — la decisión más rica
- Cómo se identifica a las trabajadoras (motor de reconocimiento facial)
- Cómo se emparejan ingreso y salida en una jornada
- Cómo se expresan y aplican las reglas de negocio
- Cómo se modela la celda del cronograma (horario / estado / pendiente)
- Cómo se exporta al formato Excel de [[formato-cronograma-actual]]
- Cómo funciona la cola de revisión humana

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
