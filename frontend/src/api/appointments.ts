import { request } from '@/api/request';
import type { Appointment, AppointmentDraft } from '@/types/appointment';

export function fetchAppointments() {
  return request<Appointment[]>({
    url: '/appointments',
    method: 'GET',
  });
}

export function createAppointment(payload: AppointmentDraft) {
  return request<Appointment>({
    url: '/appointments',
    method: 'POST',
    data: payload,
  });
}

export function deleteAppointment(id: number) {
  return request<boolean>({
    url: `/appointments/${id}`,
    method: 'DELETE',
    responseType: 'json',
  });
}
