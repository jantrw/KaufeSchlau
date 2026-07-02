package de.kaufeschlau.discounter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ListCommandTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void buildsRequestFromArguments() throws Exception {
        var requestedQuery = new AtomicReference<String>();
        var command = command(200, "{\"items\":[{\"id\":\"lidl\",\"name\":\"Lidl\",\"prospectUrl\":\"https://example.test\"}]}",
                requestedQuery);

        var exitCode = new CommandLine(command).execute(
                "--plz", "65185", "--region", "hessen", "--id", "lidl", "--ids", "penny,kaufland");

        assertEquals(0, exitCode);
        assertTrue(requestedQuery.get().contains("plz=65185"));
        assertTrue(requestedQuery.get().contains("region=hessen"));
        assertTrue(requestedQuery.get().contains("retailerIds=lidl%2Cpenny%2Ckaufland"));
    }

    @Test
    void rendersPlainSuccess() throws Exception {
        var out = new StringWriter();
        var command = command(200, "{\"items\":[{\"name\":\"Lidl\",\"prospectUrl\":\"https://example.test/lidl\"}]}", out,
                new StringWriter(), new AtomicReference<>());

        var exitCode = new CommandLine(command).execute("--ids", "lidl");

        assertEquals(0, exitCode);
        assertEquals("Lidl: https://example.test/lidl%n".formatted(), out.toString());
    }

    @Test
    void printsBackendError() throws Exception {
        var err = new StringWriter();
        var command = command(400, "{\"code\":\"LOCATION_REQUIRED\",\"message\":\"PLZ oder Region erforderlich\"}",
                new StringWriter(), err, new AtomicReference<>());

        var exitCode = new CommandLine(command).execute();

        assertEquals(1, exitCode);
        assertEquals("LOCATION_REQUIRED: PLZ oder Region erforderlich%n".formatted(), err.toString());
    }

    @Test
    void printsUnknownDiscounterError() throws Exception {
        var err = new StringWriter();
        var command = command(400, "{\"code\":\"UNKNOWN_DISCOUNTER\",\"message\":\"Unbekannter Händler: netto\"}",
                new StringWriter(), err, new AtomicReference<>());

        var exitCode = new CommandLine(command).execute("--region", "hessen", "--id", "netto");

        assertEquals(1, exitCode);
        assertEquals("UNKNOWN_DISCOUNTER: Unbekannter Händler: netto%n".formatted(), err.toString());
    }

    @Test
    void printsUnreachableBackend() {
        var err = new StringWriter();
        var command = new ListCommand("http://localhost:1", new PrintWriter(new StringWriter(), true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute();

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Backend nicht erreichbar"));
    }

    @Test
    void printsHelpForListCommand() {
        var out = new StringWriter();
        var err = new StringWriter();
        var cli = new CommandLine(new DiscounterCli());
        cli.setOut(new PrintWriter(out, true));
        cli.setErr(new PrintWriter(err, true));

        var exitCode = cli.execute("list", "--help");

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("Usage: discounter list"));
        assertTrue(out.toString().contains("--plz"));
        assertTrue(out.toString().contains("--region"));
        assertTrue(out.toString().contains("--format"));
        assertEquals("", err.toString());
    }

    private ListCommand command(int status, String body, AtomicReference<String> requestedQuery) throws IOException {
        return command(status, body, new StringWriter(), new StringWriter(), requestedQuery);
    }

    private ListCommand command(
            int status,
            String body,
            StringWriter out,
            StringWriter err,
            AtomicReference<String> requestedQuery)
            throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/prospects", exchange -> {
            requestedQuery.set(exchange.getRequestURI().getRawQuery());
            respond(exchange, status, body);
        });
        server.start();

        return new ListCommand("http://localhost:" + server.getAddress().getPort(), new PrintWriter(out, true),
                new PrintWriter(err, true));
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        var bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var response = exchange.getResponseBody()) {
            response.write(bytes);
        }
    }

}
