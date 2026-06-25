package de.kaufeschlau.discounter.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "list",
        description = "Prospektlinks vom Backend abrufen.",
        mixinStandardHelpOptions = true)
public class ListCommand implements Callable<Integer> {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient;
    private final String backendUrl;
    private final PrintWriter out;
    private final PrintWriter err;

    @Option(names = "--plz", description = "Fünfstellige Postleitzahl für standortabhängige Händler.")
    String plz;

    @Option(names = "--region", description = "Region oder Bundesland als Alternative zur PLZ.")
    String region;

    @Option(names = "--id", description = "Einzelne Händler-ID.")
    String id;

    @Option(names = "--ids", split = ",", description = "Mehrere Händler-IDs als CSV, zum Beispiel lidl,penny.")
    List<String> ids = new ArrayList<>();

    @Option(names = "--format", defaultValue = "plain", description = "Ausgabeformat: ${COMPLETION-CANDIDATES}.")
    OutputFormat format;

    public ListCommand() {
        this(System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8080"), new PrintWriter(System.out, true),
                new PrintWriter(System.err, true));
    }

    ListCommand(String backendUrl, PrintWriter out, PrintWriter err) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(), backendUrl, out, err);
    }

    ListCommand(HttpClient httpClient, String backendUrl, PrintWriter out, PrintWriter err) {
        this.httpClient = httpClient;
        this.backendUrl = backendUrl;
        this.out = out;
        this.err = err;
    }

    @Override
    public Integer call() {
        try {
            var response = httpClient.send(request(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                renderSuccess(response.body());
                return 0;
            }

            err.println(errorMessage(response.statusCode(), response.body()));
            return 1;
        } catch (IOException e) {
            if (isUnreachableBackend(e)) {
                err.println("Backend nicht erreichbar: " + backendUrl);
                return 1;
            }
            err.println("Backend-Aufruf fehlgeschlagen: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            err.println("Backend-Aufruf abgebrochen.");
            return 1;
        }
    }

    private static boolean isUnreachableBackend(IOException e) {
        return e instanceof ConnectException
                || e instanceof UnknownHostException
                || e instanceof HttpConnectTimeoutException;
    }

    private HttpRequest request() {
        return HttpRequest.newBuilder(URI.create(backendUrl.replaceAll("/+$", "") + "/api/v1/prospects" + query()))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
    }

    private String query() {
        var params = new ArrayList<String>();
        add(params, "plz", plz);
        add(params, "region", region);

        var selectedIds = new ArrayList<String>();
        if (id != null && !id.isBlank()) {
            selectedIds.add(id);
        }
        selectedIds.addAll(ids);
        if (!selectedIds.isEmpty()) {
            add(params, "retailerIds", String.join(",", selectedIds));
        }

        return params.isEmpty() ? "" : "?" + String.join("&", params);
    }

    private static void add(List<String> params, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        params.add(name + "=" + url(value));
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void renderSuccess(String body) throws IOException {
        if (format == OutputFormat.json) {
            out.println(body);
            return;
        }

        var root = JSON.readTree(body);
        var prospects = root.isArray() ? root : root.path("items");
        if (!prospects.isArray()) {
            out.println(body);
            return;
        }

        for (var prospect : prospects) {
            var name = text(prospect, "name", text(prospect, "id", "unbekannt"));
            var url = text(prospect, "prospectUrl", text(prospect, "url", ""));
            out.println(name + ": " + url);

            var notice = text(prospect, "notice", text(prospect, "message", ""));
            if (!notice.isBlank()) {
                out.println("  Hinweis: " + notice);
            }
        }
    }

    private static String errorMessage(int status, String body) {
        try {
            var root = JSON.readTree(body);
            var code = text(root, "code", text(root, "errorCode", ""));
            var message = text(root, "message", text(root, "detail", text(root, "error", body)));
            return code.isBlank() ? message : code + ": " + message;
        } catch (IOException e) {
            return "Backend-Fehler (" + status + "): " + body;
        }
    }

    private static String text(JsonNode node, String field, String fallback) {
        var value = node.path(field);
        return value.isTextual() ? value.asText() : fallback;
    }
}
