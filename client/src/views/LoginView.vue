<template>
  <div class="flex justify-center items-center bg-gray-100 min-h-screen">
    <div class="bg-white p-8 rounded shadow-lg w-[25rem]">
      <h2 class="text-2xl mb-4">Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="email" class="block text-gray-700 text-sm font-bold mb-2">Email</label>
          <input v-model="email" type="email" id="email"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-6">
          <label for="password" class="block text-gray-700 text-sm font-bold mb-2">Password</label>
          <input v-model="password" type="password" id="password"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <button type="submit"
          class="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none">Login</button>
      </form>
      <div class="flex justify-center items-center mt-4">
        <h4 class="mr-1">New customer? </h4>
        <StyledRouterLink text="Start here" path="/signup" textClass="text-blue-700 hover:text-blue-600 font-semibold" />
      </div>

      <!-- Error message -->
      <div v-if="loginErrorResponse" class="flex justify-center font-semibold text-red-700 mt-2">
        <p>{{ loginErrorResponse.message }}</p>
      </div>

      <!-- Diagnostics panel - always visible on error -->
      <div v-if="diagnostics" class="mt-3 p-3 bg-gray-50 rounded border border-gray-200 text-xs font-mono">
        <div class="font-sans font-bold text-sm text-gray-700 mb-2">Login Diagnostics</div>

        <div class="space-y-1.5">
          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Error Type:</span>
            <span :class="diagnostics.errorType === 'network' ? 'text-orange-600' : 'text-red-600'" class="font-bold">
              {{ diagnostics.errorType === 'network' ? 'NETWORK ERROR' : 'BACKEND ERROR' }}
            </span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">HTTP Status:</span>
            <span class="font-bold" :class="httpStatusColor">{{ diagnostics.httpStatus || 'N/A' }}</span>
            <span class="text-gray-400 ml-1">({{ httpStatusText }})</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Request URL:</span>
            <span class="text-gray-800 break-all">{{ diagnostics.requestUrl }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Method:</span>
            <span class="text-gray-800">{{ diagnostics.requestMethod }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Email Sent:</span>
            <span class="text-gray-800">{{ diagnostics.requestPayload.email }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Backend Msg:</span>
            <span class="text-red-700 font-bold">{{ diagnostics.backendMessage }}</span>
          </div>

          <div v-if="diagnostics.fullResponseData">
            <span class="text-gray-500">Full Response:</span>
            <pre class="mt-1 p-2 bg-gray-100 rounded text-[10px] overflow-x-auto whitespace-pre-wrap max-h-32 overflow-y-auto">{{ JSON.stringify(diagnostics.fullResponseData, null, 2) }}</pre>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">Time:</span>
            <span class="text-gray-800">{{ diagnostics.timestamp }}</span>
          </div>
        </div>

        <!-- Quick diagnosis hint -->
        <div class="mt-3 p-2 rounded text-[11px] font-sans" :class="diagnosisHint.bgClass">
          <span class="font-bold" :class="diagnosisHint.textClass">{{ diagnosisHint.title }}</span>
          <p class="text-gray-700 mt-0.5">{{ diagnosisHint.description }}</p>
        </div>
      </div>

      <!-- Backend connectivity test -->
      <div class="mt-3">
        <button @click="testConnection"
          :disabled="isTestingConnection"
          class="text-xs text-blue-500 hover:text-blue-700 underline cursor-pointer disabled:opacity-50">
          {{ isTestingConnection ? 'Testing...' : 'Test Backend Connection' }}
        </button>
        <div v-if="connectionTestResult" class="mt-1 text-xs font-mono"
          :class="connectionTestResult.reachable ? 'text-green-600' : 'text-orange-600'">
          {{ connectionTestResult.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import StyledRouterLink from '@/components/StyledRouterLink.vue';
import { useConnectionStore } from '@/stores/network/connectionStore'
import type { LoginDiagnostics } from '@/stores/network/connectionStore'

export default defineComponent({
  setup() {
    const email = ref('');
    const password = ref('');
    const loginErrorResponse = ref<any>(null);
    const isTestingConnection = ref(false);
    const connectionTestResult = ref<{ reachable: boolean; status?: number; message: string } | null>(null);

    const connectionStore = useConnectionStore();

    const diagnostics = computed<LoginDiagnostics | null>(() => {
      return connectionStore.states.loginDiagnostics;
    });

    const httpStatusColor = computed(() => {
      const status = diagnostics.value?.httpStatus;
      if (!status) return 'text-orange-600';
      if (status >= 500) return 'text-red-600';
      if (status >= 400) return 'text-yellow-600';
      return 'text-green-600';
    });

    const httpStatusText = computed(() => {
      const status = diagnostics.value?.httpStatus;
      if (!status) return 'No response';
      const map: Record<number, string> = {
        0: 'No response / Network failure',
        400: 'Bad Request',
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
        500: 'Internal Server Error',
        502: 'Bad Gateway',
        503: 'Service Unavailable',
      };
      return map[status] || 'Unknown';
    });

    const diagnosisHint = computed(() => {
      const d = diagnostics.value;
      if (!d) return { title: '', description: '', bgClass: '', textClass: '' };

      if (d.errorType === 'network') {
        return {
          title: 'Network Error',
          description: 'The backend server is unreachable. Check: 1) Is the server running? 2) Is Nginx proxy configured correctly? 3) Is the API URL correct?',
          bgClass: 'bg-orange-50 border border-orange-200',
          textClass: 'text-orange-700'
        };
      }

      if (d.httpStatus === 500) {
        return {
          title: 'Server Internal Error (500)',
          description: 'The backend crashed while processing the login. This is likely a backend bug — check the server logs (docker logs merchandise-server). Common cause: password encoding mismatch or missing user data.',
          bgClass: 'bg-red-50 border border-red-200',
          textClass: 'text-red-700'
        };
      }

      if (d.httpStatus === 401) {
        return {
          title: 'Authentication Failed (401)',
          description: 'The email exists but the password does not match. Possible causes: 1) Password not correctly encoded in database (check DataInitializer logs), 2) DelegatingPasswordEncoder prefix mismatch.',
          bgClass: 'bg-yellow-50 border border-yellow-200',
          textClass: 'text-yellow-700'
        };
      }

      if (d.httpStatus === 400) {
        return {
          title: 'Bad Request (400)',
          description: 'The request format is incorrect or validation failed. Check the backend message above for details.',
          bgClass: 'bg-yellow-50 border border-yellow-200',
          textClass: 'text-yellow-700'
        };
      }

      return {
        title: `HTTP ${d.httpStatus}`,
        description: 'Unexpected response from the server. Check the full response data above.',
        bgClass: 'bg-gray-50 border border-gray-200',
        textClass: 'text-gray-700'
      };
    });

    async function handleLogin() {
      await connectionStore.API.submitLogin(email.value, password.value);
      loginErrorResponse.value = connectionStore.states.loginErrorResponse;
    }

    async function testConnection() {
      isTestingConnection.value = true;
      connectionTestResult.value = null;
      try {
        connectionTestResult.value = await connectionStore.API.testBackendConnection();
      } finally {
        isTestingConnection.value = false;
      }
    }

    return {
      email, password, handleLogin, loginErrorResponse,
      diagnostics,
      httpStatusColor, httpStatusText, diagnosisHint,
      testConnection, isTestingConnection, connectionTestResult
    };
  },

  components: { StyledRouterLink }
})
</script>
