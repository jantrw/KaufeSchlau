package de.kaufeschlau.discounter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ProspectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsAllProspectsWithoutLocation() throws Exception {
        mockMvc.perform(get("/api/v1/prospects"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("PLZ oder Region ist erforderlich für: aldi-nord, aldi-sued, netto-marken-discount, rewe, edeka"));
    }

    @Test
    void allowsLocationFreeRetailerFilterWithoutLocation() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("retailerIds", "lidl,penny,kaufland"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].id").value("lidl"))
                .andExpect(jsonPath("$.items[0].url").value("https://www.lidl.de/c/online-prospekte/s10005610"))
                .andExpect(jsonPath("$.items[0].resolutionMode").value("STATIC_ENTRYPOINT"))
                .andExpect(jsonPath("$.items[0].requiresLocationContext").value(false));
    }

    @Test
    void rejectsLocationRequiredRetailerFilterWithoutLocation() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("retailerIds", "rewe,edeka"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("PLZ oder Region ist erforderlich für: rewe, edeka"));
    }

    @Test
    void rejectsInvalidPlzForLocationRequiredRetailerFilter() throws Exception {
        mockMvc.perform(get("/api/v1/prospects")
                        .queryParam("retailerIds", "rewe")
                        .queryParam("plz", "abcde"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("PLZ muss fünfstellig numerisch sein."));
    }

    @Test
    void rejectsInvalidPlzForLocationFreeRetailerFilter() throws Exception {
        mockMvc.perform(get("/api/v1/prospects")
                        .queryParam("retailerIds", "lidl")
                        .queryParam("plz", "abcde"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("PLZ muss fünfstellig numerisch sein."));
    }

    @Test
    void rejectsUnknownRegionForSingleLocationRequiredRetailer() throws Exception {
        mockMvc.perform(get("/api/v1/prospects/rewe").queryParam("region", "atlantis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Region ist unbekannt: atlantis"));
    }

    @Test
    void rejectsUnknownRegionForSingleLocationFreeRetailer() throws Exception {
        mockMvc.perform(get("/api/v1/prospects/lidl").queryParam("region", "atlantis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Region ist unbekannt: atlantis"));
    }

    @Test
    void rejectsAldiRegionForPlzBasedRetailer() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("retailerIds", "rewe").queryParam("region", "aldi-sued"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Region ist unbekannt: aldi-sued"));
    }

    @Test
    void returnsFallbackHintsForLocationResolvedRetailersWithLocation() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("retailerIds", "rewe").queryParam("plz", "65185"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value("rewe"))
                .andExpect(jsonPath("$.items[0].url").value("https://www.rewe.de/angebote/nationale-angebote/"))
                .andExpect(jsonPath("$.items[0].resolutionMode").value("LOCATION_RESOLVED"))
                .andExpect(jsonPath("$.items[0].requiresLocationContext").value(true))
                .andExpect(jsonPath("$.items[0].fallbackHint").value("Phase 1 nutzt den offiziellen Einstiegspunkt. Filialgenaue Auflösung folgt später."))
                .andExpect(jsonPath("$.items[0].resolverHint").value("PLZ -> Markt -> /angebote/{ort}/{marketId}/{marketSlug}/"));
    }

    @Test
    void returnsSingleProspectForExplicitAldiWithoutLocation() throws Exception {
        mockMvc.perform(get("/api/v1/prospects/aldi-sued"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("aldi-sued"))
                .andExpect(jsonPath("$.url").value("https://www.aldi-sued.de/prospekte"))
                .andExpect(jsonPath("$.requiresLocationContext").value(false));
    }

    @Test
    void returnsSingleLocationRequiredProspectWithRegion() throws Exception {
        mockMvc.perform(get("/api/v1/prospects/edeka").queryParam("region", "hessen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("edeka"))
                .andExpect(jsonPath("$.url").value("https://www.edeka.de/angebote/"))
                .andExpect(jsonPath("$.resolutionMode").value("LOCATION_RESOLVED"))
                .andExpect(jsonPath("$.requiresLocationContext").value(true))
                .andExpect(jsonPath("$.fallbackHint").value("Phase 1 nutzt den offiziellen Einstiegspunkt. Filialgenaue Auflösung folgt später."))
                .andExpect(jsonPath("$.resolverHint").value("PLZ -> Markt-ID -> /markt-id/{marketId}/prospekt.jsp"))
                .andExpect(jsonPath("$.marketSearchUrl").value("https://www.edeka.de/marktsuche.jsp"))
                .andExpect(jsonPath("$.officialUrl").value(true));
    }

    @Test
    void rejectsUnknownSingleRetailer() throws Exception {
        mockMvc.perform(get("/api/v1/prospects/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RETAILER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Unbekannter Händler: unknown"));
    }

    @Test
    void rejectsUnknownRetailerInFilter() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("retailerIds", "unknown").queryParam("plz", "65185"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RETAILER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Unbekannter Händler: unknown"));
    }

    @Test
    void resolvesAutomaticAldiSelectionWithPlz() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/prospects").queryParam("plz", "65185"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(7)))
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"id\":\"aldi-sued\"");
        assertThat(body).doesNotContain("\"id\":\"aldi-nord\"");
    }

    @Test
    void resolvesAutomaticAldiSelectionWithRegion() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/prospects").queryParam("region", "hessen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(7)))
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"id\":\"aldi-sued\"");
        assertThat(body).doesNotContain("\"id\":\"aldi-nord\"");
    }

    @Test
    void rejectsUnknownRegionForAutomaticAldiSelection() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("region", "atlantis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Region ist unbekannt: atlantis"));
    }

    @Test
    void rejectsAldiRegionForAutomaticSelectionWithPlzBasedRetailers() throws Exception {
        mockMvc.perform(get("/api/v1/prospects").queryParam("region", "aldi-sued"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Region ist unbekannt: aldi-sued"));
    }
}
