import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';

// 文件上传（multipart/form-data），后端返回文件名（不含目录）
export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: routesConstants.UPLOAD,
    method: 'post',
    data: formData,
  });
}
