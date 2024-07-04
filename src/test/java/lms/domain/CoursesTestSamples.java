package lms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CoursesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Courses getCoursesSample1() {
        return new Courses().id(1L).course_name("course_name1");
    }

    public static Courses getCoursesSample2() {
        return new Courses().id(2L).course_name("course_name2");
    }

    public static Courses getCoursesRandomSampleGenerator() {
        return new Courses().id(longCount.incrementAndGet()).course_name(UUID.randomUUID().toString());
    }
}
