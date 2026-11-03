package reservas.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LlmService {

    private static final String API_URL =
            "https://api.openai.com/v1/responses";

    private static final String MODEL =
            "gpt-5.6-luna";

    private final ObjectMapper mapper;
    private final HttpClient client;

    public LlmService() {
        mapper = new ObjectMapper();
        client = HttpClient.newHttpClient();
    }

    public ReservaInterpretada interpretar(
            String frase,
            List<String> categoriasDisponibles) {

        if (frase == null || frase.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe escribir una solicitud."
            );
        }

        String apiKey =
                System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "No está configurada la variable OPENAI_API_KEY."
            );
        }

        try {

            String prompt =
                    construirPrompt(
                            frase,
                            categoriasDisponibles
                    );

            String requestBody =
                    mapper.writeValueAsString(
                            new RequestData(
                                    MODEL,
                                    prompt
                            )
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            requestBody
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Error del servicio de IA. Código HTTP: "
                                + response.statusCode()
                );
            }

            String texto =
                    extraerTextoRespuesta(
                            response.body()
                    );

            return convertirRespuesta(
                    texto
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "La solicitud a la IA fue interrumpida.",
                    e
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo interpretar la solicitud: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String construirPrompt(
            String frase,
            List<String> categoriasDisponibles) {

        return """
                Eres parte de un sistema de reservas de recursos.

                Interpreta la solicitud del usuario y responde
                EXACTAMENTE con este formato:

                ACTIVIDAD=texto
                FECHA=AAAA-MM-DD
                HORA_INICIO=HH:mm
                HORA_FIN=HH:mm
                CATEGORIAS=categoria1|categoria2

                Fecha actual: %s

                Las únicas categorías válidas son:
                %s

                Horas válidas del sistema:
                06:00, 07:00, 08:00, 09:00, 10:00,
                11:00, 12:00, 13:00, 14:00, 15:00,
                16:00, 17:00, 18:00, 19:00, 20:00,
                21:00, 22:00.

                Reglas:
                - Usa solamente categorías de la lista anterior.
                - No inventes categorías.
                - Si una categoría solicitada no coincide exactamente,
                  elige la categoría existente más apropiada.
                - Usa formato de 24 horas.
                - Usa únicamente horas completas.
                - Los minutos siempre deben ser 00.
                - HORA_INICIO debe ser anterior a HORA_FIN.
                - No uses horas anteriores a 06:00.
                - No uses horas posteriores a 22:00.
                - No agregues explicaciones.
                - No uses Markdown.
                - No crees la reserva.
                - Solo completa los datos del formulario.
                - Si el usuario menciona una hora con minutos,
                  ajústala a una hora completa válida cercana.
                - La reserva será confirmada posteriormente
                  por el usuario.

                Solicitud:
                %s
                """.formatted(
                LocalDate.now(),
                String.join(
                        ", ",
                        categoriasDisponibles
                ),
                frase
        );
    }

    private String extraerTextoRespuesta(
            String responseBody) throws Exception {

        JsonNode root =
                mapper.readTree(
                        responseBody
                );

        JsonNode output =
                root.get("output");

        if (output == null
                || !output.isArray()) {

            throw new RuntimeException(
                    "La IA no devolvió una respuesta válida."
            );
        }

        for (JsonNode item : output) {

            JsonNode content =
                    item.get("content");

            if (content == null
                    || !content.isArray()) {

                continue;
            }

            for (JsonNode contenido : content) {

                JsonNode text =
                        contenido.get("text");

                if (text != null) {
                    return text.asText();
                }
            }
        }

        throw new RuntimeException(
                "No se encontró texto en la respuesta de la IA."
        );
    }

    private ReservaInterpretada convertirRespuesta(
            String texto) {

        ReservaInterpretada resultado =
                new ReservaInterpretada();

        List<String> categorias =
                new ArrayList<>();

        String[] lineas =
                texto.split("\\R");

        for (String linea : lineas) {

            int posicion =
                    linea.indexOf('=');

            if (posicion == -1) {
                continue;
            }

            String clave =
                    linea.substring(
                            0,
                            posicion
                    ).trim();

            String valor =
                    linea.substring(
                            posicion + 1
                    ).trim();

            switch (clave) {

                case "ACTIVIDAD" ->
                        resultado.setActividad(
                                valor
                        );

                case "FECHA" ->
                        resultado.setFecha(
                                LocalDate.parse(
                                        valor
                                )
                        );

                case "HORA_INICIO" ->
                        resultado.setHoraInicio(
                                LocalTime.parse(
                                        valor
                                )
                        );

                case "HORA_FIN" ->
                        resultado.setHoraFin(
                                LocalTime.parse(
                                        valor
                                )
                        );

                case "CATEGORIAS" -> {

                    if (!valor.isBlank()) {

                        String[] partes =
                                valor.split("\\|");

                        for (String parte : partes) {

                            if (!parte.isBlank()) {

                                categorias.add(
                                        parte.trim()
                                );
                            }
                        }
                    }
                }
            }
        }

        resultado.setCategorias(
                categorias
        );

        validarResultado(
                resultado
        );

        return resultado;
    }

    private void validarResultado(
            ReservaInterpretada resultado) {

        if (resultado.getActividad() == null
                || resultado.getActividad().isBlank()) {

            throw new RuntimeException(
                    "La IA no pudo determinar la actividad."
            );
        }

        if (resultado.getFecha() == null) {

            throw new RuntimeException(
                    "La IA no pudo determinar la fecha."
            );
        }

        if (resultado.getFecha()
                .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "La IA devolvió una fecha pasada."
            );
        }

        if (resultado.getHoraInicio() == null
                || resultado.getHoraFin() == null) {

            throw new RuntimeException(
                    "La IA no pudo determinar el horario."
            );
        }

        if (!horaValida(
                resultado.getHoraInicio())) {

            throw new RuntimeException(
                    "La IA devolvió una hora de inicio "
                            + "no permitida por el sistema."
            );
        }

        if (!horaValida(
                resultado.getHoraFin())) {

            throw new RuntimeException(
                    "La IA devolvió una hora de finalización "
                            + "no permitida por el sistema."
            );
        }

        if (!resultado.getHoraInicio()
                .isBefore(
                        resultado.getHoraFin()
                )) {

            throw new RuntimeException(
                    "La IA devolvió un rango de horas inválido."
            );
        }

        if (resultado.getCategorias() == null
                || resultado.getCategorias().isEmpty()) {

            throw new RuntimeException(
                    "La IA no pudo determinar las categorías."
            );
        }
    }

    private boolean horaValida(
            LocalTime hora) {

        if (hora == null) {
            return false;
        }

        if (hora.getMinute() != 0
                || hora.getSecond() != 0) {

            return false;
        }

        LocalTime horaMinima =
                LocalTime.of(
                        6,
                        0
                );

        LocalTime horaMaxima =
                LocalTime.of(
                        22,
                        0
                );

        return !hora.isBefore(
                horaMinima
        )
                &&
                !hora.isAfter(
                        horaMaxima
                );
    }

    private record RequestData(
            String model,
            String input) {
    }
}