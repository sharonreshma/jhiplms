package lms.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import lms.domain.Courses;
import lms.repository.rowmapper.CoursesRowMapper;
import lms.repository.rowmapper.StudentRowMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Courses entity.
 */
@SuppressWarnings("unused")
class CoursesRepositoryInternalImpl extends SimpleR2dbcRepository<Courses, Long> implements CoursesRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final StudentRowMapper studentMapper;
    private final CoursesRowMapper coursesMapper;

    private static final Table entityTable = Table.aliased("courses", EntityManager.ENTITY_ALIAS);
    private static final Table studentTable = Table.aliased("student", "student");

    public CoursesRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        StudentRowMapper studentMapper,
        CoursesRowMapper coursesMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Courses.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.studentMapper = studentMapper;
        this.coursesMapper = coursesMapper;
    }

    @Override
    public Flux<Courses> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Courses> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = CoursesSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(StudentSqlHelper.getColumns(studentTable, "student"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(studentTable)
            .on(Column.create("student_id", entityTable))
            .equals(Column.create("id", studentTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Courses.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Courses> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Courses> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Courses> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Courses> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Courses> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Courses process(Row row, RowMetadata metadata) {
        Courses entity = coursesMapper.apply(row, "e");
        entity.setStudent(studentMapper.apply(row, "student"));
        return entity;
    }

    @Override
    public <S extends Courses> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
