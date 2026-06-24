package de.kaufeschlau.discounter.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProspectControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void rejectsAllProspectsWithoutLocation() throws Exception {
        mvc.perform(get("/api/v1/prospects"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_REQUIRED"))
                .andExpect(jsonPath("$.message", containsString("PLZ oder Region erforderlich")));
    }

    @Test
    void returnsLocationFreeSelectionWithoutLocation() throws Exception {
        mvc.perform(get("/api/v1/prospects").param("retailerIds", "lidl,penny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("lidl"))
                .andExpect(jsonPath("$[0].prospectUrl").value("https://www.lidl.de/c/online-prospekte/s10005610"))
                .andExpect(jsonPath("$[1].id").value("penny"));
    }

    @Test
    void filtersAutomaticAldiRegionByPlz() throws Exception {
        mvc.perform(get("/api/v1/prospects").param("plz", "65185"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("aldi-sued")))
                .andExpect(jsonPath("$[*].id", not(hasItem("aldi-nord"))));
    }

    @Test
    void filtersAutomaticAldiRegionByRegion() throws Exception {
        mvc.perform(get("/api/v1/prospects").param("region", "hessen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("aldi-sued")))
                .andExpect(jsonPath("$[*].id", not(hasItem("aldi-nord"))));
    }

    @Test
    void rejectsUnknownDiscounter() throws Exception {
        mvc.perform(get("/api/v1/prospects").param("retailerIds", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_DISCOUNTER"))
                .andExpect(jsonPath("$.message", containsString("Unbekannter Händler: unknown")));
    }
}
