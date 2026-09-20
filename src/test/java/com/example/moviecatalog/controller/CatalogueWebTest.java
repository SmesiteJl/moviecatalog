package com.example.moviecatalog.controller;

import com.example.moviecatalog.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Walks the routes a visitor actually hits, including the access rules that separate a guest,
 * a signed-in user and an administrator.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogueWebTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void home_whenAnonymous_thenRendersCatalogueFromTheOfflineSource() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("movies"));
    }

    @Test
    void movies_whenAnonymous_thenRendersListing() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk());
    }

    @Test
    void search_whenAnonymousSearchesForAKnownTitle_thenSucceeds() throws Exception {
        mockMvc.perform(get("/search").param("query", "Начало"))
                .andExpect(status().isOk());
    }

    @Test
    void login_whenAnonymous_thenPageIsReachable() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void offlinePoster_whenAnonymous_thenStaticArtworkIsServed() throws Exception {
        mockMvc.perform(get("/img/posters/placeholder.svg"))
                .andExpect(status().isOk());
    }

    @Test
    void profile_whenAnonymous_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void adminPanel_whenAnonymous_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "viewer", roles = "USER")
    void adminPanel_whenSignedInAsRegularUser_thenIsForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminPanel_whenSignedInAsAdministrator_thenIsAllowed() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }
}
