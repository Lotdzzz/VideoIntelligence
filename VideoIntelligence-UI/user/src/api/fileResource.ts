import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type { FileVO } from '@/types/file/fileVO'
import type { FileDTO } from '@/types/file/fileDTO'
import type { PageResult } from '@/types/result'

/**
 * 视频资源列表每页条数
 * 与后端 FileConstants.DEFAULT_PAGE_SIZE（36）保持一致，避免同一份数据在不同入口下条数不一致
 * 36 = 视频页网格「一行 6 个 × 6 行」，保证每页刚好铺满 6 行
 */
export const RESOURCE_PAGE_SIZE = 36

/**
 * 分页查询资源文件（视频）
 *
 * 后端 /file/resource/list 支持 userId / categoryId / originalName / fileType / fileExt / status / 创建时间范围等条件，
 * 这里按「当前登录用户 + 分类ID + 文件名关键词」查询，用于视频内容区展示资源卡片
 * （beginTime / endTime 后端要求成对传入才生效，且只按 createTime 过滤）
 *
 * 该接口需要携带 token（网关 /file/** 不在游客白名单内），token 失效时后端返回 401，
 * 由 utils/request.ts 统一清除本地凭证并把错误透传给调用方
 *
 * @param pageNum  当前页码
 * @param pageSize 每页条数
 * @param dto      查询条件（userId 传当前登录用户ID，categoryId / originalName 为空表示不过滤）
 * @returns 分页结果（records / total / pages）
 */
export function listResources(pageNum: number, pageSize: number, dto?: FileDTO) {
    return request.get<unknown, PageResult<FileVO>>(routesConstants.FILE_RESOURCE_LIST, {
        params: {
            pageNum,
            pageSize,
            // 未填写时传 null，便于后端条件查询处理空条件
            userId: dto?.userId ?? null,
            categoryId: dto?.categoryId ?? null,
            originalName: dto?.originalName || null,
            fileType: dto?.fileType || null,
            fileExt: dto?.fileExt || null,
            status: dto?.status ?? null,
            beginTime: dto?.beginTime || null,
            endTime: dto?.endTime || null,
        },
    })
}

/**
 * 删除单个资源文件（视频）
 *
 * @param id 文件ID
 */
export function deleteResource(id: number) {
    return request.delete<unknown, void>(`${routesConstants.FILE_RESOURCE_DELETE}/${id}`)
}

/**
 * 批量删除资源文件（视频）
 *
 * @param ids 文件ID集合
 */
export function batchDeleteResources(ids: number[]) {
    return request.delete<unknown, void>(routesConstants.FILE_RESOURCE_BATCH_DELETE, {
        data: ids,
    })
}
