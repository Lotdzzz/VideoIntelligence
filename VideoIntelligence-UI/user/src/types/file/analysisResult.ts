import type { NodeTreeData } from 'jsmind'

export interface AnalysisResultData {
  id: number
  userId: number
  originalName?: string | null
  cover?: string | null
  [key: string]: unknown
}

export interface AnalysisMind {
  meta?: {
    name?: string
    author?: string
    version?: string
  }
  format: 'node_tree'
  data: NodeTreeData
}

export interface AnalysisResult {
  knowledge?: string | null
  mind?: AnalysisMind | null
  data?: AnalysisResultData | null
}