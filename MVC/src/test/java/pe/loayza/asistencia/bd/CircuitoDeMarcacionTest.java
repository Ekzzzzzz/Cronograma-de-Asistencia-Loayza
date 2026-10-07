package pe.loayza.asistencia.bd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.loayza.asistencia.modelo.FuenteHora;
import pe.loayza.asistencia.modelo.Jornada;
import pe.loayza.asistencia.modelo.Marcacion;
import pe.loayza.asistencia.modelo.OrigenMarcacion;
import pe.loayza.asistencia.modelo.TipoMarcacion;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>El circuito completo contra MySQL de verdad</h2>
 *
 * {@code sp_registrar_marcacion} → {@code Jornada.de} → {@code sp_actualizar_jornada}.
 *
 * <h3>Por que aqui NO hay transaccion de prueba</h3>
 * {@code @JdbcTest} mete cada prueba en una transaccion que se deshace al
 * terminar. Es comodo, pero dentro de ella el {@code @Transactional} de
 * {@link MarcacionesBD#registrar} no abre la suya: <b>se une a la de la
 * prueba</b>, y lo que se probaria seria otra cosa. Con
 * {@code NOT_SUPPORTED} cada llamada confirma de verdad, como en produccion.
 *
 * <p>El precio es limpiar a mano. Todo se escribe en 2099, una fecha que ningun
 * dato real va a pisar, y se borra antes y despues de cada prueba.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({MarcacionesBD.class, CronogramaBD.class, FotosBD.class})
@DisplayName("Circuito de marcacion: insertar, emparejar y reescribir la celda")
class CircuitoDeMarcacionTest {

    private static final LocalDate DIA = LocalDate.of(2099, 1, 5);
    private static final int SEDE = 1;
    /** Su sede habitual es la 1, asi que sale en la pestana de esa sede. */
    private static final int PERSONA = 1;

    @Autowired
    MarcacionesBD marcaciones;

    @Autowired
    CronogramaBD cronograma;

    @Autowired
    FotosBD fotos;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void limpiar() {
        jdbc.update("DELETE i FROM incidencia i JOIN marcacion m ON m.id = i.marcacion_id"
                + " WHERE m.fecha >= '2099-01-01'");
        jdbc.update("DELETE f FROM foto_marcacion f JOIN marcacion m ON m.id = f.marcacion_id"
                + " WHERE m.fecha >= '2099-01-01'");
        jdbc.update("DELETE FROM marcacion WHERE fecha >= '2099-01-01'");
        jdbc.update("DELETE FROM jornada_diaria WHERE fecha >= '2099-01-01'");
    }

    // ------------------------------------------------------------- ayudantes

    private static Marcacion marca(TipoMarcacion tipo, LocalDateTime cuando) {
        return Marcacion.de(PERSONA, SEDE, tipo, cuando);
    }

    private static LocalDateTime a(int hora, int minuto) {
        return DIA.atTime(hora, minuto);
    }

    private MarcacionRegistrada registrar(TipoMarcacion tipo, LocalDateTime cuando) {
        return marcaciones.registrar(MarcacionNueva.deTrabajadora(marca(tipo, cuando), UUID.randomUUID().toString()));
    }

    private FilaDiaSede celda(LocalDate dia) {
        return cronograma.diaSede(SEDE, dia).stream()
                .filter(c -> c.trabajadoraId() == PERSONA)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hay celda para la persona " + PERSONA));
    }

    private List<Marcacion> guardadas(LocalDate dia) {
        return marcaciones.delDia(SEDE, dia).stream()
                .map(FilaMarcacion::marcacion)
                .filter(m -> m.trabajadoraId() == PERSONA)
                .toList();
    }

    // --------------------------------------------------------------- pruebas

    @Test
    @DisplayName("9-1 y 2-6 son ocho horas, no nueve, y la celda lo dice")
    void ochoHorasNoNueve() {
        registrar(TipoMarcacion.ENTRADA, a(9, 0));
        registrar(TipoMarcacion.SALIDA, a(13, 0));
        registrar(TipoMarcacion.ENTRADA, a(14, 0));
        MarcacionRegistrada ultima = registrar(TipoMarcacion.SALIDA, a(18, 0));

        assertThat(ultima.celdaActualizada()).isTrue();
        assertThat(ultima.marcacionesHoy()).isEqualTo(4);
        assertThat(ultima.contador().mensaje()).isEqualTo("Marcacion 4 de 4 de hoy");

        FilaDiaSede c = celda(DIA);
        assertThat(c.etiqueta()).isEqualTo("9:00AM - 1:00PM\n2:00PM - 6:00PM");
        assertThat(c.minutos()).isEqualTo(480);
        assertThat(c.turnos()).isEqualTo(2);
        assertThat(c.primeraEntrada()).isEqualTo(LocalTime.of(9, 0));
        assertThat(c.ultimaSalida()).isEqualTo(LocalTime.of(18, 0));
        assertThat(c.turnoAbierto()).isFalse();
    }

    @Test
    @DisplayName("La marcacion existe antes que su foto, y la foto llega despues (regla 2, ADR-014)")
    void laFotoLlegaDespues() {
        String clave = UUID.randomUUID().toString();
        MarcacionRegistrada r = marcaciones.registrar(
                MarcacionNueva.deTrabajadora(marca(TipoMarcacion.ENTRADA, a(9, 0)), clave));

        // Guardada, en su celda, y todavia sin foto: no la espero.
        assertThat(r.id()).isPositive();
        assertThat(marcaciones.delDia(SEDE, DIA).getFirst().sinFoto()).isTrue();
        assertThat(celda(DIA).etiqueta()).isEqualTo("9:00AM -");

        // Llega la foto, identificada por la clave y no por el id.
        assertThat(fotos.registrarFoto(clave, "fotos/" + clave + ".jpg", "mini/" + clave + ".jpg"))
                .contains(r.id());
        FilaMarcacion conFoto = marcaciones.delDia(SEDE, DIA).getFirst();
        assertThat(conFoto.sinFoto()).isFalse();
        assertThat(conFoto.fotoMiniRuta()).isEqualTo("mini/" + clave + ".jpg");

        // La misma foto reenviada no da error ni se duplica.
        assertThat(fotos.registrarFoto(clave, "fotos/" + clave + ".jpg", "mini/" + clave + ".jpg"))
                .contains(r.id());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM foto_marcacion WHERE marcacion_id = ?",
                Integer.class, r.id())).isEqualTo(1);

        // Una foto de una clave que no existe no rompe nada: control decide.
        assertThat(fotos.registrarFoto(UUID.randomUUID().toString(), "fotos/x.jpg", null)).isEmpty();
    }

    @Test
    @DisplayName("Un reenvio (misma clave) no crea otra marcacion: devuelve la que ya estaba")
    void reenvioNoDuplica() {
        String clave = UUID.randomUUID().toString();
        MarcacionRegistrada primera = marcaciones.registrar(
                MarcacionNueva.deTrabajadora(marca(TipoMarcacion.ENTRADA, a(9, 0)), clave));
        // El servidor recalcula la hora de cada intento: el reenvio llega
        // con unos segundos de diferencia y aun asi es la misma marcacion.
        MarcacionRegistrada reenvio = marcaciones.registrar(
                MarcacionNueva.deTrabajadora(marca(TipoMarcacion.ENTRADA, DIA.atTime(9, 0, 3)), clave));

        assertThat(primera.repetida()).isFalse();
        assertThat(reenvio.repetida()).isTrue();
        assertThat(reenvio.id()).isEqualTo(primera.id());
        assertThat(reenvio.marcacionesHoy()).isEqualTo(1);
        assertThat(guardadas(DIA)).singleElement()
                .extracting(Marcacion::momento).isEqualTo(a(9, 0));
        assertThat(celda(DIA).etiqueta()).isEqualTo("9:00AM -");
    }

    @Test
    @DisplayName("Ocho reenvios SIMULTANEOS de la misma clave: una sola marcacion")
    void reenviosSimultaneos() throws Exception {
        String clave = UUID.randomUUID().toString();
        int hilos = 8;
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<MarcacionRegistrada>> envios = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(hilos)) {
            for (int i = 0; i < hilos; i++) {
                envios.add(pool.submit(() -> {
                    salida.await();
                    return marcaciones.registrar(
                            MarcacionNueva.deTrabajadora(marca(TipoMarcacion.ENTRADA, a(9, 0)), clave));
                }));
            }
            salida.countDown();
        }
        List<MarcacionRegistrada> resultados = new ArrayList<>();
        for (Future<MarcacionRegistrada> f : envios) {
            resultados.add(f.get());
        }

        assertThat(guardadas(DIA)).hasSize(1);
        assertThat(resultados).extracting(MarcacionRegistrada::id).containsOnly(resultados.getFirst().id());
        assertThat(resultados).filteredOn(MarcacionRegistrada::repetida).hasSize(hilos - 1);
    }

    @Test
    @DisplayName("Dos TOQUES en el mismo segundo (dos claves): entran los dos y se ven los dos (regla 1)")
    void dobleToqueEntranLasDos() {
        registrar(TipoMarcacion.ENTRADA, a(9, 0));
        MarcacionRegistrada r = registrar(TipoMarcacion.ENTRADA, a(9, 0));

        assertThat(r.marcacionesHoy()).isEqualTo(2);
        assertThat(guardadas(DIA)).hasSize(2);
        assertThat(celda(DIA).etiqueta()).isEqualTo("9:00AM -\n9:00AM -");
    }

    @Test
    @DisplayName("Una nota enorme se corta; la marcacion no se pierde")
    void notaEnormeSeCortaLaNota() {
        // 600 emojis: en Java son 1200 unidades UTF-16, en MySQL 600 caracteres.
        String nota = "😀".repeat(600);
        Marcacion conNota = new Marcacion(0, PERSONA, SEDE, TipoMarcacion.ENTRADA, a(9, 0),
                FuenteHora.SERVIDOR, null, OrigenMarcacion.TRABAJADORA, nota);

        marcaciones.registrar(MarcacionNueva.deTrabajadora(conNota, UUID.randomUUID().toString()));

        String guardada = guardadas(DIA).getFirst().notas();
        assertThat(guardada.codePointCount(0, guardada.length())).isEqualTo(Recorte.NOTAS);
        assertThat(guardada).startsWith("😀😀").endsWith("…");
    }

    @Test
    @DisplayName("Las fracciones de segundo se cortan, no se redondean hacia adelante")
    void fraccionesDeSegundo() {
        registrar(TipoMarcacion.ENTRADA, DIA.atTime(9, 0, 59, 900_000_000));

        assertThat(guardadas(DIA).getFirst().momento()).isEqualTo(DIA.atTime(9, 0, 59));
    }

    @Test
    @DisplayName("Si la celda no se puede guardar, la marcacion se queda igual (regla 1)")
    void siFallaLaCeldaLaMarcacionSeQueda() {
        // jornada_diaria.turnos es TINYINT UNSIGNED: maximo 255. Sin tope de
        // marcaciones (ADR-012), la 512.a marcacion del dia hace 256 turnos y
        // sp_actualizar_jornada da error de rango. Es absurdo en la practica,
        // pero es un fallo REAL de la segunda mitad del circuito, sin trucos
        // ni simulaciones, y la regla dice que ni asi se pierde la hora.
        LocalDateTime inicio = a(6, 0);
        MarcacionRegistrada ultima = null;
        for (int i = 0; i < 512; i++) {
            TipoMarcacion tipo = i % 2 == 0 ? TipoMarcacion.ENTRADA : TipoMarcacion.SALIDA;
            ultima = registrar(tipo, inicio.plusSeconds(i * 10L));
        }

        assertThat(guardadas(DIA)).as("marcaciones guardadas").hasSize(512);
        assertThat(ultima.celdaActualizada()).as("la celda de la 512.a").isFalse();
        // La celda se quedo como estaba con la 511.a: 255 turnos completos.
        assertThat(celda(DIA).turnos()).isEqualTo(255);
    }

    @Test
    @DisplayName("Diez envios simultaneos de la misma persona: la celda cuenta los diez")
    void enviosSimultaneos() throws Exception {
        List<String> fallos = new ArrayList<>();
        // Cinco rondas en cinco dias distintos: una carrera puede no darse en
        // una ronda suelta, y una prueba de concurrencia que pasa por suerte
        // no prueba nada.
        for (int ronda = 0; ronda < 5; ronda++) {
            LocalDate dia = DIA.plusDays(ronda);
            int hilos = 10;
            CountDownLatch salida = new CountDownLatch(1);
            List<Future<MarcacionRegistrada>> envios = new ArrayList<>();

            try (ExecutorService pool = Executors.newFixedThreadPool(hilos)) {
                for (int i = 0; i < hilos; i++) {
                    TipoMarcacion tipo = i % 2 == 0 ? TipoMarcacion.ENTRADA : TipoMarcacion.SALIDA;
                    LocalDateTime cuando = dia.atTime(9, 0).plusMinutes(i);
                    envios.add(pool.submit(() -> {
                        salida.await(); // todos esperan aqui y arrancan a la vez
                        return registrar(tipo, cuando);
                    }));
                }
                salida.countDown();
                for (Future<MarcacionRegistrada> f : envios) {
                    try {
                        f.get();
                    } catch (Exception e) {
                        fallos.add(dia + ": un envio fallo -> " + e.getCause());
                    }
                }
            }

            List<Marcacion> todas = guardadas(dia);
            Jornada esperada = Jornada.de(SEDE, dia, PERSONA, todas);
            FilaDiaSede c = celda(dia);
            if (todas.size() != hilos) {
                fallos.add(dia + ": se guardaron " + todas.size() + " de " + hilos);
            }
            if (!c.etiqueta().equals(esperada.etiqueta()) || c.minutos() != esperada.minutos()) {
                fallos.add(dia + ": la celda dice " + c.minutos() + " min ["
                        + c.etiqueta().replace("\n", " | ") + "] y deberia decir "
                        + esperada.minutos() + " min [" + esperada.etiqueta().replace("\n", " | ") + "]");
            }
        }
        assertThat(fallos).isEmpty();
    }
}
