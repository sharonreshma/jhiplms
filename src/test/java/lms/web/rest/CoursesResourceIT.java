package lms.web.rest;

import static lms.domain.CoursesAsserts.*;
import static lms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import lms.IntegrationTest;
import lms.domain.Courses;
import lms.repository.CoursesRepository;
import lms.repository.EntityManager;
import lms.service.CoursesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link CoursesResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CoursesResourceIT {

    private static final String DEFAULT_COURSE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COURSE_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/courses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CoursesRepository coursesRepository;

    @Mock
    private CoursesRepository coursesRepositoryMock;

    @Mock
    private CoursesService coursesServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Courses courses;

    private Courses insertedCourses;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Courses createEntity(EntityManager em) {
        Courses courses = new Courses().course_name(DEFAULT_COURSE_NAME).start_date(DEFAULT_START_DATE).end_date(DEFAULT_END_DATE);
        return courses;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Courses createUpdatedEntity(EntityManager em) {
        Courses courses = new Courses().course_name(UPDATED_COURSE_NAME).start_date(UPDATED_START_DATE).end_date(UPDATED_END_DATE);
        return courses;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Courses.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        courses = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedCourses != null) {
            coursesRepository.delete(insertedCourses).block();
            insertedCourses = null;
        }
        deleteEntities(em);
    }

    @Test
    void createCourses() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Courses
        var returnedCourses = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Courses.class)
            .returnResult()
            .getResponseBody();

        // Validate the Courses in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCoursesUpdatableFieldsEquals(returnedCourses, getPersistedCourses(returnedCourses));

        insertedCourses = returnedCourses;
    }

    @Test
    void createCoursesWithExistingId() throws Exception {
        // Create the Courses with an existing ID
        courses.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllCoursesAsStream() {
        // Initialize the database
        coursesRepository.save(courses).block();

        List<Courses> coursesList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Courses.class)
            .getResponseBody()
            .filter(courses::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(coursesList).isNotNull();
        assertThat(coursesList).hasSize(1);
        Courses testCourses = coursesList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertCoursesAllPropertiesEquals(courses, testCourses);
        assertCoursesUpdatableFieldsEquals(courses, testCourses);
    }

    @Test
    void getAllCourses() {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        // Get all the coursesList
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
            .value(hasItem(courses.getId().intValue()))
            .jsonPath("$.[*].course_name")
            .value(hasItem(DEFAULT_COURSE_NAME))
            .jsonPath("$.[*].start_date")
            .value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].end_date")
            .value(hasItem(DEFAULT_END_DATE.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCoursesWithEagerRelationshipsIsEnabled() {
        when(coursesServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(coursesServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCoursesWithEagerRelationshipsIsNotEnabled() {
        when(coursesServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(coursesRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getCourses() {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        // Get the courses
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, courses.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(courses.getId().intValue()))
            .jsonPath("$.course_name")
            .value(is(DEFAULT_COURSE_NAME))
            .jsonPath("$.start_date")
            .value(is(DEFAULT_START_DATE.toString()))
            .jsonPath("$.end_date")
            .value(is(DEFAULT_END_DATE.toString()));
    }

    @Test
    void getNonExistingCourses() {
        // Get the courses
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingCourses() throws Exception {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the courses
        Courses updatedCourses = coursesRepository.findById(courses.getId()).block();
        updatedCourses.course_name(UPDATED_COURSE_NAME).start_date(UPDATED_START_DATE).end_date(UPDATED_END_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedCourses.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedCourses))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCoursesToMatchAllProperties(updatedCourses);
    }

    @Test
    void putNonExistingCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, courses.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCoursesWithPatch() throws Exception {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the courses using partial update
        Courses partialUpdatedCourses = new Courses();
        partialUpdatedCourses.setId(courses.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCourses.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedCourses))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Courses in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCoursesUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCourses, courses), getPersistedCourses(courses));
    }

    @Test
    void fullUpdateCoursesWithPatch() throws Exception {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the courses using partial update
        Courses partialUpdatedCourses = new Courses();
        partialUpdatedCourses.setId(courses.getId());

        partialUpdatedCourses.course_name(UPDATED_COURSE_NAME).start_date(UPDATED_START_DATE).end_date(UPDATED_END_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCourses.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedCourses))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Courses in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCoursesUpdatableFieldsEquals(partialUpdatedCourses, getPersistedCourses(partialUpdatedCourses));
    }

    @Test
    void patchNonExistingCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, courses.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCourses() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        courses.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(courses))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Courses in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCourses() {
        // Initialize the database
        insertedCourses = coursesRepository.save(courses).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the courses
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, courses.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return coursesRepository.count().block();
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

    protected Courses getPersistedCourses(Courses courses) {
        return coursesRepository.findById(courses.getId()).block();
    }

    protected void assertPersistedCoursesToMatchAllProperties(Courses expectedCourses) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCoursesAllPropertiesEquals(expectedCourses, getPersistedCourses(expectedCourses));
        assertCoursesUpdatableFieldsEquals(expectedCourses, getPersistedCourses(expectedCourses));
    }

    protected void assertPersistedCoursesToMatchUpdatableProperties(Courses expectedCourses) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCoursesAllUpdatablePropertiesEquals(expectedCourses, getPersistedCourses(expectedCourses));
        assertCoursesUpdatableFieldsEquals(expectedCourses, getPersistedCourses(expectedCourses));
    }
}
