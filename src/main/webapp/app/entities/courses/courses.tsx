import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat, getSortState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortUp, faSortDown } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, SORT } from 'app/shared/util/pagination.constants';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './courses.reducer';

export const Courses = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const coursesList = useAppSelector(state => state.courses.entities);
  const loading = useAppSelector(state => state.courses.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const order = sortState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    } else {
      return order === ASC ? faSortUp : faSortDown;
    }
  };

  return (
    <div>
      <h2 id="courses-heading" data-cy="CoursesHeading">
        <Translate contentKey="lmsApp.courses.home.title">Courses</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="lmsApp.courses.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/courses/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="lmsApp.courses.home.createLabel">Create new Courses</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {coursesList && coursesList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="lmsApp.courses.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('course_name')}>
                  <Translate contentKey="lmsApp.courses.course_name">Course Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('course_name')} />
                </th>
                <th className="hand" onClick={sort('start_date')}>
                  <Translate contentKey="lmsApp.courses.start_date">Start Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('start_date')} />
                </th>
                <th className="hand" onClick={sort('end_date')}>
                  <Translate contentKey="lmsApp.courses.end_date">End Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('end_date')} />
                </th>
                <th>
                  <Translate contentKey="lmsApp.courses.student">Student</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {coursesList.map((courses, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/courses/${courses.id}`} color="link" size="sm">
                      {courses.id}
                    </Button>
                  </td>
                  <td>{courses.course_name}</td>
                  <td>
                    {courses.start_date ? <TextFormat type="date" value={courses.start_date} format={APP_LOCAL_DATE_FORMAT} /> : null}
                  </td>
                  <td>{courses.end_date ? <TextFormat type="date" value={courses.end_date} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{courses.student ? <Link to={`/student/${courses.student.id}`}>{courses.student.student_name}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/courses/${courses.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`/courses/${courses.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/courses/${courses.id}/delete`)}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="lmsApp.courses.home.notFound">No Courses found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Courses;
