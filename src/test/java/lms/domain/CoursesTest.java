package lms.domain;

import static lms.domain.CoursesTestSamples.*;
import static lms.domain.StudentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import lms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CoursesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Courses.class);
        Courses courses1 = getCoursesSample1();
        Courses courses2 = new Courses();
        assertThat(courses1).isNotEqualTo(courses2);

        courses2.setId(courses1.getId());
        assertThat(courses1).isEqualTo(courses2);

        courses2 = getCoursesSample2();
        assertThat(courses1).isNotEqualTo(courses2);
    }

    @Test
    void studentTest() {
        Courses courses = getCoursesRandomSampleGenerator();
        Student studentBack = getStudentRandomSampleGenerator();

        courses.setStudent(studentBack);
        assertThat(courses.getStudent()).isEqualTo(studentBack);

        courses.student(null);
        assertThat(courses.getStudent()).isNull();
    }
}
