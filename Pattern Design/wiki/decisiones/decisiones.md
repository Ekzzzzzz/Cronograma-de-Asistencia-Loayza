---
titulo: Decisiones
tipo: decision
estado: estable
actualizado: 2026-09-06
tags: [adr, indice]
---

# Decisiones

Índice de las decisiones tomadas, en formato ADR ligero. Una decisión entra aquí cuando deja
de ser una pregunta abierta en [[huecos-abiertos]].

| ADR | Decisión | Estado | Fecha |
|---|---|---|---|
| [[adr-001-patrones-seleccionados]] | Stack Spring Boot, núcleo de 7 patrones, periferia de 5 y 3 descartes razonados | aceptada | 2026-09-06 |
| [[adr-002-arquitectura-produccion]] | MySQL 8, fotos en disco, VPS, lista cerrada de trabajadoras, retención de 3 meses y estrategia de rendimiento | aceptada | 2026-09-06 |
| [[adr-003-login-y-cronograma-simple]] | Login de administrador; la celda solo lleva horas; State descartado y Observer al núcleo | aceptada | 2026-09-06 |
| [[adr-004-despliegue-azure]] | Azure: una VM B1s con Spring Boot y MySQL, fotos a Blob Storage, HTTPS gratuito y presupuesto de 100 USD | aceptada | 2026-09-06 |
| [[adr-005-hosting-largo-plazo]] | Azure para el curso y Oracle Always Free para producción; ODS 8 confirmado; equipo de dos programando a la par | aceptada | 2026-09-06 |
| [[adr-006-dashboard-y-turnos-multiples]] | Dashboard con seis funciones; la celda lleva hasta 6 pares, uno por línea; cargo podóloga/cajera; correcciones como marcación nueva | aceptada | 2026-09-06 |
| [[adr-007-seguridad-endpoint-publico]] | Cuatro barreras contra abuso ordenadas por costo; tope duro de 12 marcaciones; sin CAPTCHA; jornada completa o media | aceptada | 2026-09-06 |
| [[adr-008-alcance-interfaz-y-cola-sin-senal]] | Sin reconocimiento facial ni GPS; una administradora; arranque en blanco; cola sin señal con doble hora y tope de 2 h; Thymeleaf con HTMX | aceptada | 2026-09-07 |
| [[adr-009-estructura-y-dos-patrones-que-vuelven]] | Bridge y Command vuelven por la migración de nube y la cola; estructura de `MVC` y `SQL`; paquetes con el nombre de las capas; plan comprimido a cuatro semanas | aceptada | 2026-09-07 |
| [[adr-010-sin-rechazos-oracle-y-nombre-libre]] | Ninguna marcación se rechaza por su secuencia; nombre libre con trabajadora provisional; cargo fuera de la plantilla; exportación diaria; ráfaga de 80; Oracle Always Free como plataforma | aceptada | 2026-09-20 |

## Pendientes de decidir

Las que siguen abiertas están en [[huecos-abiertos]]. Las más urgentes ahora que
[[modelo-datos]] ya existe:

1. **Si Adapter, Bridge y Command son de verdad lo que decimos.** Los tres entran «por
   confirmar» en [[adr-009-estructura-y-dos-patrones-que-vuelven]]. Se deciden **escribiendo las clases**, no antes.
   Si alguno no se sostiene, sale — y el descarte también puntúa, en el capítulo de
   antipatrones.
2. **Si el nivel gratuito de la cuenta de estudiante cubre la VM.** Cambia la duración del
   crédito por un factor de dos, y se comprueba en el portal.
3. **Confirmar con el docente que el grupo puede ser de dos.** PC-3 nombra los archivos
   `Apellidos1-2-3`. Es una pregunta de dos minutos y cuesta puntos de forma tonta.
4. **Probar Spring Boot y MySQL en ARM temprano**, si el destino es Oracle.
5. **Comprar el dominio.** Sin HTTPS válido el navegador no abre la cámara: es bloqueante,
   no cosmético.
