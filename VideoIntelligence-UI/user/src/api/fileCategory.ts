import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type { FileCategoryVO } from '@/types/file/fileCategoryVO'
import type { FileCategoryDTO } from '@/types/file/fileCategoryDTO'
import type { PageResult } from '@/types/result'

/**
 * 侧边栏分类列表每页条数
 * 与后端 FileConstants.DEFAULT_PAGE_SIZE（36）保持一致，避免同一份数据在不同入口下条数不一致
 */
export const CATEGORY_PAGE_SIZE = 36

/**
 * 查询当前登录用户的资源文件（视频）分类列表（不分页，用于侧边栏分类展示）
 *
 * 该接口需要携带 token（网关 /file/** 不在游客白名单内，后端通过 SecurityUtils.getUserId() 取当前用户），
 * token 失效时后端返回 401，由 utils/request.ts 统一清除本地凭证并把错误透传给调用方
 *
 * @returns 当前登录用户下的分类列表（无数据时后端返回空数组）
 */
export function listUserFileCategories() {
    return request.get<unknown, FileCategoryVO[]>(routesConstants.FILE_CATEGORY_USER)
}

/**
 * 分页搜索资源文件（视频）分类
 *
 * 后端 /file/category/list 支持 userId / categoryName / 创建时间范围等条件，
 * 这里按「当前登录用户 + 分类名模糊」查询，用于侧边栏的分类搜索
 *
 * @param pageNum  当前页码
 * @param pageSize 每页条数
 * @param dto      查询条件（userId 必传当前登录用户ID，categoryName 为空表示不按名称过滤）
 * @returns 分页结果（records / total / pages）
 */
export function searchUserFileCategories(pageNum: number, pageSize: number, dto?: FileCategoryDTO) {
    return request.get<unknown, PageResult<FileCategoryVO>>(routesConstants.FILE_CATEGORY_LIST, {
        params: {
            pageNum,
            pageSize,
            // 未填写时传 null，便于后端条件查询处理空条件
            userId: dto?.userId ?? null,
            categoryName: dto?.categoryName || null,
        },
    })
}

/**
 * 查询某个用户的资源文件（视频）分类列表（不分页）
 * 用于「全量删除」前取全部分类ID：侧边栏展示的数据可能被搜索条件或每页条数截断，
 * 直接拿展示列表的ID会出现漏删
 *
 * @param userId 所属用户ID
 * @returns 该用户下的分类列表
 */
export function listAllUserFileCategories(userId: number) {
    return request.get<unknown, FileCategoryVO[]>(routesConstants.FILE_CATEGORY_ALL, {
        params: { userId },
    })
}

/**
 * 新增资源文件（视频）分类
 * 后端 addCategory 会按 userId 做同名校验，因此 userId 必须传当前登录用户ID；
 * sort 不传时走数据库默认值（0，排最前）
 */
export function addFileCategory(data: FileCategoryDTO) {
    return request.post<unknown, void>(routesConstants.FILE_CATEGORY_ADD, data)
}

/**
 * 修改资源文件（视频）分类
 * 后端 updateCategory 会按 userId 做同名校验，因此 userId 必须传当前登录用户ID
 */
export function updateFileCategory(data: FileCategoryDTO) {
    return request.put<unknown, void>(routesConstants.FILE_CATEGORY_UPDATE, data)
}

/**
 * 删除单个资源文件（视频）分类
 *
 * @param id 分类ID
 */
export function deleteFileCategory(id: number) {
    return request.delete<unknown, void>(`${routesConstants.FILE_CATEGORY_DELETE}/${id}`)
}

/**
 * 批量删除资源文件（视频）分类（侧边栏「全量删除」复用该接口）
 *
 * @param ids 分类ID集合
 */
export function batchDeleteFileCategories(ids: number[]) {
    return request.delete<unknown, void>(routesConstants.FILE_CATEGORY_BATCH_DELETE, {
        data: ids,
    })
}

