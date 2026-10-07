package pe.loayza.asistencia.bd;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import pe.loayza.asistencia.modelo.CatalogoSedes;
import pe.loayza.asistencia.modelo.Sede;
import pe.loayza.asistencia.modelo.TipoIncidencia;
import pe.loayza.asistencia.modelo.TipoMarcacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * <h2>Los procedimientos que no son el circuito de marcacion</h2>
 *
 * Una prueba por procedimiento, contra el semillero de
 * {@code SQL/03_datos_prueba.sql}. Cada una lee <b>todas</b> las columnas que
 * mapea al menos una vez: con {@code DataClassRowMapper}, una columna
 * renombrada en el SQL deja su campo en nulo o en cero <b>sin dar error</b>, y
 * solo una prueba que mire el valor lo descubre.
 *
 * <h3>Aqui SI hay transaccion de prueba</h3>
 * A diferencia de {@link CircuitoDeMarcacionTest}, estas pruebas no estudian la
 * transaccion: estudian lo que se lee y se escribe. Asi que se dejan dentro de
 * la transaccion de {@code @JdbcTest}, que <b>se deshace al terminar</b>: las
 * incidencias, provisionales y accesos que se crean aqui no quedan en la base.
 *
 * <p>El semillero se ancla a {@code CURDATE()}: su primer lunes es el de hace
 * dos semanas, y estas pruebas lo calculan igual.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MarcacionesBD.class, CronogramaBD.class, IncidenciasBD.class,
        TrabajadorasBD.class, AdministradoresBD.class, FotosBD.class, SedesBD.class})
@DisplayName("Procedimientos almacenados: uno por uno contra el semillero")
class ProcedimientosBDTest {

    /** El primer lunes del semillero. */
    static final LocalDate LUNES = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(2);
    /** El sabado de la segunda semana, donde viven los casos raros. */
    static final LocalDate SABADO = LUNES.plusDays(12);

    @Autowired MarcacionesBD marcaciones;
    @Autowired CronogramaBD cronograma;
    @Autowired IncidenciasBD incidencias;
    @Autowired TrabajadorasBD trabajadoras;
    @Autowired AdministradoresBD administradores;
    @Autowired FotosBD fotos;
    @Autowired SedesBD sedesBD;

    @Nested
    @DisplayName("Marcaciones")
    class Marcaciones {

        @Test
        @DisplayName("sp_contexto_marcacion: cuantas lleva y cual fue la ultima")
        void contexto() {
            ContextoMarcacion dia = marcaciones.contexto(1, 1, LUNES.atTime(12, 0));
            assertThat(dia.marcacionesHoy()).isEqualTo(4);
            assertThat(dia.ultimoTipo()).isEqualTo(TipoMarcacion.SALIDA);

            ContextoMarcacion nada = marcaciones.contexto(1, 1, LocalDateTime.of(2099, 1, 1, 9, 0));
            assertThat(nada.marcacionesHoy()).isZero();
            assertThat(nada.ultimoTipo()).isNull();
        }

        @Test
        @DisplayName("sp_historial_trabajadora: cruza sedes, y con las mismas columnas que el dia")
        void historial() {
            List<FilaMarcacion> filas = marcaciones.historial(1, LUNES, LUNES.plusDays(13));

            // 12 dias de 2 turnos = 48, mas el apoyo en Miraflores del viernes.
            assertThat(filas).hasSize(50);
            assertThat(filas).extracting(FilaMarcacion::sede).containsOnly("Los Olivos", "Miraflores");
            FilaMarcacion apoyo = filas.stream().filter(f -> f.sede().equals("Miraflores")).findFirst().orElseThrow();
            assertThat(apoyo.marcacion().notas()).isEqualTo("Apoyo en Miraflores");
            assertThat(apoyo.marcacion().sedeId()).isEqualTo(7);
            assertThat(apoyo.trabajadora()).isNotBlank();
            assertThat(apoyo.fotoMiniRuta()).isEqualTo("mini/1-apoyo-e.jpg");
            assertThat(apoyo.sinFoto()).isFalse();
            assertThat(apoyo.incidencias()).isZero();
        }

        @Test
        @DisplayName("sp_marcaciones_del_dia: la correccion a mano se ve como tal")
        void correccionDeLaAdministradora() {
            FilaMarcacion corregida = marcaciones.delDia(null, SABADO.minusDays(2)).stream()
                    .filter(f -> f.corregidaPor() != null)
                    .findFirst().orElseThrow();
            assertThat(corregida.marcacion().trabajadoraId()).isEqualTo(7);
            assertThat(corregida.marcacion().origen().exigeMotivo()).isTrue();
            assertThat(corregida.motivo()).startsWith("Olvido de salida");
            assertThat(corregida.sinFoto()).isTrue();
        }
    }

    @Nested
    @DisplayName("Cronograma y exportacion")
    class Cronograma {

        @Test
        @DisplayName("sp_cronograma_semanal: la rejilla completa, 14 personas por 7 dias")
        void semanal() {
            List<FilaCronograma> rejilla = cronograma.semanal(1, LUNES);

            assertThat(rejilla).hasSize(14 * 7);
            Map<Integer, Long> diasPorPersona = rejilla.stream()
                    .collect(Collectors.groupingBy(FilaCronograma::trabajadoraId, Collectors.counting()));
            assertThat(diasPorPersona.values()).containsOnly(7L);
            // El domingo viene, vacio: el Excel sale cuadrado sin rellenar nada.
            assertThat(rejilla).filteredOn(f -> f.fecha().getDayOfWeek() == DayOfWeek.SUNDAY)
                    .allMatch(FilaCronograma::vacia);
            FilaCronograma lunes = rejilla.stream()
                    .filter(f -> f.trabajadoraId() == 1 && f.fecha().equals(LUNES)).findFirst().orElseThrow();
            assertThat(lunes.diaSemana()).as("DAYOFWEEK de MySQL: lunes = 2").isEqualTo(2);
            assertThat(lunes.turnos()).isEqualTo(2);
            assertThat(lunes.minutos()).isBetween(460, 500);
            assertThat(lunes.primeraEntrada()).isNotNull();
            assertThat(lunes.ultimaSalida()).isNotNull();
            assertThat(lunes.etiqueta()).contains("\n");
            assertThat(lunes.nombre()).isNotBlank();
        }

        @Test
        @DisplayName("sp_cronograma_semanal exige un lunes: un miercoles daria una semana falsa")
        void semanalExigeLunes() {
            assertThatThrownBy(() -> cronograma.semanal(1, LUNES.plusDays(2)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("sp_cronograma_diario devuelve exactamente lo mismo que ese dia de la semanal")
        void diarioIgualQueSemanal() {
            List<FilaCronograma> rejilla = cronograma.semanal(1, LUNES);
            for (int d = 0; d < 7; d++) {
                LocalDate dia = LUNES.plusDays(d);
                assertThat(cronograma.diario(1, dia))
                        .as("dia %s", dia)
                        .isEqualTo(rejilla.stream().filter(f -> f.fecha().equals(dia)).toList());
            }
        }

        @Test
        @DisplayName("sp_totales_horas suma lo mismo que las celdas de la semana")
        void totalesCuadranConLaRejilla() {
            Map<Integer, Integer> porRejilla = cronograma.semanal(1, LUNES).stream()
                    .collect(Collectors.groupingBy(FilaCronograma::trabajadoraId,
                            Collectors.summingInt(FilaCronograma::minutos)));

            List<TotalHoras> totales = cronograma.totales(1, LUNES, LUNES.plusDays(6));

            assertThat(totales).isNotEmpty();
            for (TotalHoras t : totales) {
                assertThat(t.minutos()).as(t.nombre()).isEqualTo(porRejilla.get(t.trabajadoraId()));
                assertThat(t.sedeId()).isEqualTo(1);
                assertThat(t.sede()).isEqualTo("Los Olivos");
                assertThat(t.diasTrabajados()).isEqualTo(6);
                assertThat(t.horas().doubleValue()).isCloseTo(t.minutos() / 60.0,
                        org.assertj.core.data.Offset.offset(0.01));
            }
        }

        @Test
        @DisplayName("sp_dia_sede: la salida sin entrada en otra sede sale en la pestana de esa sede")
        void salidaSinEntradaEnOtraSede() {
            FilaDiaSede rara = cronograma.diaSede(5, SABADO).stream()
                    .filter(f -> f.trabajadoraId() == 17).findFirst().orElseThrow();
            assertThat(rara.etiqueta()).isEqualTo("- 4:40PM");
            assertThat(rara.turnoAbierto()).isFalse();
            assertThat(rara.incidenciasPendientes()).isEqualTo(1);
            assertThat(rara.provisional()).isFalse();

            FilaDiaSede encolada = cronograma.diaSede(1, SABADO).stream()
                    .filter(f -> f.trabajadoraId() == 23).findFirst().orElseThrow();
            assertThat(encolada.marcacionesConHoraDelTelefono()).isEqualTo(1);
            assertThat(encolada.desfaseMaximoMinutos()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Incidencias")
    class Incidencias {

        @Test
        @DisplayName("sp_incidencias: las cinco del semillero, cada una con su marcacion")
        void lasDelSemillero() {
            List<FilaIncidencia> todas = incidencias.listar(LUNES.atStartOfDay(), LocalDateTime.now(), true);

            assertThat(todas).hasSize(5);
            assertThat(todas).extracting(FilaIncidencia::tipo).containsExactlyInAnyOrder(
                    TipoIncidencia.HORA_DEL_TELEFONO, TipoIncidencia.ENVIO_DUPLICADO,
                    TipoIncidencia.FOTO_NO_GUARDADA, TipoIncidencia.FOTO_SOSPECHOSA,
                    TipoIncidencia.COLA_MUY_ANTIGUA);
            assertThat(todas).allMatch(i -> i.marcacionId() != null && !i.revisada());

            FilaIncidencia sospechosa = todas.stream()
                    .filter(i -> i.tipo() == TipoIncidencia.FOTO_SOSPECHOSA).findFirst().orElseThrow();
            assertThat(sospechosa.ip()).isEqualTo("2001:1388:5::9");
            assertThat(sospechosa.marcacionTipo()).isEqualTo(TipoMarcacion.SALIDA);
            assertThat(sospechosa.marcacionMomento()).isEqualTo(SABADO.atTime(16, 40));
            assertThat(sospechosa.trabajadoraId()).isEqualTo(17);
            assertThat(sospechosa.trabajadora()).isNotBlank();
            assertThat(sospechosa.sedeId()).isEqualTo(5);
            assertThat(sospechosa.sede()).isEqualTo("San Miguel");
            assertThat(sospechosa.detalle()).contains("9000x9000");
        }

        @Test
        @DisplayName("Registrar, ver y marcar como revisada; un detalle largo se corta")
        void cicloCompleto() {
            LocalDateTime ahora = LocalDateTime.of(2099, 1, 5, 9, 0);
            incidencias.registrar(IncidenciaNueva.sobre(ahora, null, "10.0.0.1", null, null,
                    TipoIncidencia.ENVIO_DUPLICADO, "x".repeat(300)));

            List<FilaIncidencia> nueva = incidencias.listar(ahora, ahora, true);
            assertThat(nueva).hasSize(1);
            assertThat(nueva.getFirst().ip()).isEqualTo("10.0.0.1");
            assertThat(nueva.getFirst().detalle()).hasSize(Recorte.DETALLE).endsWith("…");
            assertThat(nueva.getFirst().marcacionTipo()).isNull();

            assertThat(nueva.getFirst().claveEnvio()).isNull();
            assertThat(nueva.getFirst().fotoRuta()).isNull();

            incidencias.marcarRevisada(nueva.getFirst().id());
            assertThat(incidencias.listar(ahora, ahora, true)).isEmpty();
            assertThat(incidencias.listar(ahora, ahora, false)).singleElement()
                    .extracting(FilaIncidencia::revisada).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("Foto sin marcacion: se guarda y se avisa (decision del usuario, 2026-10-06)")
    class FotoSinMarcacion {

        @Test
        @DisplayName("La foto de una clave desconocida deja un aviso con su ruta, en la fila de esa persona")
        void dejaElAviso() {
            String clave = java.util.UUID.randomUUID().toString();
            String ruta = "fotos/" + clave + ".jpg";
            LocalDateTime llego = SABADO.atTime(10, 15);
            int antes = cronograma.diaSede(1, SABADO).stream()
                    .filter(f -> f.trabajadoraId() == 1).findFirst().orElseThrow().incidenciasPendientes();

            // 1. La base dice que esa clave no tiene marcacion.
            assertThat(fotos.registrarFoto(clave, ruta, null)).isEmpty();
            // 2. La capa control (aqui, la prueba) deja el aviso con la foto.
            incidencias.registrar(IncidenciaNueva.fotoSinMarcacion(llego, clave, ruta, "190.40.1.2", 1, 1));

            FilaIncidencia aviso = incidencias.listar(llego, llego, true).getFirst();
            assertThat(aviso.tipo()).isEqualTo(TipoIncidencia.FOTO_SIN_MARCACION);
            assertThat(aviso.fotoRuta()).isEqualTo(ruta);
            assertThat(aviso.claveEnvio()).isEqualTo(clave);
            assertThat(aviso.marcacionId()).isNull();
            assertThat(aviso.revisada()).isFalse();
            assertThat(aviso.sede()).isEqualTo("Los Olivos");

            // 3. Y sale junto a la persona en el dashboard, no en una lista aparte.
            assertThat(cronograma.diaSede(1, SABADO).stream()
                    .filter(f -> f.trabajadoraId() == 1).findFirst().orElseThrow().incidenciasPendientes())
                    .isEqualTo(antes + 1);
        }
    }

    @Nested
    @DisplayName("Provisionales")
    class Provisionales {

        @Test
        @DisplayName("sp_provisionales: la del semillero, con sus dos marcaciones")
        void laDelSemillero() {
            List<FilaProvisional> filas = trabajadoras.provisionales();
            assertThat(filas).hasSize(1);
            FilaProvisional p = filas.getFirst();
            assertThat(p.marcaciones()).isEqualTo(2);
            assertThat(p.nombre()).isNotBlank();
            assertThat(p.creadaEn()).isNotNull();
            assertThat(p.sedeHabitual()).isNotBlank();
            assertThat(p.primeraMarcacion()).isBefore(p.ultimaMarcacion());
        }

        @Test
        @DisplayName("sp_registrar_provisional: el mismo nombre dos veces es la misma persona")
        void reutilizaElMismoNombre() {
            int primera = trabajadoras.registrarProvisional("  ZZ Prueba Provisional ", 1);
            int segunda = trabajadoras.registrarProvisional("ZZ Prueba Provisional", 1);
            assertThat(segunda).isEqualTo(primera);
            assertThat(trabajadoras.provisionales()).hasSize(2);
        }

        @Test
        @DisplayName("Un nombre de 100 letras se corta a 80: el alta no falla")
        void nombreLargoSeCorta() {
            trabajadoras.registrarProvisional("Z".repeat(100), 1);
            assertThat(trabajadoras.provisionales()).extracting(FilaProvisional::nombre)
                    .anyMatch(n -> n.length() == Recorte.NOMBRE && n.endsWith("…"));
        }
    }

    @Nested
    @DisplayName("Sedes y padron (hueco 32)")
    class SedesYPadron {

        @Test
        @DisplayName("sp_sedes: las siete, por nombre, y cargan el Singleton del modelo")
        void sedes() {
            List<Sede> sedes = sedesBD.todas();

            assertThat(sedes).extracting(Sede::nombre).containsExactly(
                    "La Molina", "Lince", "Los Olivos", "Miraflores", "San Borja", "San Miguel", "Surco");
            assertThat(sedes).allMatch(Sede::activa);
            assertThat(sedes).allMatch(s -> s.latitud() != null && s.longitud() != null && s.plusCode() != null);

            // La primera vez que el Singleton del modelo recibe datos reales.
            CatalogoSedes catalogo = CatalogoSedes.instancia();
            try {
                catalogo.cargar(sedes);
                assertThat(catalogo.porId(7).nombre()).isEqualTo("Miraflores");
                assertThat(catalogo.activas()).hasSize(7);
            } finally {
                catalogo.limpiar();
            }
        }

        @Test
        @DisplayName("sp_padron por sede: las 14 de la sede, mas su provisional si la tiene")
        void padronPorSede() {
            assertThat(trabajadoras.padron(1)).hasSize(14).noneMatch(FilaPadron::provisional)
                    .allMatch(p -> p.sedeHabitual() == 1 && !p.nombre().isBlank());

            List<FilaPadron> lince = trabajadoras.padron(4);
            assertThat(lince).hasSize(15);
            assertThat(lince).filteredOn(FilaPadron::provisional).extracting(FilaPadron::nombre)
                    .containsExactly("Sandra Ccahuana");
        }

        @Test
        @DisplayName("sp_padron sin sede: todas, para quien cubre otra sede y para marcar sin senal")
        void padronCompleto() {
            assertThat(trabajadoras.padron(null)).hasSize(99);
        }
    }

    @Nested
    @DisplayName("Administradora y fotos")
    class AdministradoraYFotos {

        @Test
        @DisplayName("sp_autenticar_admin devuelve el hash, y toString() no lo ensena")
        void credencial() {
            CredencialAdmin admin = administradores.buscar("admin").orElseThrow();
            assertThat(admin.id()).isEqualTo(1);
            assertThat(admin.usuario()).isEqualTo("admin");
            assertThat(admin.clave()).startsWith("$2").hasSize(60);
            assertThat(admin.nombre()).isNotBlank();
            assertThat(admin.activo()).isTrue();
            assertThat(admin.toString()).doesNotContain(admin.clave()).contains("clave=***");

            assertThat(administradores.buscar("nadie")).isEmpty();
        }

        @Test
        @DisplayName("sp_marcar_acceso_admin deja la fecha del ultimo acceso")
        void ultimoAcceso() {
            administradores.marcarAcceso(1);
            assertThat(administradores.buscar("admin").orElseThrow().ultimoAcceso()).isNotNull();
        }

        @Test
        @DisplayName("sp_purgar_fotos: con 90 dias no toca el semillero; con 0, todo lo anterior a hoy")
        void purga() {
            assertThat(fotos.marcarPurgadas(90)).isZero();
            assertThat(fotos.marcarPurgadas(0)).isGreaterThan(3000);
        }
    }
}
