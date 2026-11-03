
package reservas.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import reservas.protocol.Solicitud;
import reservas.protocol.Respuesta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    private static final int PUERTO = 5000;
    private static final ObjectMapper JSON = new ObjectMapper();

    public static void main(String[] args) {

        ExecutorService clientes = Executors.newFixedThreadPool(20);

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("================================");
            System.out.println(" SERVIDOR SISTEMA DE RESERVAS");
            System.out.println(" Puerto: " + PUERTO);
            System.out.println(" Esperando clientes...");
            System.out.println("================================");

            while (true) {
                Socket socket = servidor.accept();

                System.out.println(
                        "Cliente conectado: " +
                                socket.getRemoteSocketAddress()
                );

                clientes.execute(() -> atenderCliente(socket));
            }

        } catch (IOException e) {
            System.err.println(
                    "Error del servidor: " + e.getMessage()
            );
        } finally {
            clientes.shutdown();
        }
    }

    private static void atenderCliente(Socket socket) {

        try (
                socket;
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(),
                        true,
                        StandardCharsets.UTF_8
                )
        ) {

            String linea;

            while ((linea = entrada.readLine()) != null) {

                try {
                    Solicitud solicitud =
                            JSON.readValue(linea, Solicitud.class);

                    Respuesta respuesta;

                    if ("PING".equals(solicitud.getOperacion())) {

                        respuesta = new Respuesta(
                                true,
                                "PONG",
                                "Servidor funcionando correctamente"
                        );

                    } else {

                        respuesta = new Respuesta(
                                false,
                                "Operacion no reconocida",
                                null
                        );
                    }

                    salida.println(
                            JSON.writeValueAsString(respuesta)
                    );

                } catch (Exception e) {

                    Respuesta error = new Respuesta(
                            false,
                            "Solicitud invalida: " + e.getMessage(),
                            null
                    );

                    salida.println(
                            JSON.writeValueAsString(error)
                    );
                }
            }

            System.out.println(
                    "Cliente desconectado: " +
                            socket.getRemoteSocketAddress()
            );

        } catch (IOException e) {
            System.err.println(
                    "Error de comunicacion: " + e.getMessage()
            );
        }
    }
}
