<template>
  <div class="flex justify-center items-center bg-gray-100 min-h-screen">
    <div class="bg-white p-8 rounded shadow-lg w-[25rem]">
      <h2 class="text-2xl mb-4">登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="email" class="block text-gray-700 text-sm font-bold mb-2">邮箱</label>
          <input v-model="email" type="email" id="email"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-6">
          <label for="password" class="block text-gray-700 text-sm font-bold mb-2">密码</label>
          <input v-model="password" type="password" id="password"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <button type="submit"
          class="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none">登录</button>
      </form>
      <div class="flex justify-center items-center mt-4">
        <h4 class="mr-1">新用户？</h4>
        <StyledRouterLink text="点击注册" path="/signup" textClass="text-blue-700 hover:text-blue-600 font-semibold" />
      </div>

      <!-- Error message -->
      <div v-if="loginErrorResponse" class="flex justify-center font-semibold text-red-700 mt-2">
        <p>{{ loginErrorResponse.message }}</p>
      </div>

      <!-- Diagnostics panel - always visible on error -->
      <div v-if="diagnostics" class="mt-3 p-3 bg-gray-50 rounded border border-gray-200 text-xs font-mono">
        <div class="font-sans font-bold text-sm text-gray-700 mb-2">登录诊断</div>

        <div class="space-y-1.5">
          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">错误类型：</span>
            <span :class="diagnostics.errorType === 'network' ? 'text-orange-600' : 'text-red-600'" class="font-bold">
              {{ diagnostics.errorType === 'network' ? '网络错误' : '后端错误' }}
            </span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">HTTP 状态：</span>
            <span class="font-bold" :class="httpStatusColor">{{ diagnostics.httpStatus || 'N/A' }}</span>
            <span class="text-gray-400 ml-1">({{ httpStatusText }})</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">请求 URL：</span>
            <span class="text-gray-800 break-all">{{ diagnostics.requestUrl }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">请求方法：</span>
            <span class="text-gray-800">{{ diagnostics.requestMethod }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">发送邮箱：</span>
            <span class="text-gray-800">{{ diagnostics.requestPayload.email }}</span>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">后端消息：</span>
            <span class="text-red-700 font-bold">{{ diagnostics.backendMessage }}</span>
          </div>

          <div v-if="diagnostics.fullResponseData">
            <span class="text-gray-500">完整响应：</span>
            <pre class="mt-1 p-2 bg-gray-100 rounded text-[10px] overflow-x-auto whitespace-pre-wrap max-h-32 overflow-y-auto">{{ JSON.stringify(diagnostics.fullResponseData, null, 2) }}</pre>
          </div>

          <div class="flex">
            <span class="text-gray-500 w-28 shrink-0">时间：</span>
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
          {{ isTestingConnection ? '测试中...' : '测试后端连接' }}
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
      if (!status) return '无响应';
      const map: Record<number, string> = {
        0: '无响应 / 网络故障',
        400: '请求错误',
        401: '未授权',
        403: '禁止访问',
        404: '未找到',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务不可用',
      };
      return map[status] || '未知';
    });

    const diagnosisHint = computed(() => {
      const d = diagnostics.value;
      if (!d) return { title: '', description: '', bgClass: '', textClass: '' };

      if (d.errorType === 'network') {
        return {
          title: '网络错误',
          description: '后端服务器无法访问。请检查：1) 服务器是否正在运行？2) Nginx 代理配置是否正确？3) API 地址是否正确？',
          bgClass: 'bg-orange-50 border border-orange-200',
          textClass: 'text-orange-700'
        };
      }

      if (d.httpStatus === 500) {
        return {
          title: '服务器内部错误 (500)',
          description: '后端在处理登录时崩溃。这可能是后端 Bug — 请检查服务器日志（docker logs merchandise-server）。常见原因：密码编码不匹配或缺少用户数据。',
          bgClass: 'bg-red-50 border border-red-200',
          textClass: 'text-red-700'
        };
      }

      if (d.httpStatus === 401) {
        return {
          title: '认证失败 (401)',
          description: '邮箱存在但密码不匹配。可能原因：1) 密码在数据库中未正确编码（检查 DataInitializer 日志），2) DelegatingPasswordEncoder 前缀不匹配。',
          bgClass: 'bg-yellow-50 border border-yellow-200',
          textClass: 'text-yellow-700'
        };
      }

      if (d.httpStatus === 400) {
        return {
          title: '请求错误 (400)',
          description: '请求格式不正确或验证失败。请查看上方的后端消息了解详情。',
          bgClass: 'bg-yellow-50 border border-yellow-200',
          textClass: 'text-yellow-700'
        };
      }

      return {
        title: `HTTP ${d.httpStatus}`,
        description: '服务器返回了意外的响应。请查看上方的完整响应数据。',
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
