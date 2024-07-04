package lms.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import lms.domain.Courses;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Courses}, with proper type conversions.
 */
@Service
public class CoursesRowMapper implements BiFunction<Row, String, Courses> {

    private final ColumnConverter converter;

    public CoursesRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Courses} stored in the database.
     */
    @Override
    public Courses apply(Row row, String prefix) {
        Courses entity = new Courses();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setCourse_name(converter.fromRow(row, prefix + "_course_name", String.class));
        entity.setStart_date(converter.fromRow(row, prefix + "_start_date", LocalDate.class));
        entity.setEnd_date(converter.fromRow(row, prefix + "_end_date", LocalDate.class));
        entity.setStudentId(converter.fromRow(row, prefix + "_student_id", Long.class));
        return entity;
    }
}
