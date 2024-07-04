import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './courses.reducer';

export const CoursesDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const coursesEntity = useAppSelector(state => state.courses.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="coursesDetailsHeading">
          <Translate contentKey="lmsApp.courses.detail.title">Courses</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{coursesEntity.id}</dd>
          <dt>
            <span id="course_name">
              <Translate contentKey="lmsApp.courses.course_name">Course Name</Translate>
            </span>
          </dt>
          <dd>{coursesEntity.course_name}</dd>
          <dt>
            <span id="start_date">
              <Translate contentKey="lmsApp.courses.start_date">Start Date</Translate>
            </span>
          </dt>
          <dd>
            {coursesEntity.start_date ? <TextFormat value={coursesEntity.start_date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="end_date">
              <Translate contentKey="lmsApp.courses.end_date">End Date</Translate>
            </span>
          </dt>
          <dd>
            {coursesEntity.end_date ? <TextFormat value={coursesEntity.end_date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="lmsApp.courses.student">Student</Translate>
          </dt>
          <dd>{coursesEntity.student ? coursesEntity.student.student_name : ''}</dd>
        </dl>
        <Button tag={Link} to="/courses" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/courses/${coursesEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CoursesDetail;
