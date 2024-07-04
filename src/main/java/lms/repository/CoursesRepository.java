package lms.repository;

import lms.domain.Courses;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Courses entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CoursesRepository extends ReactiveCrudRepository<Courses, Long>, CoursesRepositoryInternal {
    @Override
    Mono<Courses> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Courses> findAllWithEagerRelationships();

    @Override
    Flux<Courses> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM courses entity WHERE entity.student_id = :id")
    Flux<Courses> findByStudent(Long id);

    @Query("SELECT * FROM courses entity WHERE entity.student_id IS NULL")
    Flux<Courses> findAllWhereStudentIsNull();

    @Override
    <S extends Courses> Mono<S> save(S entity);

    @Override
    Flux<Courses> findAll();

    @Override
    Mono<Courses> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface CoursesRepositoryInternal {
    <S extends Courses> Mono<S> save(S entity);

    Flux<Courses> findAllBy(Pageable pageable);

    Flux<Courses> findAll();

    Mono<Courses> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Courses> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Courses> findOneWithEagerRelationships(Long id);

    Flux<Courses> findAllWithEagerRelationships();

    Flux<Courses> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
