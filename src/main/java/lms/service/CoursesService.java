package lms.service;

import lms.domain.Courses;
import lms.repository.CoursesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link lms.domain.Courses}.
 */
@Service
@Transactional
public class CoursesService {

    private static final Logger log = LoggerFactory.getLogger(CoursesService.class);

    private final CoursesRepository coursesRepository;

    public CoursesService(CoursesRepository coursesRepository) {
        this.coursesRepository = coursesRepository;
    }

    /**
     * Save a courses.
     *
     * @param courses the entity to save.
     * @return the persisted entity.
     */
    public Mono<Courses> save(Courses courses) {
        log.debug("Request to save Courses : {}", courses);
        return coursesRepository.save(courses);
    }

    /**
     * Update a courses.
     *
     * @param courses the entity to save.
     * @return the persisted entity.
     */
    public Mono<Courses> update(Courses courses) {
        log.debug("Request to update Courses : {}", courses);
        return coursesRepository.save(courses);
    }

    /**
     * Partially update a courses.
     *
     * @param courses the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Courses> partialUpdate(Courses courses) {
        log.debug("Request to partially update Courses : {}", courses);

        return coursesRepository
            .findById(courses.getId())
            .map(existingCourses -> {
                if (courses.getCourse_name() != null) {
                    existingCourses.setCourse_name(courses.getCourse_name());
                }
                if (courses.getStart_date() != null) {
                    existingCourses.setStart_date(courses.getStart_date());
                }
                if (courses.getEnd_date() != null) {
                    existingCourses.setEnd_date(courses.getEnd_date());
                }

                return existingCourses;
            })
            .flatMap(coursesRepository::save);
    }

    /**
     * Get all the courses.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Courses> findAll() {
        log.debug("Request to get all Courses");
        return coursesRepository.findAll();
    }

    /**
     * Get all the courses with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<Courses> findAllWithEagerRelationships(Pageable pageable) {
        return coursesRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Returns the number of courses available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return coursesRepository.count();
    }

    /**
     * Get one courses by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Courses> findOne(Long id) {
        log.debug("Request to get Courses : {}", id);
        return coursesRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the courses by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Courses : {}", id);
        return coursesRepository.deleteById(id);
    }
}
