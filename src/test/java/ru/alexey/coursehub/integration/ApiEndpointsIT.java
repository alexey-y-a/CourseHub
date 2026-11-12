package ru.alexey.coursehub.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiEndpointsIT {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createsUserThroughApi() throws Exception {
        mockMvc.perform(postJson(
                        "/api/users", "{\"name\":\"Anna\",\"email\":\"anna@example.com\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("anna@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void rejectsInvalidPayloadWithFieldErrors() throws Exception {
        mockMvc.perform(postJson("/api/users", "{\"name\":\"\",\"email\":\"not-an-email\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void returnsProblemDetailForMissingCourse() throws Exception {
        mockMvc.perform(get("/api/courses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Course 999999 not found"));
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String body = "{\"name\":\"Anna\",\"email\":\"dup@example.com\",\"role\":\"STUDENT\"}";
        mockMvc.perform(postJson("/api/users", body)).andExpect(status().isCreated());

        mockMvc.perform(postJson("/api/users", body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void runsFullLearningFlow() throws Exception {
        long categoryId = idOf(postJson("/api/categories", "{\"name\":\"Programming\"}"));
        long teacherId = idOf(
                postJson("/api/users", "{\"name\":\"Ivan\",\"email\":\"teacher@example.com\",\"role\":\"TEACHER\"}"));
        long studentId = idOf(
                postJson("/api/users", "{\"name\":\"Anna\",\"email\":\"student@example.com\",\"role\":\"STUDENT\"}"));
        long courseId = idOf(postJson(
                "/api/courses",
                "{\"title\":\"Hibernate\",\"description\":\"ORM\",\"categoryId\":" + categoryId + ",\"teacherId\":"
                        + teacherId + ",\"duration\":10,\"tags\":[\"Java\"]}"));
        long moduleId = idOf(postJson("/api/courses/" + courseId + "/modules", "{\"title\":\"Intro\"}"));
        long lessonId = idOf(postJson("/api/modules/" + moduleId + "/lessons", "{\"title\":\"What is ORM\"}"));
        long assignmentId = idOf(
                postJson("/api/lessons/" + lessonId + "/assignments", "{\"title\":\"Homework\",\"maxScore\":100}"));

        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments?studentId=" + studentId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        long submissionId = idOf(postJson(
                "/api/assignments/" + assignmentId + "/submissions?studentId=" + studentId,
                "{\"content\":\"my answer\"}"));

        mockMvc.perform(patch("/api/submissions/" + submissionId + "/grade")
                        .contentType(JSON)
                        .content("{\"score\":95,\"feedback\":\"good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(95))
                .andExpect(jsonPath("$.status").value("GRADED"));

        mockMvc.perform(postJson(
                        "/api/courses/" + courseId + "/reviews?studentId=" + studentId,
                        "{\"rating\":5,\"comment\":\"great\"}"))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/courses/" + courseId + "/structure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].title").value("Intro"))
                .andExpect(jsonPath("$.modules[0].lessons[0].title").value("What is ORM"));

        mockMvc.perform(get("/api/users/" + studentId + "/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].score").value(95));

        mockMvc.perform(get("/api/courses/" + courseId + "/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(5.0));

        mockMvc.perform(get("/api/users/" + studentId + "/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private long idOf(MockHttpServletRequestBuilder builder) throws Exception {
        MvcResult result =
                mockMvc.perform(builder).andExpect(status().isCreated()).andReturn();
        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private MockHttpServletRequestBuilder postJson(String url, String body) {
        return post(url).contentType(JSON).content(body);
    }
}
