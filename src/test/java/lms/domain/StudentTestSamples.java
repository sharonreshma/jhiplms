package lms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class StudentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Student getStudentSample1() {
        return new Student().id(1L).reg_no("reg_no1").student_name("student_name1");
    }

    public static Student getStudentSample2() {
        return new Student().id(2L).reg_no("reg_no2").student_name("student_name2");
    }

    public static Student getStudentRandomSampleGenerator() {
        return new Student()
            .id(longCount.incrementAndGet())
            .reg_no(UUID.randomUUID().toString())
            .student_name(UUID.randomUUID().toString());
    }
}
