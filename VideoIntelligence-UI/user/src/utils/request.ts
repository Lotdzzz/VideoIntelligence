import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { getToken, removeToken } from './token';
import { constants } from '@/constants/configuration';

const service: AxiosInstance = axios.create({
  baseURL: constants.API_BASE_URL,
  timeout: 300000,
});

// 记录每个请求最近一次发起的时间戳（毫秒），用于 2 秒内的防重复提交
const recentRequestTimes = new Map<string, number>();

/** 防重复提交的时间窗口：ms内相同请求直接拦截 */
const DEDUP_WINDOW_MS = 50;

/** 清理已超出时间窗口的记录，避免 Map 无限增长 */
function cleanExpiredRequests(now: number) {
  for (const [key, timestamp] of recentRequestTimes) {
    if (now - timestamp >= DEDUP_WINDOW_MS) {
      recentRequestTimes.delete(key);
    }
  }
}

/**
 * 生成请求唯一标识。
 * 以 method + url + params + data 作为依据，内容完全一致视为重复请求。
 * 注意：这是纯前端近似方案，若业务方在 data 中携带时间戳/随机数会导致判断失效。
 */
function getRequestKey(config: InternalAxiosRequestConfig): string {
  const { method, url, params, data } = config;
  return [method?.toUpperCase(), url, JSON.stringify(params ?? ''), JSON.stringify(data ?? '')].join('&');
}

//请求拦截器
/**
 * Axios 请求拦截器 的标准写法，它的核心目标是：
 * 在每一次 HTTP 请求发送给后端之前
 * 自动把 Token（身份令牌）塞进请求头里，这样后端就能识别出“你是谁”。
 */
service.interceptors.request.use(
  config => {
    const now = Date.now();
    cleanExpiredRequests(now);

    const key = getRequestKey(config);
    const lastTime = recentRequestTimes.get(key);

    if (lastTime !== undefined && now - lastTime < DEDUP_WINDOW_MS) {
      ElMessage.warning('请勿重复提交');
      return Promise.reject(new axios.CanceledError('请勿重复提交'));
    }

    recentRequestTimes.set(key, now);

    const token = getToken();
    if (config.headers.isToken !== false && token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  //将错误对象包装成 Promise 失败状态，抛给调用者（即 .catch() 能捕获到）
  //必须 return Promise.reject，否则这个错误会被“吞掉”，后端会收到空请求
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(res)
    }
    return res.data
  },
  error => {

    // 401 未授权：token 过期或无效，只清除本地凭证，响应体原样透传给调用方，
    // 由调用方展示后端 msg（401 时后端固定返回 {"code": "401"}，没有 msg，会走统一兜底提示）
    // 注意：这里不跳转登录页（user 端没有 /login 路由，跳转只会落到 404 页面）
    if (error.response && error.response.status === 401) {
      removeToken()
      return Promise.reject(error.response.data)
    }

    // 业务错误：后端返回了统一响应体（Result），直接透传给调用方，
    // 让业务代码通过 error.msg 读取后端错误信息
    if (error.response && error.response.data) {
      return Promise.reject(error.response.data)
    }

    let { message } = error

    if (message == "Network Error") {
      message = "后端接口连接异常"
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时"
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.slice(-3) + "异常"
    }
    
    error.message = message
    
    return Promise.reject(error)
  }
)
// 响应拦截器

export default service;
