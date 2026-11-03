
package reservas.frontend;

import reservas.frontend.proxy.ServicioProxy;
import reservas.protocol.Respuesta;

import java.io.IOException;

public class ClientePrueba {

    public static void main(String[] args) {

        ServicioProxy proxy =
                new ServicioProxy("127.0.0.1", 5000);

        System.out.println("================================");
        System.out.println(" PRUEBA DEL PROXY");
        System.out.println("================================");

        try {

            // Primera prueba: operación válida
            Respuesta respuesta = proxy.ejecutar(
                    "PING",
                    null
            );

            System.out.println("PRUEBA 1: PING");
            System.out.println("Exito: " + respuesta.isExito());
            System.out.println("Mensaje: " + respuesta.getMensaje());
            System.out.println("Datos: " + respuesta.getDatos());

            // Segunda prueba: operación no reconocida
            Respuesta error = proxy.ejecutar(
                    "OPERACION_INVALIDA",
                    null
            );

            System.out.println();
            System.out.println("PRUEBA 2: OPERACION INVALIDA");
            System.out.println("Exito: " + error.isExito());
            System.out.println("Mensaje: " + error.getMensaje());

            // Validación automática
            if (respuesta.isExito()
                    && "PONG".equals(respuesta.getMensaje())
                    && !error.isExito()) {

                System.out.println();
                System.out.println("RESULTADO: PRUEBAS EXITOSAS");

            } else {

                System.err.println();
                System.err.println("RESULTADO: PRUEBAS FALLIDAS");
            }

        } catch (IOException e) {

            System.err.println(
                    "Error al comunicarse con el servidor: "
                            + e.getMessage()
            );
        }
    }
}