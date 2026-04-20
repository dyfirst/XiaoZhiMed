<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useAppointmentsStore } from '@/stores/appointments';
import type { AppointmentDraft } from '@/types/appointment';

const appointmentsStore = useAppointmentsStore();
const dialogVisible = ref(false);
const formRef = ref<FormInstance>();

const form = reactive<AppointmentDraft>({
  username: '',
  idCard: '',
  department: '',
  date: '',
  time: '',
  doctorName: '',
});

const rules: FormRules<AppointmentDraft> = {
  username: [{ required: true, message: '请输入就诊人姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  department: [{ required: true, message: '请输入科室', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  time: [{ required: true, message: '请选择时间', trigger: 'change' }],
  doctorName: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
};

const totalCount = computed(() => appointmentsStore.appointments.length);

function resetForm() {
  form.username = '';
  form.idCard = '';
  form.department = '';
  form.date = '';
  form.time = '';
  form.doctorName = '';
}

async function submitAppointment() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  await appointmentsStore.addAppointment({ ...form });
  ElMessage.success('预约已创建');
  dialogVisible.value = false;
  resetForm();
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除这条预约记录吗？', '删除提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await appointmentsStore.removeAppointment(id);
    ElMessage.success('预约已删除');
  } catch {
    // 用户取消时不需要额外提示。
  }
}

onMounted(() => {
  appointmentsStore.loadAppointments().catch(() => {
    ElMessage.error('预约列表加载失败，请确认后端已启动');
  });
});
</script>

<template>
  <div class="appointments-page">
    <el-card class="summary-card">
      <div class="summary-head">
        <div>
          <p class="section-eyebrow">Appointment Desk</p>
          <h2 class="section-title">预约管理台</h2>
          <p class="section-copy">
            直接对接后端 `/appointments` 接口，支持列表查看、创建和删除。
          </p>
        </div>
        <div class="summary-actions">
          <el-statistic title="当前预约数" :value="totalCount" />
          <el-button type="primary" round @click="dialogVisible = true">新增预约</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="appointmentsStore.loading"
        :data="appointmentsStore.appointments"
        stripe
        empty-text="暂无预约记录"
      >
        <el-table-column prop="username" label="就诊人" min-width="120" />
        <el-table-column prop="idCard" label="身份证号" min-width="180" />
        <el-table-column prop="department" label="科室" min-width="120" />
        <el-table-column prop="doctorName" label="医生" min-width="120" />
        <el-table-column prop="date" label="日期" min-width="120" />
        <el-table-column prop="time" label="时段" min-width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="新增预约"
      width="min(92vw, 640px)"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="就诊人" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" />
          </el-form-item>
          <el-form-item label="科室" prop="department">
            <el-input v-model="form.department" placeholder="例如：神经内科" />
          </el-form-item>
          <el-form-item label="医生" prop="doctorName">
            <el-input v-model="form.doctorName" />
          </el-form-item>
          <el-form-item label="日期" prop="date">
            <el-date-picker
              v-model="form.date"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
            />
          </el-form-item>
          <el-form-item label="时段" prop="time">
            <el-time-select
              v-model="form.time"
              start="08:00"
              end="18:00"
              step="00:30"
              placeholder="选择时间"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.appointments-page {
  display: grid;
  gap: 18px;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
}

.summary-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 16px;
}

@media (max-width: 900px) {
  .summary-head,
  .summary-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
