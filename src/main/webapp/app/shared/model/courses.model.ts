import dayjs from 'dayjs';
import { IStudent } from 'app/shared/model/student.model';

export interface ICourses {
  id?: number;
  course_name?: string | null;
  start_date?: dayjs.Dayjs | null;
  end_date?: dayjs.Dayjs | null;
  student?: IStudent | null;
}

export const defaultValue: Readonly<ICourses> = {};
