package de.kaufeschlau.discounter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
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
        var command = command(200, "[{\"id\":\"lidl\",\"name\":\"Lidl\",\"prospectUrl\":\"https://example.test\"}]",
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
        var command = command(200, "[{\"name\":\"Lidl\",\"prospectUrl\":\"https://example.test/lidl\"}]", out,
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
    void printsUnreachableBackendForDnsFailure() {
        var err = new StringWriter();
        var command = unreachableCommand(new UnknownHostException("backend.invalid"), err);

        var exitCode = new CommandLine(command).execute();

        assertEquals(1, exitCode);
        assertEquals("Backend nicht erreichbar: http://backend.invalid%n".formatted(), err.toString());
    }

    @Test
    void printsUnreachableBackendForConnectTimeout() {
        var err = new StringWriter();
        var command = unreachableCommand(new HttpConnectTimeoutException("timeout"), err);

        var exitCode = new CommandLine(command).execute();

        assertEquals(1, exitCode);
        assertEquals("Backend nicht erreichbar: http://backend.invalid%n".formatted(), err.toString());
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

    private static ListCommand unreachableCommand(IOException failure, StringWriter err) {
        return new ListCommand(new ThrowingHttpClient(failure), "http://backend.invalid",
                new PrintWriter(new StringWriter(), true), new PrintWriter(err, true));
    }

    private static class ThrowingHttpClient extends HttpClient {

        private final IOException failure;

        ThrowingHttpClient(IOException failure) {
            this.failure = failure;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException {
            throw failure;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
    }
}
