import axios from 'axios'

/**
 * 分片直传工具（对象存储）
 *
 * 与业务请求实例（@/utils/request）完全隔离，原因：
 * 1. 分片上传地址是「预签名URL」的绝对地址（形如 http://host:9000/bucket/object?...），
 *    业务实例的 baseURL（/api）会把它拼坏
 * 2. 预签名URL的有效性由 query 中的签名决定，额外携带 Authorization 等请求头可能触发
 *    SignatureDoesNotMatch；业务实例会给所有请求自动加上 Authorization
 * 3. 业务实例按「后端统一响应体」判定成功（res.code !== 200 即失败），而对象存储返回的是
 *    标准 S3 响应，没有 code/msg 字段
 * 4. 业务实例带 50ms 防重复提交与 300s 超时，均不适用于大分片直传
 */
const storageHttp = axios.create({
  // 预签名URL是绝对地址，baseURL 留空
  baseURL: '',
  // 大分片在慢网下可能远超业务接口的超时时间，这里不设超时
  timeout: 0,
  // 预签名URL是匿名访问，携带 Cookie 会让对象存储的 CORS（Access-Control-Allow-Origin: *）校验失败
  withCredentials: false,
})

/** 分片直传选项 */
export interface PutSliceOptions {
  /** 当前分片的上传进度回调（已上传字节数） */
  onProgress?: (loadedBytes: number) => void
}

/**
 * 把单个分片直传到对象存储
 *
 * @param url    该分片的预签名URL（由后端 slice/info 返回）
 * @param body   该分片的二进制内容（File.slice 得到）
 * @param options 进度回调
 * @returns 响应头中的 ETag（原样返回，上报给后端时不能做任何处理）
 */
export async function putSliceToMinio(
    url: string,
    body: Blob,
    options: PutSliceOptions = {}
): Promise<string> {
  const response = await storageHttp.put(url, body, {
    /**
     * 不设置任何自定义请求头：
     * 分片号 / uploadId / 对象名等已完成签名并写在 query 中，附加请求头（尤其 Content-Type）
     * 可能导致签名不匹配；浏览器会自动按 Blob 类型发送合适的 Content-Type
     */
    headers: {},
    onUploadProgress: (event) => {
      options.onProgress?.(event.loaded)
    },
  })

  // ETag 属于「非简单响应头」，浏览器只有在存储桶的 CORS 暴露了它之后才能读到
  // （实测本环境 MinIO 返回的头名是「Etag」，且 Expose-Headers 里含 Etag 与 *）
  // axios 内部会把响应头归一化，这里对 getter 与属性访问做多路兜底，避免因大小写差异读不到
  const headersContainer = response.headers as unknown as {
    get?: (name: string) => unknown
    [key: string]: unknown
  }
  const fromGetter =
      typeof headersContainer.get === 'function'
          ? (headersContainer.get('etag') ?? headersContainer.get('ETag'))
          : undefined
  const raw =
      fromGetter ??
      headersContainer.etag ??
      headersContainer.ETag ??
      headersContainer.Etag
  const eTag = typeof raw === 'string' ? raw : ''

  if (!eTag) {
    throw new Error('未获取到分片 ETag：请确认对象存储桶已配置 CORS 的 Access-Control-Expose-Headers: ETag')
  }
  return eTag
}
