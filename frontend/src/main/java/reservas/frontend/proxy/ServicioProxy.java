
package reservas.frontend.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import reservas.protocol.Solicitud;
import reservas.protocol.Respuesta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServicioProxy {

    private final String host;
    private final int puerto;
    private final ObjectMapper json;

    public ServicioProxy(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        this.json = new ObjectMapper();
    }

    public Respuesta ejecutar(String operacion, String datos)
            throws IOException {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(host, puerto),
                    5000
            );

            socket.setSoTimeout(5000);

            try (
                    PrintWriter salida = new PrintWriter(
                            socket.getOutputStream(),
                            true,
                            StandardCharsets.UTF_8
                    );

                    BufferedReader entrada = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {

                Solicitud solicitud =
                        new Solicitud(operacion, datos);

                salida.println(
                        json.writeValueAsString(solicitud)
                );

                String mensaje = entrada.readLine();

                if (mensaje == null) {
                    throw new IOException(
                            "El servidor cerro la conexion"
                    );
                }

                return json.readValue(
                        mensaje,
                        Respuesta.class
                );
            }
        }
    }
}
