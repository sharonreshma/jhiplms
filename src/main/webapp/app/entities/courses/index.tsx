import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Courses from './courses';
import CoursesDetail from './courses-detail';
import CoursesUpdate from './courses-update';
import CoursesDeleteDialog from './courses-delete-dialog';

const CoursesRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Courses />} />
    <Route path="new" element={<CoursesUpdate />} />
    <Route path=":id">
      <Route index element={<CoursesDetail />} />
      <Route path="edit" element={<CoursesUpdate />} />
      <Route path="delete" element={<CoursesDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CoursesRoutes;
