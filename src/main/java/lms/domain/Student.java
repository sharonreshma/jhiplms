package lms.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Student.
 */
@Table("student")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("reg_no")
    private String reg_no;

    @Column("student_name")
    private String student_name;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Student id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReg_no() {
        return this.reg_no;
    }

    public Student reg_no(String reg_no) {
        this.setReg_no(reg_no);
        return this;
    }

    public void setReg_no(String reg_no) {
        this.reg_no = reg_no;
    }

    public String getStudent_name() {
        return this.student_name;
    }

    public Student student_name(String student_name) {
        this.setStudent_name(student_name);
        return this;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        return getId() != null && getId().equals(((Student) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Student{" +
            "id=" + getId() +
            ", reg_no='" + getReg_no() + "'" +
            ", student_name='" + getStudent_name() + "'" +
            "}";
    }
}
