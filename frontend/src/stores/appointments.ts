import { ref } from 'vue';
import { defineStore } from 'pinia';
import { createAppointment, deleteAppointment, fetchAppointments } from '@/api/appointments';
import type { Appointment, AppointmentDraft } from '@/types/appointment';

function sortAppointments(list: Appointment[]) {
  return [...list].sort((left, right) => {
    const leftKey = `${left.date} ${left.time}`;
    const rightKey = `${right.date} ${right.time}`;
    return rightKey.localeCompare(leftKey);
  });
}

export const useAppointmentsStore = defineStore('appointments', () => {
  const appointments = ref<Appointment[]>([]);
  const loading = ref(false);

  async function loadAppointments() {
    loading.value = true;
    try {
      appointments.value = sortAppointments(await fetchAppointments());
    } finally {
      loading.value = false;
    }
  }

  async function addAppointment(payload: AppointmentDraft) {
    const created = await createAppointment(payload);
    appointments.value = sortAppointments([created, ...appointments.value]);
    return created;
  }

  async function removeAppointment(id: number) {
    await deleteAppointment(id);
    appointments.value = appointments.value.filter((item) => item.id !== id);
  }

  return {
    appointments,
    loading,
    addAppointment,
    loadAppointments,
    removeAppointment,
  };
});
