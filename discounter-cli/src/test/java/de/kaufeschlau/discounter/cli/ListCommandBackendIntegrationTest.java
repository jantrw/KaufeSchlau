package de.kaufeschlau.discounter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaufeschlau.discounter.DiscounterApplication;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import picocli.CommandLine;

@SpringBootTest(classes = DiscounterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListCommandBackendIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void listCommandReadsProspectsFromRealBackend() {
        var out = new StringWriter();
        var err = new StringWriter();
        var command = new ListCommand(
                "http://localhost:" + port,
                new PrintWriter(out, true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute("--ids", "lidl", "--format", "plain");

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("Lidl: https://www.lidl.de/c/online-prospekte/s10005610"));
        assertEquals("", err.toString());
    }

    @Test
    void listCommandPrintsBackendContractError() {
        var out = new StringWriter();
        var err = new StringWriter();
        var command = new ListCommand(
                "http://localhost:" + port,
                new PrintWriter(out, true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("PLZ oder Region erforderlich"));
    }

    @Test
    void listCommandPrintsUnknownDiscounterFromRealBackend() {
        var out = new StringWriter();
        var err = new StringWriter();
        var command = new ListCommand(
                "http://localhost:" + port,
                new PrintWriter(out, true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute("--region", "hessen", "--id", "netto");

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("UNKNOWN_DISCOUNTER: Unbekannter Händler: netto"));
    }

    @Test
    void listCommandPrintsInvalidLocationFromRealBackend() {
        var out = new StringWriter();
        var err = new StringWriter();
        var command = new ListCommand(
                "http://localhost:" + port,
                new PrintWriter(out, true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute("--region", "foo", "--id", "lidl");

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("INVALID_LOCATION: Region ist unbekannt"));
    }

    @Test
    void listCommandPrintsJsonFromRealBackend() throws Exception {
        var out = new StringWriter();
        var err = new StringWriter();
        var command = new ListCommand(
                "http://localhost:" + port,
                new PrintWriter(out, true),
                new PrintWriter(err, true));

        var exitCode = new CommandLine(command).execute("--plz", "65185", "--format", "json");

        var prospects = JSON.readTree(out.toString());
        assertEquals(0, exitCode);
        assertTrue(prospects.isArray());
        assertFalse(prospects.findValuesAsText("id").contains("aldi-nord"));
        assertTrue(prospects.findValuesAsText("id").contains("aldi-sued"));
        assertEquals("", err.toString());
    }
}
