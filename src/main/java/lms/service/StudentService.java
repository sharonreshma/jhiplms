package lms.service;

import lms.domain.Student;
import lms.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link lms.domain.Student}.
 */
@Service
@Transactional
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Save a student.
     *
     * @param student the entity to save.
     * @return the persisted entity.
     */
    public Mono<Student> save(Student student) {
        log.debug("Request to save Student : {}", student);
        return studentRepository.save(student);
    }

    /**
     * Update a student.
     *
     * @param student the entity to save.
     * @return the persisted entity.
     */
    public Mono<Student> update(Student student) {
        log.debug("Request to update Student : {}", student);
        return studentRepository.save(student);
    }

    /**
     * Partially update a student.
     *
     * @param student the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Student> partialUpdate(Student student) {
        log.debug("Request to partially update Student : {}", student);

        return studentRepository
            .findById(student.getId())
            .map(existingStudent -> {
                if (student.getReg_no() != null) {
                    existingStudent.setReg_no(student.getReg_no());
                }
                if (student.getStudent_name() != null) {
                    existingStudent.setStudent_name(student.getStudent_name());
                }

                return existingStudent;
            })
            .flatMap(studentRepository::save);
    }

    /**
     * Get all the students.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Student> findAll() {
        log.debug("Request to get all Students");
        return studentRepository.findAll();
    }

    /**
     * Returns the number of students available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return studentRepository.count();
    }

    /**
     * Get one student by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Student> findOne(Long id) {
        log.debug("Request to get Student : {}", id);
        return studentRepository.findById(id);
    }

    /**
     * Delete the student by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Student : {}", id);
        return studentRepository.deleteById(id);
    }
}
