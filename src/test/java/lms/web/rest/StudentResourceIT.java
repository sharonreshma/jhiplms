package lms.web.rest;

import static lms.domain.StudentAsserts.*;
import static lms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import lms.IntegrationTest;
import lms.domain.Student;
import lms.repository.EntityManager;
import lms.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link StudentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class StudentResourceIT {

    private static final String DEFAULT_REG_NO = "AAAAAAAAAA";
    private static final String UPDATED_REG_NO = "BBBBBBBBBB";

    private static final String DEFAULT_STUDENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_STUDENT_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/students";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Student student;

    private Student insertedStudent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Student createEntity(EntityManager em) {
        Student student = new Student().reg_no(DEFAULT_REG_NO).student_name(DEFAULT_STUDENT_NAME);
        return student;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Student createUpdatedEntity(EntityManager em) {
        Student student = new Student().reg_no(UPDATED_REG_NO).student_name(UPDATED_STUDENT_NAME);
        return student;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Student.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        student = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedStudent != null) {
            studentRepository.delete(insertedStudent).block();
            insertedStudent = null;
        }
        deleteEntities(em);
    }

    @Test
    void createStudent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Student
        var returnedStudent = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Student.class)
            .returnResult()
            .getResponseBody();

        // Validate the Student in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertStudentUpdatableFieldsEquals(returnedStudent, getPersistedStudent(returnedStudent));

        insertedStudent = returnedStudent;
    }

    @Test
    void createStudentWithExistingId() throws Exception {
        // Create the Student with an existing ID
        student.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllStudentsAsStream() {
        // Initialize the database
        studentRepository.save(student).block();

        List<Student> studentList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Student.class)
            .getResponseBody()
            .filter(student::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(studentList).isNotNull();
        assertThat(studentList).hasSize(1);
        Student testStudent = studentList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertStudentAllPropertiesEquals(student, testStudent);
        assertStudentUpdatableFieldsEquals(student, testStudent);
    }

    @Test
    void getAllStudents() {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        // Get all the studentList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(student.getId().intValue()))
            .jsonPath("$.[*].reg_no")
            .value(hasItem(DEFAULT_REG_NO))
            .jsonPath("$.[*].student_name")
            .value(hasItem(DEFAULT_STUDENT_NAME));
    }

    @Test
    void getStudent() {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        // Get the student
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, student.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(student.getId().intValue()))
            .jsonPath("$.reg_no")
            .value(is(DEFAULT_REG_NO))
            .jsonPath("$.student_name")
            .value(is(DEFAULT_STUDENT_NAME));
    }

    @Test
    void getNonExistingStudent() {
        // Get the student
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingStudent() throws Exception {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the student
        Student updatedStudent = studentRepository.findById(student.getId()).block();
        updatedStudent.reg_no(UPDATED_REG_NO).student_name(UPDATED_STUDENT_NAME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedStudent.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedStudent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedStudentToMatchAllProperties(updatedStudent);
    }

    @Test
    void putNonExistingStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateStudentWithPatch() throws Exception {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the student using partial update
        Student partialUpdatedStudent = new Student();
        partialUpdatedStudent.setId(student.getId());

        partialUpdatedStudent.reg_no(UPDATED_REG_NO).student_name(UPDATED_STUDENT_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStudent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedStudent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Student in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStudentUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedStudent, student), getPersistedStudent(student));
    }

    @Test
    void fullUpdateStudentWithPatch() throws Exception {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the student using partial update
        Student partialUpdatedStudent = new Student();
        partialUpdatedStudent.setId(student.getId());

        partialUpdatedStudent.reg_no(UPDATED_REG_NO).student_name(UPDATED_STUDENT_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStudent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedStudent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Student in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStudentUpdatableFieldsEquals(partialUpdatedStudent, getPersistedStudent(partialUpdatedStudent));
    }

    @Test
    void patchNonExistingStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, student.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamStudent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        student.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(student))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Student in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteStudent() {
        // Initialize the database
        insertedStudent = studentRepository.save(student).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the student
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, student.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return studentRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Student getPersistedStudent(Student student) {
        return studentRepository.findById(student.getId()).block();
    }

    protected void assertPersistedStudentToMatchAllProperties(Student expectedStudent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertStudentAllPropertiesEquals(expectedStudent, getPersistedStudent(expectedStudent));
        assertStudentUpdatableFieldsEquals(expectedStudent, getPersistedStudent(expectedStudent));
    }

    protected void assertPersistedStudentToMatchUpdatableProperties(Student expectedStudent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertStudentAllUpdatablePropertiesEquals(expectedStudent, getPersistedStudent(expectedStudent));
        assertStudentUpdatableFieldsEquals(expectedStudent, getPersistedStudent(expectedStudent));
    }
}
