export interface Appointment {
  id: number;
  username: string;
  idCard: string;
  department: string;
  date: string;
  time: string;
  doctorName: string;
}

export type AppointmentDraft = Omit<Appointment, 'id'>;
