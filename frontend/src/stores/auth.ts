import { computed, ref, watch } from 'vue';
import { defineStore } from 'pinia';

export const AUTH_STORAGE_KEY = 'xiaozhi-med-auth';

interface PersistedAuthState {
  profileName: string;
  token: string;
}

function getDefaultState(): PersistedAuthState {
  return {
    profileName: '前台值班台',
    token: '',
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

  const hasToken = computed(() => token.value.trim().length > 0);

  watch(
    [profileName, token],
    () => {
      if (typeof window === 'undefined') {
        return;
      }

      window.localStorage.setItem(
        AUTH_STORAGE_KEY,
        JSON.stringify({
          profileName: profileName.value,
          token: token.value,
        }),
      );
    },
    { deep: true },
  );

  return {
    profileName,
    token,
    hasToken,
  };
});
