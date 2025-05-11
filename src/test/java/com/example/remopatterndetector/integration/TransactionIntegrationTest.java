package com.example.remopatterndetector.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
public class TransactionIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("remo_db")
            .withUsername("root")
            .withPassword("password");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Test
    void shouldLogTransactionSuccessfully() throws Exception {
        String json = """
                {
                  "userId": "john123",
                  "amount": 12000,
                  "timestamp": "%s",
                  "type": "DEPOSIT"
                }
                """.formatted(now());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFetchSuspiciousTransactions() throws Exception {
        String userId = "john123";

        mockMvc.perform(get("/api/users/" + userId + "/suspicious"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFlagHighValueTransactionAsSuspicious() throws Exception {
        String userId = "richguy";

        String json = """
        {
          "userId": "%s",
          "amount": 20000,
          "timestamp": "%s",
          "type": "DEPOSIT"
        }
    """.formatted(userId, now());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/" + userId + "/suspicious"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFlagFrequentSmallTransactions() throws Exception {
        String userId = "smallspender";

        for (int i = 0; i < 6; i++) {
            String json = """
            {
              "userId": "%s",
              "amount": 50,
              "timestamp": "%s",
              "type": "WITHDRAWAL"
            }
        """.formatted(userId, now());

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/users/" + userId + "/suspicious"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
