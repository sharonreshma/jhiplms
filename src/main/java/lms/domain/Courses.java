package lms.domain;

import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Courses.
 */
@Table("courses")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Courses implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("course_name")
    private String course_name;

    @Column("start_date")
    private LocalDate start_date;

    @Column("end_date")
    private LocalDate end_date;

    @Transient
    private Student student;

    @Column("student_id")
    private Long studentId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Courses id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourse_name() {
        return this.course_name;
    }

    public Courses course_name(String course_name) {
        this.setCourse_name(course_name);
        return this;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public LocalDate getStart_date() {
        return this.start_date;
    }

    public Courses start_date(LocalDate start_date) {
        this.setStart_date(start_date);
        return this;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public LocalDate getEnd_date() {
        return this.end_date;
    }

    public Courses end_date(LocalDate end_date) {
        this.setEnd_date(end_date);
        return this;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
    }

    public Student getStudent() {
        return this.student;
    }

    public void setStudent(Student student) {
        this.student = student;
        this.studentId = student != null ? student.getId() : null;
    }

    public Courses student(Student student) {
        this.setStudent(student);
        return this;
    }

    public Long getStudentId() {
        return this.studentId;
    }

    public void setStudentId(Long student) {
        this.studentId = student;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Courses)) {
            return false;
        }
        return getId() != null && getId().equals(((Courses) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Courses{" +
            "id=" + getId() +
            ", course_name='" + getCourse_name() + "'" +
            ", start_date='" + getStart_date() + "'" +
            ", end_date='" + getEnd_date() + "'" +
            "}";
    }
}
