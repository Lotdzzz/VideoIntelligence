<template>
  <div class="role-page">
    <!-- 顶部搜索框架 -->
    <SearchPanel :loading="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="角色名称">
        <el-input
          v-model="queryParams.roleName"
          placeholder="请输入角色名称"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="权限字符">
        <el-input
          v-model="queryParams.roleKey"
          placeholder="请输入权限字符"
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

    <!-- 新增/修改弹窗 -->
    <FormDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :model="formData"
      :rules="formRules"
      :loading="submitting"
      width="720px"
      @confirm="handleSubmit"
      @cancel="handleCancel"
    >
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="权限字符" prop="roleKey">
        <el-input v-model="formData.roleKey" placeholder="请输入权限字符" />
      </el-form-item>
      <el-form-item label="显示顺序" prop="roleSort">
        <el-input-number v-model="formData.roleSort" :min="0" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
      <el-form-item label="菜单权限" prop="menuIds">
        <div class="menu-perm">
          <div class="menu-perm__toolbar">
            <el-checkbox v-model="parentChildLinked">父子联动</el-checkbox>
            <div class="menu-perm__actions">
              <el-button link type="primary" @click="handleExpandAll">展开全部</el-button>
              <el-button link type="primary" @click="handleCollapseAll">折叠全部</el-button>
              <el-button link type="primary" @click="handleToggleAll">全选/全不选</el-button>
            </div>
          </div>
          <div class="menu-perm__tree">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              :props="{ label: 'menuName', children: 'children' }"
              node-key="menuId"
              show-checkbox
              :check-strictly="!parentChildLinked"
              default-expand-all
            />
          </div>
        </div>
      </el-form-item>
    </FormDialog>

    <!-- 数据内容框架 -->
    <DataTablePanel
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :current="queryParams.pageNum"
      :size="queryParams.pageSize"
      @add="handleAdd"
      @modify="handleEdit"
      @remove="handleDelete"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
      @pagination-change="loadRoleData"
    >
      <!-- 状态列自定义渲染 -->
      <template #status="{ row }">
        <el-tag :type="row.status === '0' ? 'success' : 'danger'">
          {{ row.status === '0' ? '正常' : '停用' }}
        </el-tag>
      </template>

      <!-- 行内操作 -->
      <template #action="{ row }">
        <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
        <el-button link type="danger" @click="handleDelete([row])">删除</el-button>
      </template>
    </DataTablePanel>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElTree, type FormRules } from 'element-plus'
import SearchPanel from '@/components/SearchPanel.vue'
import DataTablePanel from '@/components/DataTablePanel.vue'
import FormDialog from '@/components/FormDialog.vue'
import {
  listRoles,
  getRoleById,
  addRole,
  updateRole,
  deleteRole,
  batchDeleteRoles,
} from '@/api/role'
import { listMenus } from '@/api/menu'
import type { PageResult } from '@/types/result'
import type { SysRoleVO } from '@/types/user/sysRoleVO'
import type { SysRoleDTO } from '@/types/user/sysRoleDTO'
import type { SysMenuVO } from '@/types/user/sysMenuVO'

// ==================== 搜索加载状态 ====================
const loading = ref(false)

// ==================== 查询参数 ====================
const queryParams = reactive({
  roleName: '',
  roleKey: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})

// ==================== 表格数据 ====================
const tableData = ref<SysRoleVO[]>([])
const total = ref(0)

// ==================== 新增/修改弹窗 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)

const defaultFormData = (): SysRoleDTO => ({
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: '',
  menuIds: [],
})

const formData = ref<SysRoleDTO>(defaultFormData())

// ==================== 菜单权限树 ====================
interface MenuTreeNode extends SysMenuVO {
  children?: MenuTreeNode[]
}

const menuTree = ref<MenuTreeNode[]>([])
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
// 父子联动开关（默认开启：勾选父节点自动勾选子节点）
const parentChildLinked = ref(true)

// 收集所有菜单ID，用于全选/全不选
const allMenuIds = computed(() => collectMenuIds(menuTree.value))

function collectMenuIds(nodes: MenuTreeNode[]): number[] {
  const ids: number[] = []
  nodes.forEach((node) => {
    ids.push(node.menuId)
    if (node.children?.length) ids.push(...collectMenuIds(node.children))
  })
  return ids
}

function buildMenuTree(records: SysMenuVO[]): MenuTreeNode[] {
  const map = new Map<number, MenuTreeNode>()
  const roots: MenuTreeNode[] = []

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

  const sortNodes = (list: MenuTreeNode[]) => {
    list.sort((a, b) => a.orderNum - b.orderNum)
    list.forEach((node) => {
      if (node.children?.length) sortNodes(node.children)
    })
  }
  sortNodes(roots)

  return roots
}

async function loadMenuTree() {
  try {
    const page = (await listMenus(1, 1000, {})) as unknown as PageResult<SysMenuVO>
    menuTree.value = buildMenuTree(page.records ?? [])
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载菜单树失败')
  }
}

// 精确回显勾选：临时切换为「不联动」，避免父节点回显时级联勾选所有子级
function setMenuCheckedKeys(keys: number[]) {
  const tree = menuTreeRef.value
  if (!tree) return
  const store = (tree as any).store
  const prev = store.checkStrictly
  store.checkStrictly = true
  tree.setCheckedKeys(keys)
  store.checkStrictly = prev
}

function handleExpandAll() {
  const nodes = Object.values((menuTreeRef.value as any)?.store?.nodesMap ?? {})
  nodes.forEach((node: any) => node.expand())
}

function handleCollapseAll() {
  const nodes = Object.values((menuTreeRef.value as any)?.store?.nodesMap ?? {})
  nodes.forEach((node: any) => node.collapse())
}

function handleToggleAll() {
  const tree = menuTreeRef.value
  if (!tree) return
  const isAll = tree.getCheckedKeys().length === allMenuIds.value.length
  tree.setCheckedKeys(isAll ? [] : allMenuIds.value)
}

const formRules: FormRules = {
  roleName: [{ required: true, trigger: 'blur', message: '请输入角色名称' }],
  roleKey: [{ required: true, trigger: 'blur', message: '请输入权限字符' }],
}

// ==================== 列配置 ====================
const columns = [
  { label: '角色名称', prop: 'roleName' },
  { label: '权限字符', prop: 'roleKey' },
  { label: '显示顺序', prop: 'roleSort', width: 100, align: 'center' as const },
  { label: '状态', prop: 'status', slot: 'status', width: 100, align: 'center' as const },
  { label: '创建时间', prop: 'createTime', width: 180 },
  { label: '备注', prop: 'remark' },
]

// ==================== 加载数据 ====================
async function loadRoleData() {
  loading.value = true
  try {
    const dto: SysRoleDTO = {
      roleName: queryParams.roleName,
      roleKey: queryParams.roleKey,
      status: queryParams.status,
    }
    const page = (await listRoles(
      queryParams.pageNum,
      queryParams.pageSize,
      dto,
    )) as unknown as PageResult<SysRoleVO>
    tableData.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
function handleSearch() {
  queryParams.pageNum = 1
  loadRoleData()
}

function handleReset() {
  queryParams.roleName = ''
  queryParams.roleKey = ''
  queryParams.status = ''
  handleSearch()
}

function handleAdd() {
  formData.value = defaultFormData()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
  nextTick(() => {
    setMenuCheckedKeys([])
  })
}

async function handleEdit(row: SysRoleVO) {
  try {
    const detail = (await getRoleById(row.roleId)) as unknown as SysRoleVO
    formData.value = {
      roleId: detail.roleId,
      roleName: detail.roleName,
      roleKey: detail.roleKey,
      roleSort: detail.roleSort,
      status: detail.status,
      remark: detail.remark,
      menuIds: detail.menuIds ?? [],
    }
    dialogTitle.value = '修改角色'
    dialogVisible.value = true
    nextTick(() => {
      setMenuCheckedKeys(formData.value.menuIds ?? [])
    })
  } catch (error: any) {
    ElMessage.error(error?.msg || '获取角色详情失败')
  }
}

function handleCancel() {
  dialogVisible.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    const checkedKeys = (menuTreeRef.value?.getCheckedKeys() ?? []) as number[]
    const halfCheckedKeys = (menuTreeRef.value?.getHalfCheckedKeys() ?? []) as number[]
    formData.value.menuIds = [...checkedKeys, ...halfCheckedKeys]

    if (formData.value.roleId) {
      await updateRole(formData.value)
      ElMessage.success('修改成功')
    } else {
      await addRole(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadRoleData()
  } catch (error: any) {
    ElMessage.error(error?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(rows: SysRoleVO[]) {
  if (!rows.length) return

  const roleIds = rows
    .map((row) => row.roleId)
    .filter((id): id is number => typeof id === 'number')

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${rows.length} 个角色吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )

    if (roleIds.length === 1) {
      await deleteRole(roleIds[0]!)
    } else {
      await batchDeleteRoles(roleIds)
    }

    ElMessage.success('删除成功')
    loadRoleData()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.msg || '删除失败')
  }
}

function handleCurrentChange(page: number) {
  queryParams.pageNum = page
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
}

// ==================== 初始化 ====================
onMounted(() => {
  loadRoleData()
  loadMenuTree()
})
</script>

<style scoped>
.role-page {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-perm {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.menu-perm__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.menu-perm__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-perm__tree {
  height: 320px;
  overflow-y: auto;
  padding: 8px;
}
</style>