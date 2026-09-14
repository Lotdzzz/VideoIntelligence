/**
 * 后端统一响应
 */
export interface Result<T = any>{

    code:number

    msg:string

    data:T

}

/**
 * MyBatis-Plus 分页结果
 */
export interface PageResult<T = any> {

    /** 记录列表 */
    records: T[]

    /** 总记录数 */
    total: number

    /** 每页条数 */
    size: number

    /** 当前页码 */
    current: number

    /** 总页数 */
    pages: number
}