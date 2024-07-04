package lms.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class CoursesAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertCoursesAllPropertiesEquals(Courses expected, Courses actual) {
        assertCoursesAutoGeneratedPropertiesEquals(expected, actual);
        assertCoursesAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertCoursesAllUpdatablePropertiesEquals(Courses expected, Courses actual) {
        assertCoursesUpdatableFieldsEquals(expected, actual);
        assertCoursesUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertCoursesAutoGeneratedPropertiesEquals(Courses expected, Courses actual) {
        assertThat(expected)
            .as("Verify Courses auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertCoursesUpdatableFieldsEquals(Courses expected, Courses actual) {
        assertThat(expected)
            .as("Verify Courses relevant properties")
            .satisfies(e -> assertThat(e.getCourse_name()).as("check course_name").isEqualTo(actual.getCourse_name()))
            .satisfies(e -> assertThat(e.getStart_date()).as("check start_date").isEqualTo(actual.getStart_date()))
            .satisfies(e -> assertThat(e.getEnd_date()).as("check end_date").isEqualTo(actual.getEnd_date()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertCoursesUpdatableRelationshipsEquals(Courses expected, Courses actual) {
        assertThat(expected)
            .as("Verify Courses relationships")
            .satisfies(e -> assertThat(e.getStudent()).as("check student").isEqualTo(actual.getStudent()));
    }
}