package lms.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import lms.domain.Student;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Student}, with proper type conversions.
 */
@Service
public class StudentRowMapper implements BiFunction<Row, String, Student> {

    private final ColumnConverter converter;

    public StudentRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Student} stored in the database.
     */
    @Override
    public Student apply(Row row, String prefix) {
        Student entity = new Student();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setReg_no(converter.fromRow(row, prefix + "_reg_no", String.class));
        entity.setStudent_name(converter.fromRow(row, prefix + "_student_name", String.class));
        return entity;
    }
}
