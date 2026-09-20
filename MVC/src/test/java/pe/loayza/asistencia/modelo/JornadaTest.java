package pe.loayza.asistencia.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del emparejamiento: la unica regla del sistema que, si sale mal,
 * paga horas que no se trabajaron o deja de pagar las que si.
 *
 * <p><b>Ni una sola prueba de aqui necesita base de datos ni Spring.</b> Eso
 * no es casualidad: es el motivo de que esta logica viva en Java y no en un
 * procedimiento almacenado. Correr esto entero cuesta milisegundos, asi que se
 * puede correr en cada cambio.
 */
@DisplayName("Jornada: emparejar marcaciones en turnos")
class JornadaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 14);
    private static final int SEDE = 1;
    private static final int TRABAJADORA = 7;

    private static Marcacion entrada(int hora, int minuto) {
        return Marcacion.de(TRABAJADORA, SEDE, TipoMarcacion.ENTRADA, en(hora, minuto));
    }

    private static Marcacion salida(int hora, int minuto) {
        return Marcacion.de(TRABAJADORA, SEDE, TipoMarcacion.SALIDA, en(hora, minuto));
    }

    private static LocalDateTime en(int hora, int minuto) {
        return LocalDateTime.of(DIA, LocalTime.of(hora, minuto));
    }

    private static Jornada jornadaCon(Marcacion... marcaciones) {
        return Jornada.de(SEDE, DIA, TRABAJADORA, List.of(marcaciones));
    }

    // =================================================================
    @Nested
    @DisplayName("El caso normal")
    class CasoNormal {

        @Test
        @DisplayName("una entrada y una salida son un turno de esas horas")
        void turnoSimple() {
            Jornada j = jornadaCon(entrada(9, 0), salida(18, 0));

            assertThat(j.turnos()).hasSize(1);
            assertThat(j.turnosCompletos()).isEqualTo(1);
            assertThat(j.minutos()).isEqualTo(540);
            assertThat(j.etiqueta()).isEqualTo("9:00AM - 6:00PM");
            assertThat(j.tieneTurnoAbierto()).isFalse();
        }

        @Test
        @DisplayName("LA REGLA QUE MAS IMPORTA: se suma turno a turno, no de extremo a extremo")
        void dosTurnosNoSumanElAlmuerzo() {
            // 9-1 y 3-8. Son nueve horas trabajadas, no once.
            Jornada j = jornadaCon(
                    entrada(9, 0), salida(13, 0),
                    entrada(15, 0), salida(20, 0));

            assertThat(j.minutos())
                    .as("restar la ultima salida menos la primera entrada daria 660 "
                        + "y el sistema estaria pagando el almuerzo")
                    .isEqualTo(540);

            assertThat(j.primeraEntrada()).isEqualTo(LocalTime.of(9, 0));
            assertThat(j.ultimaSalida()).isEqualTo(LocalTime.of(20, 0));
            assertThat(j.turnosCompletos()).isEqualTo(2);
        }

        @Test
        @DisplayName("la etiqueta lleva un par por linea, como la celda del Excel")
        void etiquetaUnParPorLinea() {
            Jornada j = jornadaCon(
                    entrada(9, 5), salida(13, 3),
                    entrada(14, 1), salida(18, 12));

            assertThat(j.etiqueta()).isEqualTo("9:05AM - 1:03PM\n2:01PM - 6:12PM");
        }
    }

    // =================================================================
    @Nested
    @DisplayName("Lo que ADR-012 obliga a aceptar: nada se rechaza")
    class NadaSeRechaza {

        @Test
        @DisplayName("entrada sin salida: la hora y el guion, con el hueco a la vista")
        void entradaSinSalida() {
            Jornada j = jornadaCon(entrada(10, 12));

            assertThat(j.etiqueta()).isEqualTo("10:12AM -");
            assertThat(j.tieneTurnoAbierto()).isTrue();
            assertThat(j.ultimaSalida()).isNull();
            assertThat(j.minutos())
                    .as("no se sabe cuanto duro; suponerlo falsearia sus horas")
                    .isZero();
            assertThat(j.turnosCompletos()).isZero();
        }

        @Test
        @DisplayName("salida sin entrada: tampoco se descarta")
        void salidaSinEntrada() {
            Jornada j = jornadaCon(salida(20, 46));

            assertThat(j.etiqueta()).isEqualTo("- 8:46PM");
            assertThat(j.turnos()).hasSize(1);
            assertThat(j.turnos().getFirst().huerfano()).isTrue();
            assertThat(j.tieneTurnoAbierto())
                    .as("una salida huerfana no deja a nadie dentro del local")
                    .isFalse();
            assertThat(j.minutos()).isZero();
        }

        @Test
        @DisplayName("doble entrada: se aceptan las dos y la primera queda coja")
        void dobleEntrada() {
            Jornada j = jornadaCon(entrada(9, 0), entrada(9, 30), salida(18, 0));

            assertThat(j.marcaciones())
                    .as("ninguna marcacion se pierde: todas son evidencia")
                    .hasSize(3);
            assertThat(j.etiqueta()).isEqualTo("9:00AM -\n9:30AM - 6:00PM");
            assertThat(j.minutos())
                    .as("solo cuenta el turno que si se cerro: 9:30 a 18:00")
                    .isEqualTo(510);
        }

        @Test
        @DisplayName("doble salida: la segunda queda huerfana, no se descarta")
        void dobleSalida() {
            Jornada j = jornadaCon(entrada(9, 0), salida(13, 0), salida(13, 5));

            assertThat(j.etiqueta()).isEqualTo("9:00AM - 1:00PM\n- 1:05PM");
            assertThat(j.minutos()).isEqualTo(240);
            assertThat(j.marcaciones()).hasSize(3);
        }

        @Test
        @DisplayName("seis pares seguidos en la misma sede se aceptan enteros")
        void seisPares() {
            Jornada j = jornadaCon(
                    entrada(7, 0), salida(8, 0),
                    entrada(9, 0), salida(10, 0),
                    entrada(11, 0), salida(12, 0),
                    entrada(13, 0), salida(14, 0),
                    entrada(15, 0), salida(16, 0),
                    entrada(17, 0), salida(18, 0));

            assertThat(j.turnosCompletos()).isEqualTo(6);
            assertThat(j.minutos()).isEqualTo(360);
            assertThat(j.etiqueta().lines()).hasSize(6);
            assertThat(j.etiqueta().length())
                    .isLessThanOrEqualTo(Jornada.ETIQUETA_MAX);
        }

        @Test
        @DisplayName("ADR-012: ya no hay tope, asi que doce pares tambien entran")
        void masAllaDelViejoTopeDeDoce() {
            // Hasta ADR-012 la marcacion 13 se rechazaba. Ahora no hay
            // numero magico: esta jornada tiene 24 marcaciones y ninguna
            // se pierde. Es absurda, y el sistema la registra igual;
            // quien decide si tiene sentido es una persona, no un limite.
            List<Marcacion> muchas = new ArrayList<>();
            for (int h = 0; h < 12; h++) {
                muchas.add(entrada(h, 0));
                muchas.add(salida(h, 30));
            }

            Jornada j = Jornada.de(SEDE, DIA, TRABAJADORA, muchas);

            assertThat(j.marcaciones()).hasSize(24);
            assertThat(j.turnosCompletos()).isEqualTo(12);
            assertThat(j.minutos()).isEqualTo(12 * 30);
        }

        @Test
        @DisplayName("sin tope, la celda sigue cabiendo en la columna de la base")
        void laEtiquetaNoDesbordaLaColumna() {
            // Esta es la consecuencia tecnica de quitar el tope, y es la
            // que se escapa: sin limite de marcaciones, la etiqueta puede
            // crecer sin fin, y MySQL la cortaria EN SILENCIO. Una jornada
            // a medias que nadie sabe que esta a medias no se encuentra.
            List<Marcacion> exageradas = new ArrayList<>();
            for (int h = 0; h < 24; h++) {
                exageradas.add(entrada(h, 0));
                exageradas.add(salida(h, 30));
            }

            Jornada j = Jornada.de(SEDE, DIA, TRABAJADORA, exageradas);

            assertThat(j.etiqueta().length())
                    .as("tiene que caber en el VARCHAR(400) de jornada_diaria")
                    .isLessThanOrEqualTo(Jornada.ETIQUETA_MAX);
            assertThat(j.etiqueta())
                    .as("se corta por linea entera: media hora escrita es peor que una que falta")
                    .doesNotEndWith("-");
            assertThat(j.turnosCompletos())
                    .as("los turnos NO se truncan aunque el texto si: este numero no miente")
                    .isEqualTo(24);
            assertThat(j.minutos())
                    .as("y las horas tampoco se pierden")
                    .isEqualTo(24 * 30);
        }
    }

    // =================================================================
    @Nested
    @DisplayName("Turno abierto: el error que destapo esta prueba")
    class TurnoAbierto {

        @Test
        @DisplayName("hay turno abierto aunque exista una ultima salida posterior")
        void abiertoConSalidaPrevia() {
            // Este es el caso que rompia la regla «turno abierto = no hay
            // ultima salida», y con multi-marcacion es el caso NORMAL.
            Jornada j = jornadaCon(
                    entrada(9, 0), salida(13, 0),
                    entrada(14, 0), salida(18, 0),
                    entrada(19, 0));

            assertThat(j.ultimaSalida())
                    .as("si hay ultima salida, a las 6")
                    .isEqualTo(LocalTime.of(18, 0));
            assertThat(j.tieneTurnoAbierto())
                    .as("y sin embargo sigue dentro del local desde las 7")
                    .isTrue();
        }

        @Test
        @DisplayName("una jornada vacia no tiene turno abierto")
        void vacia() {
            Jornada j = jornadaCon();

            assertThat(j.vacia()).isTrue();
            assertThat(j.tieneTurnoAbierto()).isFalse();
            assertThat(j.minutos()).isZero();
            assertThat(j.etiqueta()).isEmpty();
            assertThat(j.primeraEntrada()).isNull();
        }
    }

    // =================================================================
    @Nested
    @DisplayName("Orden y cola sin senal")
    class OrdenYCola {

        @Test
        @DisplayName("las marcaciones se ordenan solas: de una cola llegan desordenadas")
        void ordenaAunqueLleguenAlReves() {
            Jornada j = Jornada.de(SEDE, DIA, TRABAJADORA, List.of(
                    salida(18, 0),
                    entrada(14, 0),
                    salida(13, 0),
                    entrada(9, 0)));

            assertThat(j.etiqueta()).isEqualTo("9:00AM - 1:00PM\n2:00PM - 6:00PM");
            assertThat(j.minutos()).isEqualTo(480);
        }

        @Test
        @DisplayName("una marcacion con hora del telefono exige advertencia siempre")
        void advertenciaSinUmbral() {
            Marcacion encolada = new Marcacion(1, TRABAJADORA, SEDE, TipoMarcacion.ENTRADA,
                    en(9, 0), FuenteHora.DISPOSITIVO, en(9, 38),
                    OrigenMarcacion.TRABAJADORA, "sin senal");

            Jornada j = Jornada.de(SEDE, DIA, TRABAJADORA, List.of(encolada, salida(18, 0)));

            assertThat(j.exigeAdvertencia()).isTrue();
            assertThat(j.desfaseMaximoMinutos()).isEqualTo(38);
            assertThat(j.minutos())
                    .as("el turno se cuenta con la hora del telefono, que es la que vale")
                    .isEqualTo(540);
        }

        @Test
        @DisplayName("un turno normal no exige ninguna advertencia")
        void sinAdvertencia() {
            Jornada j = jornadaCon(entrada(9, 0), salida(18, 0));

            assertThat(j.exigeAdvertencia()).isFalse();
            assertThat(j.desfaseMaximoMinutos()).isZero();
        }
    }

    // =================================================================
    @Nested
    @DisplayName("El contador que ve la trabajadora")
    class Contador {

        @Test
        @DisplayName("dice «1 de 2», «2 de 2», «3 de 4» — nunca «de 12»")
        void cuentaEnPares() {
            assertThat(ContadorDeHoy.para(1).mensaje()).isEqualTo("Marcacion 1 de 2 de hoy");
            assertThat(ContadorDeHoy.para(2).mensaje()).isEqualTo("Marcacion 2 de 2 de hoy");
            assertThat(ContadorDeHoy.para(3).mensaje()).isEqualTo("Marcacion 3 de 4 de hoy");
            assertThat(ContadorDeHoy.para(4).mensaje()).isEqualTo("Marcacion 4 de 4 de hoy");
            assertThat(ContadorDeHoy.para(5).mensaje()).isEqualTo("Marcacion 5 de 6 de hoy");
        }

        @Test
        @DisplayName("el total nunca va por delante: el formulario no delata cuanto margen queda")
        void noAnunciaElTope() {
            // La propiedad que de verdad protege al endpoint publico no es
            // «que no aparezca el numero 12» -con 11 marcaciones el par
            // siguiente ES 12, y eso es una coincidencia aritmetica inocente-.
            // Es que el total mostrado NUNCA se aleje de lo que ella lleva
            // hecho: como mucho una mas. Asi el mensaje jamas revela cuantos
            // envios admite todavia el sistema.
            for (int hechas = 1; hechas <= 20; hechas++) {
                ContadorDeHoy c = ContadorDeHoy.para(hechas);
                assertThat(c.total())
                        .as("con %d marcaciones el contador no puede insinuar mas margen", hechas)
                        .isLessThanOrEqualTo(hechas + 1)
                        .isGreaterThanOrEqualTo(hechas);
            }
        }

        @Test
        @DisplayName("la jornada sabe su propio contador")
        void contadorDesdeLaJornada() {
            Jornada j = jornadaCon(entrada(9, 0), salida(13, 0), entrada(14, 0));

            assertThat(j.contador().mensaje()).isEqualTo("Marcacion 3 de 4 de hoy");
            assertThat(j.contador().parCompleto()).isFalse();
        }
    }

    // =================================================================
    @Nested
    @DisplayName("Lo que el modelo se niega a construir")
    class Invariantes {

        @Test
        @DisplayName("un turno sin ninguna marcacion no existe")
        void turnoVacio() {
            assertThatThrownBy(() -> new Turno(null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("no se puede meter una SALIDA en el hueco de la entrada")
        void tiposCruzados() {
            assertThatThrownBy(() -> new Turno(salida(9, 0), null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("una marcacion sin hora no sirve de evidencia")
        void marcacionSinHora() {
            assertThatThrownBy(() -> Marcacion.de(1, 1, TipoMarcacion.ENTRADA, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
