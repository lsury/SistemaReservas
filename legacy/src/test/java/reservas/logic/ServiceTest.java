package reservas.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

    private Service service;

    private static final String TEST_FILE =
            "target/test-data/service-test.xml";

    @BeforeEach
    void preparar() {

        File archivo =
                new File(TEST_FILE);

        if (archivo.exists()) {
            archivo.delete();
        }

        service =
                new Service(TEST_FILE);
    }

    @Test
    void loginAdministradorCorrecto() {

        Usuario usuario =
                service.login(
                        "admin",
                        "admin"
                );

        assertNotNull(usuario);

        assertEquals(
                "admin",
                usuario.getId()
        );

        assertEquals(
                Rol.ADMINISTRADOR,
                usuario.getRol()
        );
    }

    @Test
    void loginConClaveIncorrecta() {

        Usuario usuario =
                service.login(
                        "admin",
                        "incorrecta"
                );

        assertNull(usuario);
    }

    @Test
    void agregarCategoria() {

        Categoria categoria =
                new Categoria(
                        "1",
                        "Proyector"
                );

        service.agregarCategoria(
                categoria
        );

        assertEquals(
                1,
                service.getCategorias().size()
        );

        assertEquals(
                "Proyector",
                service.getCategorias()
                        .get(0)
                        .getDescripcion()
        );
    }
    @Test
    void crearReservaCorrectamente() {

        Funcionario funcionario =
                new Funcionario(
                        "func1",
                        "func1",
                        Rol.FUNCIONARIO,
                        "Funcionario Uno",
                        "8888-8888"
                );

        service.agregarFuncionario(funcionario);

        Categoria categoria =
                new Categoria(
                        "1",
                        "Laptop"
                );

        service.agregarCategoria(categoria);

        Recurso recurso =
                new Recurso(
                        "LAP-001",
                        categoria,
                        "Laptop Dell"
                );

        service.agregarRecurso(recurso);

        Reserva reserva =
                service.crearReserva(
                        "Reunión",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(8, 0),
                        LocalTime.of(10, 0),
                        funcionario,
                        List.of(categoria)
                );

        assertNotNull(reserva);

        assertEquals(
                "Reunión",
                reserva.getActividad()
        );

        assertEquals(
                EstadoReserva.ACTIVA,
                reserva.getEstado()
        );

        assertEquals(
                1,
                reserva.getRecursos().size()
        );

        assertEquals(
                "LAP-001",
                reserva.getRecursos()
                        .get(0)
                        .getId()
        );
    }

    @Test
    void rechazarReservaConConflicto() {

        Funcionario funcionario =
                new Funcionario(
                        "func1",
                        "func1",
                        Rol.FUNCIONARIO,
                        "Funcionario Uno",
                        "8888-8888"
                );

        service.agregarFuncionario(funcionario);

        Categoria categoria =
                new Categoria(
                        "1",
                        "Laptop"
                );

        service.agregarCategoria(categoria);

        Recurso recurso =
                new Recurso(
                        "LAP-001",
                        categoria,
                        "Laptop Dell"
                );

        service.agregarRecurso(recurso);

        LocalDate fecha =
                LocalDate.now().plusDays(1);

        service.crearReserva(
                "Primera reunión",
                fecha,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                funcionario,
                List.of(categoria)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.crearReserva(
                                "Segunda reunión",
                                fecha,
                                LocalTime.of(9, 0),
                                LocalTime.of(11, 0),
                                funcionario,
                                List.of(categoria)
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "No hay recursos disponibles"
                        )
        );
    }

    @Test
    void permitirHorarioConsecutivo() {

        Funcionario funcionario =
                new Funcionario(
                        "func1",
                        "func1",
                        Rol.FUNCIONARIO,
                        "Funcionario Uno",
                        "8888-8888"
                );

        service.agregarFuncionario(funcionario);

        Categoria categoria =
                new Categoria(
                        "1",
                        "Laptop"
                );

        service.agregarCategoria(categoria);

        Recurso recurso =
                new Recurso(
                        "LAP-001",
                        categoria,
                        "Laptop Dell"
                );

        service.agregarRecurso(recurso);

        LocalDate fecha =
                LocalDate.now().plusDays(1);

        service.crearReserva(
                "Primera reunión",
                fecha,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                funcionario,
                List.of(categoria)
        );

        Reserva segunda =
                service.crearReserva(
                        "Segunda reunión",
                        fecha,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        funcionario,
                        List.of(categoria)
                );

        assertNotNull(segunda);

        assertEquals(
                "Segunda reunión",
                segunda.getActividad()
        );
    }

    @Test
    void cancelarReservaFutura() {

        Funcionario funcionario =
                new Funcionario(
                        "func1",
                        "func1",
                        Rol.FUNCIONARIO,
                        "Funcionario Uno",
                        "8888-8888"
                );

        service.agregarFuncionario(funcionario);

        Categoria categoria =
                new Categoria(
                        "1",
                        "Laptop"
                );

        service.agregarCategoria(categoria);

        Recurso recurso =
                new Recurso(
                        "LAP-001",
                        categoria,
                        "Laptop Dell"
                );

        service.agregarRecurso(recurso);

        Reserva reserva =
                service.crearReserva(
                        "Reunión",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(8, 0),
                        LocalTime.of(10, 0),
                        funcionario,
                        List.of(categoria)
                );

        service.cancelarReserva(
                reserva,
                funcionario
        );

        assertEquals(
                EstadoReserva.CANCELADA,
                reserva.getEstado()
        );
    }
}