package br.com.assurance.projetoassurance.exemplo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
//br.com.assurance.projetoassurance.exemplo.Handler
public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String BASE_URL = "https://viacep.com.br/ws/%s/json/";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        long start = System.currentTimeMillis();

        try {
            String cep = extrairCep(event);
            cep = cep.replaceAll("\\D", "");
            if (cep.length() != 8) {
                return resposta(400, Map.of("erro", "CEP invalido. Deve conter 8 digitos."));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(BASE_URL, cep)))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            CepResponse result = mapper.readValue(response.body(), CepResponse.class);

            long end = System.currentTimeMillis();
            result.requestTimeMs = end - start;

            return resposta(200, result);

        } catch (IllegalArgumentException e) {
            return resposta(400, Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return resposta(500, Map.of("erro", "Erro ao consultar ViaCEP: " + e.getMessage()));
        }
    }

    /**
     * Le o CEP tanto de uma invocacao HTTP (campo "body" com JSON em String)
     * quanto de uma invocacao direta (evento ja contendo "cep").
     */
    private String extrairCep(Map<String, Object> event) throws Exception {
        if (event == null) {
            throw new IllegalArgumentException("Evento vazio.");
        }

        // Invocacao via Function URL / API Gateway: CEP vem dentro de "body"
        Object body = event.get("body");
        if (body instanceof String s && !s.isBlank()) {
            JsonNode node = mapper.readTree(s);
            JsonNode cepNode = node.get("cep");
            if (cepNode != null && !cepNode.asText().isBlank()) {
                return cepNode.asText();
            }
        }

        // Invocacao direta (aba Test com {"cep": "..."})
        Object cepDireto = event.get("cep");
        if (cepDireto != null && !cepDireto.toString().isBlank()) {
            return cepDireto.toString();
        }

        throw new IllegalArgumentException("Parametro 'cep' nao informado.");
    }

    /**
     * Monta a resposta no formato esperado pela Function URL / API Gateway.
     */
    private Map<String, Object> resposta(int statusCode, Object corpo) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", statusCode);
        resp.put("headers", Map.of("Content-Type", "application/json"));
        try {
            resp.put("body", mapper.writeValueAsString(corpo));
        } catch (Exception e) {
            resp.put("body", "{\"erro\":\"falha ao serializar resposta\"}");
        }
        return resp;
    }
}

