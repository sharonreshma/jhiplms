export interface IStudent {
  id?: number;
  reg_no?: string | null;
  student_name?: string | null;
}

export const defaultValue: Readonly<IStudent> = {};
