package com.alai.entitlements.web;

import com.alai.entitlements.model.AppUser;
import com.alai.entitlements.repository.UserEntitlementRepository;
import com.alai.entitlements.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fast, DB-free tests for the controller layer. The repository behavior itself
 * (the actual join/left-join logic) is covered separately in
 * UserEntitlementRepositoryTest against a real Postgres - here we're only
 * checking that the controller wires requests/responses correctly.
 */
@WebMvcTest(AccessController.class)
class AccessControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    UserEntitlementRepository userEntitlementRepository;

    @Test
    void listUsersReturnsThemSortedByFullName() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(
                userFixture(2, "Bob Smith"),
                userFixture(1, "Aisha Bello")
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Aisha Bello"))
                .andExpect(jsonPath("$[1].fullName").value("Bob Smith"));
    }

    @Test
    void userAccessDelegatesToRepositoryAndSerializesFields() throws Exception {
        AccessItemDto item = new AccessItemDto(
                1, "SAP_FI_GL_APRV_L3", "role", "SAP ERP Financials",
                LocalDate.of(2025, 1, 15),
                "Allows approving general ledger entries.", null);

        when(userEntitlementRepository.findAccessForUser(1)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/users/1/access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].crypticTitle").value("SAP_FI_GL_APRV_L3"))
                .andExpect(jsonPath("$[0].applicationName").value("SAP ERP Financials"))
                .andExpect(jsonPath("$[0].description").value("Allows approving general ledger entries."))
                .andExpect(jsonPath("$[0].riskNote").value(nullValue()));
    }

    @Test
    void userAccessReturnsEmptyArrayWhenUserHasNoGrants() throws Exception {
        when(userEntitlementRepository.findAccessForUser(999)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/999/access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * AppUser's fields are only ever populated by JPA/Hibernate in production,
     * so there are no public setters - ReflectionTestUtils fills them for fixtures.
     */
    private AppUser userFixture(int id, String fullName) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "userId", id);
        ReflectionTestUtils.setField(user, "fullName", fullName);
        ReflectionTestUtils.setField(user, "employeeId", "E1000" + id);
        return user;
    }
}
