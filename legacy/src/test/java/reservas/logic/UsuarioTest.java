package reservas.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UsuarioTest {

    @Test
    void crearUsuarioCorrectamente() {

        Usuario usuario =
                new Usuario(
                        "usuario1",
                        "clave123",
                        Rol.FUNCIONARIO
                );

        assertEquals(
                "usuario1",
                usuario.getId()
        );

        assertEquals(
                "clave123",
                usuario.getClave()
        );

        assertEquals(
                Rol.FUNCIONARIO,
                usuario.getRol()
        );
    }

    @Test
    void cambiarClaveUsuario() {

        Usuario usuario =
                new Usuario(
                        "usuario1",
                        "clave1",
                        Rol.FUNCIONARIO
                );

        usuario.setClave(
                "clave2"
        );

        assertEquals(
                "clave2",
                usuario.getClave()
        );
    }
}