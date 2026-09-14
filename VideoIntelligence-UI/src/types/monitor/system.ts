/**
 * 系统监控 - CPU 信息
 */
export interface CpuInfo {
  /** CPU 核心数 */
  cores: number

  /** CPU 使用率（百分比） */
  usage: number
}

/**
 * 系统监控 - 磁盘信息
 */
export interface DiskInfo {
  /** 剩余空间（字节） */
  free: number

  /** 挂载点 */
  mount: string

  /** 磁盘名称 */
  name: string

  /** 读取字节数 */
  readBytes: number

  /** 读取次数 */
  reads: number

  /** 总空间（字节） */
  total: number

  /** 已用空间（字节） */
  used: number

  /** 写入字节数 */
  writeBytes: number

  /** 写入次数 */
  writes: number
}

/**
 * 系统监控 - JVM 信息
 */
export interface JvmInfo {
  /** 堆内存最大值（字节） */
  heapMax: number

  /** 堆内存已使用（字节） */
  heapUsed: number

  /** JVM 名称 */
  name: string

  /** 非堆内存已提交（字节） */
  nonHeapCommitted: number

  /** 非堆内存已使用（字节） */
  nonHeapUsed: number

  /** JVM 启动时间戳（毫秒） */
  startTime: number

  /** JVM 运行时长（毫秒） */
  uptime: number

  /** JVM 厂商 */
  vendor: string

  /** JVM 版本 */
  version: string
}

/**
 * 系统监控 - 内存信息
 */
export interface MemoryInfo {
  /** 可用内存（字节） */
  available: number

  /** 总内存（字节） */
  total: number

  /** 已用内存（字节） */
  used: number

  /** 内存使用率（百分比） */
  usedPercent: number
}

/**
 * 系统监控信息（/system/monitor/info 接口返回）
 */
export interface SystemMonitorInfo {
  /** CPU 信息 */
  cpu: CpuInfo

  /** 磁盘列表 */
  disks: DiskInfo[]

  /** JVM 信息 */
  jvm: JvmInfo

  /** 系统负载（Windows 环境下为 null，实际结构待后端确认后补充） */
  load: unknown | null

  /** 内存信息 */
  memory: MemoryInfo
}
