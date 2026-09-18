import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';

/**
 * 文件上传（multipart/form-data）
 *
 * 后端已接入阿里云 OSS，接口返回的是文件的完整访问地址，
 * 形如 https://<bucket>.<endpoint>/<uuid>-<原文件名>.jpg，
 * 因此调用方拿到结果后直接存入 avatar 等字段即可，无需再做路径拼接。
 */
export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: routesConstants.UPLOAD,
    method: 'post',
    data: formData,
  });
}
