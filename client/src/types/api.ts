/**
 * @file api.ts
 * @description 定义后端 API 通用响应数据结构
 * @input 无
 * @output ApiResponse 泛型接口
 */

/**
 * API 通用响应结构，泛型 T 为业务数据的具体类型
 * @template T - 业务数据的类型
 */
export interface ApiResponse<T> {
  /** 响应时间戳 */
  timestamp: string
  /** 请求是否成功 */
  success: boolean
  /** HTTP 状态码 */
  status: number
  /** 响应消息 */
  message: string
  /** 业务数据 */
  data: T
}
