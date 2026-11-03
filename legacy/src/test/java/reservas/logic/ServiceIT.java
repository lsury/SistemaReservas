package reservas.logic;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceIT {

    private static final String TEST_FILE =
            "target/test-data/integration-test.xml";

    @Test
    void guardarYCargarCategoriaDesdeXml() {

        File archivo =
                new File(TEST_FILE);

        if (archivo.exists()) {
            archivo.delete();
        }

        Service service1 =
                new Service(TEST_FILE);

        Categoria categoria =
                new Categoria(
                        "1",
                        "Proyector"
                );

        service1.agregarCategoria(
                categoria
        );

        assertTrue(
                archivo.exists()
        );

        Service service2 =
                new Service(TEST_FILE);

        Categoria cargada =
                service2.buscarCategoriaPorId(
                        "1"
                );

        assertNotNull(
                cargada
        );

        assertEquals(
                "Proyector",
                cargada.getDescripcion()
        );
    }
}