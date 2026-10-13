package com.paycheck.payment;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests: real Spring app, real fraud rules, in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void approvesNormalPaymentAndMasksCardNumber() throws Exception {
        submit("4111111111111111", "2500.00", "IN")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.cardNumber").value("************1111"))
                .andExpect(jsonPath("$.reason").doesNotExist());
    }

    @Test
    void declinesPaymentOverTheAmountLimit() throws Exception {
        submit("4111111111111111", "150000", "IN")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reason").value("Amount is over the limit of 100000"));
    }

    @Test
    void declinesFourthPaymentFromSameCardWithinOneMinute() throws Exception {
        for (int i = 0; i < 3; i++) {
            submit("5555555555554444", "100", "IN").andExpect(jsonPath("$.status").value("APPROVED"));
        }

        submit("5555555555554444", "100", "IN")
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reason").value("More than 3 payments from this card in 1 minute"));
    }

    @Test
    void listsAllPayments() throws Exception {
        submit("4111111111111111", "100", "IN");
        submit("5555555555554444", "200", "KP");

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].status").value("DECLINED"))
                .andExpect(jsonPath("$[1].reason").value("Country KP is blocked"));
    }

    @Test
    void getsOnePaymentById() throws Exception {
        String body = submit("4111111111111111", "999.50", "us").andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/payments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(999.5))
                .andExpect(jsonPath("$.country").value("US"));
    }

    @Test
    void returns404ForUnknownPayment() throws Exception {
        mockMvc.perform(get("/payments/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payment 999999 not found"));
    }

    @Test
    void returns400ForInvalidRequest() throws Exception {
        submit("12ab", "-5", "India")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cardNumber").exists())
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.country").exists());
    }

    private ResultActions submit(String cardNumber, String amount, String country) throws Exception {
        String json = """
                {"cardNumber": "%s", "amount": %s, "country": "%s"}
                """.formatted(cardNumber, amount, country);
        return mockMvc.perform(post("/payments").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
