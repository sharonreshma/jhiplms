package lms.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import lms.domain.Courses;
import lms.repository.CoursesRepository;
import lms.service.CoursesService;
import lms.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link lms.domain.Courses}.
 */
@RestController
@RequestMapping("/api/courses")
public class CoursesResource {

    private static final Logger log = LoggerFactory.getLogger(CoursesResource.class);

    private static final String ENTITY_NAME = "courses";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CoursesService coursesService;

    private final CoursesRepository coursesRepository;

    public CoursesResource(CoursesService coursesService, CoursesRepository coursesRepository) {
        this.coursesService = coursesService;
        this.coursesRepository = coursesRepository;
    }

    /**
     * {@code POST  /courses} : Create a new courses.
     *
     * @param courses the courses to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new courses, or with status {@code 400 (Bad Request)} if the courses has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Courses>> createCourses(@RequestBody Courses courses) throws URISyntaxException {
        log.debug("REST request to save Courses : {}", courses);
        if (courses.getId() != null) {
            throw new BadRequestAlertException("A new courses cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return coursesService
            .save(courses)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/courses/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /courses/:id} : Updates an existing courses.
     *
     * @param id the id of the courses to save.
     * @param courses the courses to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated courses,
     * or with status {@code 400 (Bad Request)} if the courses is not valid,
     * or with status {@code 500 (Internal Server Error)} if the courses couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Courses>> updateCourses(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Courses courses
    ) throws URISyntaxException {
        log.debug("REST request to update Courses : {}, {}", id, courses);
        if (courses.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, courses.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return coursesRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return coursesService
                    .update(courses)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        result ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                                .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /courses/:id} : Partial updates given fields of an existing courses, field will ignore if it is null
     *
     * @param id the id of the courses to save.
     * @param courses the courses to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated courses,
     * or with status {@code 400 (Bad Request)} if the courses is not valid,
     * or with status {@code 404 (Not Found)} if the courses is not found,
     * or with status {@code 500 (Internal Server Error)} if the courses couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Courses>> partialUpdateCourses(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Courses courses
    ) throws URISyntaxException {
        log.debug("REST request to partial update Courses partially : {}, {}", id, courses);
        if (courses.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, courses.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return coursesRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Courses> result = coursesService.partialUpdate(courses);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        res ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                                .body(res)
                    );
            });
    }

    /**
     * {@code GET  /courses} : get all the courses.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of courses in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Courses>> getAllCourses(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Courses");
        return coursesService.findAll().collectList();
    }

    /**
     * {@code GET  /courses} : get all the courses as a stream.
     * @return the {@link Flux} of courses.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Courses> getAllCoursesAsStream() {
        log.debug("REST request to get all Courses as a stream");
        return coursesService.findAll();
    }

    /**
     * {@code GET  /courses/:id} : get the "id" courses.
     *
     * @param id the id of the courses to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the courses, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Courses>> getCourses(@PathVariable("id") Long id) {
        log.debug("REST request to get Courses : {}", id);
        Mono<Courses> courses = coursesService.findOne(id);
        return ResponseUtil.wrapOrNotFound(courses);
    }

    /**
     * {@code DELETE  /courses/:id} : delete the "id" courses.
     *
     * @param id the id of the courses to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCourses(@PathVariable("id") Long id) {
        log.debug("REST request to delete Courses : {}", id);
        return coursesService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
