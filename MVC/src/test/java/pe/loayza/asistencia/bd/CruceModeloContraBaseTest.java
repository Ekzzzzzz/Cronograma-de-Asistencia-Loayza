package pe.loayza.asistencia.bd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import pe.loayza.asistencia.modelo.Jornada;
import pe.loayza.asistencia.modelo.Marcacion;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>La primera vez que el modelo Java ve filas reales</h2>
 *
 * El semillero ({@code SQL/03_datos_prueba.sql}) calcula las celdas de
 * {@code jornada_diaria} <b>en SQL</b>, con su propio bucle. Esta prueba
 * vuelve a calcular cada una <b>en Java</b>, con {@link Jornada#de}, partiendo
 * de las marcaciones crudas, y exige que salgan identicas caracter por
 * caracter.
 *
 * <p>Si pasa, quedan probadas tres cosas a la vez: que los mappers leen bien,
 * que el emparejamiento funciona sobre datos que nunca vio, y que las dos
 * implementaciones —la del semillero y la del modelo— coinciden.
 *
 * <h3>Como se prueba la capa sola</h3>
 * {@code @JdbcTest} levanta <b>solo</b> la parte de Spring que habla con la base
 * —el pool de conexiones, el {@code JdbcTemplate} y las transacciones—, sin
 * servidor web ni Thymeleaf. Por defecto cambiaria MySQL por una base en
 * memoria; {@code replace = NONE} se lo prohibe, porque lo que se prueba aqui
 * son <b>los procedimientos de MySQL</b>, y una base en memoria no los tiene.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MarcacionesBD.class, CronogramaBD.class})
@DisplayName("Cruce: el modelo Java recalcula las celdas que el semillero calculo en SQL")
class CruceModeloContraBaseTest {

    @Autowired
    MarcacionesBD marcaciones;

    @Autowired
    CronogramaBD cronograma;

    @Test
    @DisplayName("Cada celda de jornada_diaria sale igual recalculada en Java")
    void cadaCeldaSaleIgual() {
        LocalDate hoy = LocalDate.now();
        int comparadas = 0;
        List<String> diferencias = new ArrayList<>();

        for (int sede = 1; sede <= 7; sede++) {
            // El semillero se ancla a CURDATE(): sus dos semanas caen siempre
            // dentro de las tres ultimas.
            for (LocalDate dia = hoy.minusDays(21); !dia.isAfter(hoy); dia = dia.plusDays(1)) {

                Map<Integer, List<Marcacion>> porPersona = marcaciones.delDia(sede, dia).stream()
                        .map(FilaMarcacion::marcacion)
                        .collect(Collectors.groupingBy(Marcacion::trabajadoraId, TreeMap::new,
                                Collectors.toList()));

                Map<Integer, FilaDiaSede> celdas = cronograma.diaSede(sede, dia).stream()
                        .collect(Collectors.toMap(FilaDiaSede::trabajadoraId, Function.identity()));

                TreeSet<Integer> personas = new TreeSet<>(porPersona.keySet());
                personas.addAll(celdas.keySet());

                for (int persona : personas) {
                    Jornada java = Jornada.de(sede, dia, persona, porPersona.getOrDefault(persona, List.of()));
                    FilaDiaSede sql = celdas.get(persona);
                    String donde = "sede " + sede + ", " + dia + ", trabajadora " + persona + ": ";

                    if (sql == null) {
                        diferencias.add(donde + "tiene marcaciones pero no tiene celda");
                        continue;
                    }
                    comparadas++;
                    if (!java.etiqueta().equals(sql.etiqueta())) {
                        diferencias.add(donde + "etiqueta Java [" + java.etiqueta().replace("\n", " | ")
                                + "] vs SQL [" + sql.etiqueta().replace("\n", " | ") + "]");
                    }
                    if (java.minutos() != sql.minutos()) {
                        diferencias.add(donde + "minutos Java " + java.minutos() + " vs SQL " + sql.minutos());
                    }
                    if (java.turnosCompletos() != sql.turnos()) {
                        diferencias.add(donde + "turnos Java " + java.turnosCompletos() + " vs SQL " + sql.turnos());
                    }
                    if (java.tieneTurnoAbierto() != sql.turnoAbierto()) {
                        diferencias.add(donde + "turno abierto Java " + java.tieneTurnoAbierto()
                                + " vs SQL " + sql.turnoAbierto());
                    }
                }
            }
        }

        assertThat(diferencias).as("celdas donde Java y SQL no coinciden").isEmpty();
        // Si el semillero no se cargo, no hay nada que comparar y la prueba
        // pasaria en vacio. Esto lo impide.
        assertThat(comparadas).as("celdas comparadas").isEqualTo(1180);
    }
}
