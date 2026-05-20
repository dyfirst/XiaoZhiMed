import { computed, ref, watch } from 'vue';
import { defineStore } from 'pinia';
import { sendCode, login as apiLogin } from '@/api/auth';

export const AUTH_STORAGE_KEY = 'xiaozhi-med-auth';

interface UserInfo {
  userId: number;
  name: string;
  phone: string;
}

interface PersistedAuthState {
  profileName: string;
  token: string;
  userInfo: UserInfo | null;
}

function getDefaultState(): PersistedAuthState {
  return {
    profileName: '前台值班台',
    token: '',
    userInfo: null,
  };
}

function loadPersistedAuthState(): PersistedAuthState {
  if (typeof window === 'undefined') {
    return getDefaultState();
  }

  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
      return getDefaultState();
    }

    const parsed = JSON.parse(raw) as Partial<PersistedAuthState>;
    return {
      profileName: parsed.profileName || '前台值班台',
      token: parsed.token || '',
      userInfo: parsed.userInfo || null,
    };
  } catch {
    return getDefaultState();
  }
}

export function readPersistedToken() {
  return loadPersistedAuthState().token.trim();
}

export const useAuthStore = defineStore('auth', () => {
  const initialState = loadPersistedAuthState();
  const profileName = ref(initialState.profileName);
  const token = ref(initialState.token);
  const userInfo = ref<UserInfo | null>(initialState.userInfo);

  const hasToken = computed(() => token.value.trim().length > 0);
  const userId = computed(() => userInfo.value?.userId ?? null);
  const userName = computed(() => userInfo.value?.name ?? '');

  watch(
    [profileName, token, userInfo],
    () => {
      if (typeof window === 'undefined') {
        return;
      }

      window.localStorage.setItem(
        AUTH_STORAGE_KEY,
        JSON.stringify({
          profileName: profileName.value,
          token: token.value,
          userInfo: userInfo.value,
        }),
      );
    },
    { deep: true },
  );

  async function requestSendCode(phone: string) {
    return sendCode(phone);
  }

  async function login(phone: string, code: string) {
    const data = await apiLogin(phone, code);
    token.value = data.token;
    userInfo.value = {
      userId: data.userId,
      name: data.name,
      phone: data.phone,
    };
    return data;
  }

  function logout() {
    token.value = '';
    userInfo.value = null;
  }

  return {
    profileName,
    token,
    userInfo,
    hasToken,
    userId,
    userName,
    requestSendCode,
    login,
    logout,
  };
});
