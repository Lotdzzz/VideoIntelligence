<template>
  <div class="menu-page">
    <!-- 顶部搜索框架 -->
    <SearchPanel :loading="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="菜单名称">
        <el-input
          v-model="queryParams.menuName"
          placeholder="请输入菜单名称"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
    </SearchPanel>

    <!-- 修改弹窗 -->
    <FormDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :model="formData"
      :rules="formRules"
      :loading="submitting"
      @confirm="handleSubmit"
      @cancel="handleCancel"
    >
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
      </el-form-item>
      <el-form-item label="上级菜单" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="parentTree"
          :props="{ label: 'menuName', children: 'children', disabled: 'disabled' }"
          node-key="menuId"
          check-strictly
          filterable
          placeholder="请选择上级菜单"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="显示顺序" prop="orderNum">
        <el-input-number v-model="formData.orderNum" :min="0" />
      </el-form-item>
      <el-form-item label="路由地址" prop="path">
        <el-input v-model="formData.path" placeholder="请输入路由地址" />
      </el-form-item>
      <el-form-item label="组件路径" prop="component">
        <el-input v-model="formData.component" placeholder="请输入组件路径" />
      </el-form-item>
      <el-form-item label="路由参数" prop="query">
        <el-input v-model="formData.query" placeholder="请输入路由参数" />
      </el-form-item>
      <el-form-item label="路由名称" prop="routeName">
        <el-input v-model="formData.routeName" placeholder="请输入路由名称" />
      </el-form-item>
      <el-form-item label="是否外链" prop="isFrame">
        <el-switch v-model="formData.isFrame" :active-value="0" :inactive-value="1" />
      </el-form-item>
      <el-form-item label="是否缓存" prop="isCache">
        <el-switch v-model="formData.isCache" :active-value="0" :inactive-value="1" />
      </el-form-item>
      <el-form-item label="菜单类型" prop="menuType">
        <el-select v-model="formData.menuType" placeholder="请选择菜单类型">
          <el-option label="目录" value="M" />
          <el-option label="菜单" value="C" />
          <el-option label="按钮" value="F" />
        </el-select>
      </el-form-item>
      <el-form-item label="显示状态" prop="visible">
        <el-select v-model="formData.visible" placeholder="请选择显示状态">
          <el-option label="显示" value="0" />
          <el-option label="隐藏" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="权限标识" prop="perms">
        <el-input v-model="formData.perms" placeholder="请输入权限标识" />
      </el-form-item>
      <el-form-item label="菜单图标" prop="icon">
        <el-input v-model="formData.icon" placeholder="请输入菜单图标" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </FormDialog>

    <!-- 数据内容框架 -->
    <TreeTablePanel
      :data="treeData"
      :columns="columns"
      :loading="loading"
      row-key="menuId"
      :default-expand-all="false"
      :show-add="false"
      :show-edit="false"
      :show-delete="false"
    >
      <!-- 类型列自定义渲染 -->
      <template #menuType="{ row }">
        <el-tag :type="menuTypeTag(row.menuType)">
          {{ menuTypeText(row.menuType) }}
        </el-tag>
      </template>

      <!-- 状态列自定义渲染 -->
      <template #status="{ row }">
        <el-tag :type="row.status === '0' ? 'success' : 'danger'">
          {{ row.status === '0' ? '正常' : '停用' }}
        </el-tag>
      </template>

      <!-- 行内操作 -->
      <template #action="{ row }">
        <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>

      <!-- 右侧工具栏：演示弹窗入口 -->
      <template #toolbar>
        <el-button type="primary" @click="demoDialogVisible = true">AI Agent只能添加菜单SQL</el-button>
      </template>
    </TreeTablePanel>

    <!-- views 目录 + AI 对话弹窗 -->
    <ResizableDialog v-model="demoDialogVisible" title="views 目录 + AI 助手" width="1000px">
      <div class="mcp-layout">
        <!-- 左侧：views 目录树（20%） -->
        <div class="mcp-views">
          <div class="mcp-views__header">
            <span class="mcp-views__title">views 目录</span>
            <span class="mcp-views__tip">点击文件可回填组件路径</span>
          </div>
          <el-input
            v-model="viewFilter"
            placeholder="搜索目录 / 文件"
            clearable
            class="mcp-views__search"
            :prefix-icon="Search"
            @input="handleViewFilter"
          />
          <div class="mcp-views__tree">
            <el-tree
              ref="viewTreeRef"
              :data="viewTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="id"
              default-expand-all
              highlight-current
              :expand-on-click-node="false"
              :filter-node-method="filterViewNode"
              @node-click="handleViewNodeClick"
            >
              <template #default="{ data }">
                <span class="mcp-views__node" :class="{ 'is-file': data.componentPath }">
                  <el-icon :size="16" class="mcp-views__node-icon">
                    <Folder v-if="!data.componentPath" />
                    <Document v-else />
                  </el-icon>
                  <span class="mcp-views__node-label">{{ data.label }}</span>
                  <el-tag
                    v-if="data.componentPath"
                    size="small"
                    type="info"
                    effect="plain"
                    class="mcp-views__node-tag"
                  >
                    页面
                  </el-tag>
                </span>
              </template>
            </el-tree>
          </div>
        </div>

        <!-- 中间：AI 对话 -->
        <div class="mcp-chat">
          <div class="mcp-chat__hint">
            请在右侧填写 perms、icon、menu_name 等参数后再向 AI 提问。
          </div>
          <div ref="chatMessagesRef" class="mcp-chat__messages">
            <div
              v-for="(msg, index) in chatMessages"
              :key="index"
              class="mcp-chat__message"
              :class="msg.role"
            >
              <div class="mcp-chat__role">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
              <div class="mcp-chat__content">{{ msg.content }}</div>
            </div>
          </div>
          <div class="mcp-chat__input">
            <el-input
              v-model="chatInput"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="请输入问题，Enter 发送，Shift+Enter 换行"
              :disabled="chatLoading"
              @keydown.enter.exact.prevent="handleSendChat"
            />
            <el-button type="primary" :loading="chatLoading" @click="handleSendChat">发送</el-button>
          </div>
        </div>

        <!-- 右侧：上下文参数（20%） -->
        <div class="mcp-params">
          <div class="mcp-params__header">
            <span class="mcp-params__title">上下文参数</span>
            <span class="mcp-params__tip">每个字段可添加多个值</span>
          </div>
          <div class="mcp-params__body">
            <div class="mcp-chat__param-list">
              <div v-for="(group, groupIndex) in chatParams" :key="group.key" class="mcp-chat__param-group">
                <div class="mcp-chat__param-group-label">
                  <span class="mcp-chat__param-group-name">{{ group.label }}</span>
                  <el-button link type="primary" :icon="Plus" @click="addParamValue(groupIndex)">添加</el-button>
                </div>
                <div class="mcp-chat__param-values">
                  <div v-for="(value, valueIndex) in group.values" :key="valueIndex" class="mcp-chat__param-row">
                    <el-input
                      v-model="group.values[valueIndex]"
                      :placeholder="group.placeholder"
                      class="mcp-chat__param-value"
                    />
                    <el-button
                      link
                      type="danger"
                      :icon="Delete"
                      :disabled="group.values.length <= 1"
                      class="mcp-chat__param-del"
                      @click="removeParamValue(groupIndex, valueIndex)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ResizableDialog>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElTree, type FormRules } from 'element-plus'
import { Search, Plus, Delete, Folder, Document } from '@element-plus/icons-vue'
import SearchPanel from '@/components/SearchPanel.vue'
import TreeTablePanel from '@/components/TreeTablePanel.vue'
import FormDialog from '@/components/FormDialog.vue'
import ResizableDialog from '@/components/ResizableDialog.vue'
import { chatWithAi } from '@/api/mcp'
import {
  listMenus,
  getMenuById,
  updateMenu,
  deleteMenu,
} from '@/api/menu'
import type { PageResult } from '@/types/result'
import type { SysMenuVO } from '@/types/user/sysMenuVO'
import type { SysMenuDTO } from '@/types/user/sysMenuDTO'

// ==================== 搜索加载状态 ====================
const loading = ref(false)

// ==================== 查询参数 ====================
const queryParams = reactive({
  menuName: '',
  status: '',
})

// ==================== 树形数据 ====================
interface SysMenuTreeNode extends SysMenuVO {
  children?: SysMenuTreeNode[]
}

const treeData = ref<SysMenuTreeNode[]>([])

// 上级菜单下拉树节点（仅展示目录/菜单，排除按钮）
interface ParentMenuNode {
  menuId: number
  menuName: string
  children?: ParentMenuNode[]
  disabled?: boolean
}

const parentTree = ref<ParentMenuNode[]>([])

// ==================== 演示弹窗（可移动可拉伸） ====================
const demoDialogVisible = ref(false)

// ==================== views 目录树（构建时静态枚举 src/views 下的页面文件） ====================
interface ViewTreeNode {
  id: string
  label: string
  componentPath?: string
  children?: ViewTreeNode[]
}

// 收集 src/views 下所有 .vue 文件（与 router 组件映射同源，保证 component 字段格式一致）
const viewModules = import.meta.glob('@/views/**/*.vue')

// 把 glob key（如 /src/views/system/user/index.vue）规范成 system/user/index
function normalizeViewPath(key: string): string {
  return key
    .replace(/^\/src\/views\//, '')
    .replace(/^\/views\//, '')
    .replace(/^@\/views\//, '')
    .replace(/\.vue$/, '')
}

// 将 views 下的文件路径组装成「目录 -> 文件」树
function buildViewTree(): ViewTreeNode[] {
  const root: ViewTreeNode = { id: 'views', label: 'views', children: [] }
  const dirMap = new Map<string, ViewTreeNode>()

  Object.keys(viewModules)
    .map(normalizeViewPath)
    .sort()
    .forEach((rel) => {
      const parts = rel.split('/') // ['system', 'user', 'index']
      const fileName = parts.pop()! // 'index'
      const dirs = parts // ['system', 'user']

      let parent = root
      let acc = ''
      for (const dir of dirs) {
        acc = acc ? `${acc}/${dir}` : dir
        let node = dirMap.get(acc)
        if (!node) {
          node = { id: `dir:${acc}`, label: dir, children: [] }
          dirMap.set(acc, node)
          parent.children!.push(node)
        }
        parent = node
      }

      parent.children!.push({
        id: `file:${rel}`,
        label: `${fileName}.vue`,
        componentPath: rel,
      })
    })

  return root.children ?? []
}

const viewTree = ref<ViewTreeNode[]>(buildViewTree())

// 点击文件节点回填「组件路径」字段
function handleViewNodeClick(node: ViewTreeNode) {
  if (node.componentPath) {
    formData.value.component = node.componentPath
    ElMessage.success(`已回填组件路径：${node.componentPath}`)
  }
}

// ==================== AI 对话（MCP 助手） ====================
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

const chatMessages = ref<ChatMessage[]>([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessagesRef = ref<HTMLElement>()

// 对话前需要用户补充的上下文参数（每个字段支持多个值）
interface ChatParamGroup {
  key: string
  label: string
  placeholder: string
  values: string[]
}

const chatParams = ref<ChatParamGroup[]>([
  { key: 'perms', label: 'perms（权限标识）', placeholder: '请输入权限标识', values: [''] },
  { key: 'icon', label: 'icon（菜单图标）', placeholder: '请输入菜单图标', values: [''] },
  { key: 'menu_name', label: 'menu_name（菜单名称）', placeholder: '请输入菜单名称', values: [''] },
])

function addParamValue(groupIndex: number) {
  chatParams.value[groupIndex]?.values.push('')
}

function removeParamValue(groupIndex: number, valueIndex: number) {
  const group = chatParams.value[groupIndex]
  if (!group || group.values.length <= 1) return
  group.values.splice(valueIndex, 1)
}

// views 目录树过滤
const viewTreeRef = ref<InstanceType<typeof ElTree>>()
const viewFilter = ref('')

function filterViewNode(value: string, data: any): boolean {
  if (!value) return true
  return (data?.label ?? '').includes(value)
}

function handleViewFilter(value: string) {
  viewTreeRef.value?.filter(value)
}

// 发送对话：把 perms/icon/menu_name 作为上下文拼进 prompt
async function handleSendChat() {
  const text = chatInput.value.trim()
  if (!text || chatLoading.value) return

  const context = chatParams.value
    .map((g) => {
      const values = g.values.map((v) => v.trim()).filter(Boolean)
      return `${g.key}: ${values.length ? values.join('、') : '未填写'}`
    })
    .join('\n')
  const prompt = `${context}\n\n${text}`

  chatMessages.value.push({ role: 'user', content: text })
  chatInput.value = ''
  chatMessages.value.push({ role: 'assistant', content: '' })

  chatLoading.value = true
  try {
    const content = (await chatWithAi(prompt)) as unknown as string
    const last = chatMessages.value[chatMessages.value.length - 1]
    if (last) last.content = content
    scrollChatToBottom()
  } catch (error: any) {
    const last = chatMessages.value[chatMessages.value.length - 1]
    if (last) last.content = error?.message || '对话失败，请重试'
    scrollChatToBottom()
  } finally {
    chatLoading.value = false
  }
}

// 消息区滚动到底部
async function scrollChatToBottom() {
  await nextTick()
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// ==================== 修改弹窗 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)

const defaultFormData = (): SysMenuDTO => ({
  menuName: '',
  parentId: 0,
  orderNum: 0,
  path: '',
  component: '',
  query: '',
  routeName: '',
  isFrame: 1,
  isCache: 0,
  menuType: 'C',
  visible: '0',
  status: '0',
  perms: '',
  icon: '',
  remark: '',
})

const formData = ref<SysMenuDTO>(defaultFormData())

const formRules: FormRules = {
  menuName: [{ required: true, trigger: 'blur', message: '请输入菜单名称' }],
}

// ==================== 列配置 ====================
const columns = [
  { label: '菜单名称', prop: 'menuName' },
  { label: '图标', prop: 'icon' },
  { label: '排序', prop: 'orderNum', width: 80, align: 'center' as const },
  { label: '类型', prop: 'menuType', slot: 'menuType', width: 100, align: 'center' as const },
  { label: '权限标识', prop: 'perms' },
  { label: '组件路径', prop: 'component' },
  { label: '状态', prop: 'status', slot: 'status', width: 100, align: 'center' as const },
  { label: '创建时间', prop: 'createTime', width: 180 },
]

// ==================== 菜单类型映射 ====================
function menuTypeText(type: string) {
  const map: Record<string, string> = {
    M: '目录',
    C: '菜单',
    F: '按钮',
  }
  return map[type] ?? type
}

function menuTypeTag(type: string): 'success' | 'info' | 'warning' {
  const map: Record<string, 'success' | 'info' | 'warning'> = {
    M: 'info',
    C: 'success',
    F: 'warning',
  }
  return map[type] ?? 'info'
}

// ==================== 加载数据 ====================
async function loadMenuData() {
  loading.value = true
  try {
    const dto: SysMenuDTO = {
      menuName: queryParams.menuName,
      status: queryParams.status,
    }
    const page = (await listMenus(1, 1000, dto)) as unknown as PageResult<SysMenuVO>
    const records = page.records ?? []
    treeData.value = buildMenuTree(records)

    // 无筛选条件时，本次返回的就是完整菜单列表，顺带刷新上级菜单树
    if (!queryParams.menuName && !queryParams.status) {
      buildParentTree(records)
    }
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 将平铺菜单按 parentId 组装成树形结构，顶层节点 parentId 为 0
function buildMenuTree(records: SysMenuVO[]): SysMenuTreeNode[] {
  const map = new Map<number, SysMenuTreeNode>()
  const roots: SysMenuTreeNode[] = []

  records.forEach((item) => {
    map.set(item.menuId, { ...item, children: [] })
  })

  map.forEach((node) => {
    const parent = map.get(node.parentId)
    if (parent) {
      parent.children!.push(node)
    } else {
      roots.push(node)
    }
  })

  const sortNodes = (list: SysMenuTreeNode[]) => {
    list.sort((a, b) => a.orderNum - b.orderNum)
    list.forEach((node) => {
      if (node.children?.length) sortNodes(node.children)
    })
  }
  sortNodes(roots)

  return roots
}

// ==================== 上级菜单树 ====================
function buildParentTree(records: SysMenuVO[]) {
  const filtered = records.filter((item) => item.menuType !== 'F')
  parentTree.value = [
    { menuId: 0, menuName: '顶级菜单', children: [] },
    ...toParentNodes(buildMenuTree(filtered)),
  ]
}

// 将完整树节点转换为上级菜单树节点（仅保留下拉所需字段）
function toParentNodes(nodes: SysMenuTreeNode[]): ParentMenuNode[] {
  return nodes.map((node) => ({
    menuId: node.menuId,
    menuName: node.menuName,
    disabled: false,
    children: node.children ? toParentNodes(node.children) : undefined,
  }))
}

// 重置所有节点的禁用状态
function resetParentDisabled(nodes: ParentMenuNode[]) {
  nodes.forEach((node) => {
    node.disabled = false
    if (node.children?.length) resetParentDisabled(node.children)
  })
}

// 禁用指定节点及其所有子孙节点，返回是否命中
function disableNodeAndDescendants(nodes: ParentMenuNode[], menuId: number): boolean {
  for (const node of nodes) {
    if (node.menuId === menuId) {
      node.disabled = true
      const markDisabled = (n: ParentMenuNode) => {
        n.disabled = true
        n.children?.forEach(markDisabled)
      }
      node.children?.forEach(markDisabled)
      return true
    }
    if (node.children?.length && disableNodeAndDescendants(node.children, menuId)) {
      return true
    }
  }
  return false
}

// ==================== 事件处理 ====================
function handleSearch() {
  loadMenuData()
}

function handleReset() {
  queryParams.menuName = ''
  queryParams.status = ''
  handleSearch()
}

async function handleEdit(row: SysMenuVO) {
  try {
    const detail = (await getMenuById(row.menuId)) as unknown as SysMenuVO
    formData.value = {
      menuId: detail.menuId,
      menuName: detail.menuName,
      parentId: detail.parentId,
      orderNum: detail.orderNum,
      path: detail.path,
      component: detail.component,
      query: detail.query,
      routeName: detail.routeName,
      isFrame: detail.isFrame,
      isCache: detail.isCache,
      menuType: detail.menuType,
      visible: detail.visible,
      status: detail.status,
      perms: detail.perms,
      icon: detail.icon,
      remark: detail.remark,
    }
    dialogTitle.value = '修改菜单'
    // 禁用自身及子孙节点，避免把上级选为自己或自己的下级
    resetParentDisabled(parentTree.value)
    disableNodeAndDescendants(parentTree.value, detail.menuId)
    dialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.msg || '获取菜单详情失败')
  }
}

function handleCancel() {
  dialogVisible.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    await updateMenu(formData.value)
    ElMessage.success('修改成功')
    dialogVisible.value = false
    loadMenuData()
  } catch (error: any) {
    ElMessage.error(error?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: SysMenuVO) {
  if (!row.menuId) return

  try {
    await ElMessageBox.confirm(
      `确定删除菜单「${row.menuName}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )

    await deleteMenu(row.menuId)

    ElMessage.success('删除成功')
    loadMenuData()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.msg || '删除失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadMenuData()
})
</script>

<style scoped>
.menu-page {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ==================== views 目录 + AI 对话弹窗 ==================== */
.mcp-layout {
  display: flex;
  height: 520px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.mcp-views {
  width: 20%;
  min-width: 170px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
  background: #fafbfc;
}

.mcp-views__header {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.mcp-views__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.mcp-views__tip {
  font-size: 12px;
  color: #909399;
}

.mcp-views__search {
  margin: 10px 12px 4px;
  width: calc(100% - 24px);
}

.mcp-views__search :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: box-shadow 0.2s;
}

.mcp-views__search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.mcp-views__search :deep(.el-input__prefix) {
  color: #a8abb2;
}

.mcp-views__tree {
  flex: 1;
  overflow: auto;
  padding: 4px 8px 8px;
}

.mcp-views__tree :deep(.el-tree-node__content) {
  height: 32px;
  border-radius: 6px;
  margin: 2px 0;
}

.mcp-views__tree :deep(.el-tree-node__content:hover) {
  background: #ecf5ff;
}

.mcp-views__tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #d9ecff;
  color: #409eff;
}

.mcp-views__node {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding-right: 4px;
}

.mcp-views__node-icon {
  color: #f7ba2a;
  flex-shrink: 0;
}

.mcp-views__node.is-file .mcp-views__node-icon {
  color: #409eff;
}

.mcp-views__node-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.mcp-views__node-tag {
  flex-shrink: 0;
}

.mcp-chat {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.mcp-chat__hint {
  padding: 10px 14px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 13px;
  border-bottom: 1px solid #d9ecff;
}

.mcp-params {
  width: 20%;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e5e7eb;
  background: #fafbfc;
}

.mcp-params__header {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.mcp-params__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.mcp-params__tip {
  font-size: 12px;
  color: #909399;
}

.mcp-params__body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.mcp-chat__param-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mcp-chat__param-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mcp-chat__param-group-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.mcp-chat__param-group-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: #606266;
}

.mcp-chat__param-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mcp-chat__param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mcp-chat__param-value {
  flex: 1;
  min-width: 0;
}

.mcp-chat__param-del {
  flex-shrink: 0;
}

.mcp-chat__messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mcp-chat__message {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.mcp-chat__message .mcp-chat__role {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.mcp-chat__message.user .mcp-chat__role {
  background: #409eff;
  color: #fff;
}

.mcp-chat__message.assistant .mcp-chat__role {
  background: #f0f2f5;
  color: #303133;
}

.mcp-chat__content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-size: 13px;
  color: #303133;
  flex: 1;
}

.mcp-chat__input {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid #e5e7eb;
}
</style>