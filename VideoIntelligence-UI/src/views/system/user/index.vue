<template>
  <div class="user-page">
    <!-- 顶部搜索框架 -->
    <SearchPanel :loading="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="用户账号">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户账号"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-input
          v-model="queryParams.nickName"
          placeholder="请输入用户昵称"
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
      @confirm="handleSubmit"
      @cancel="handleCancel"
    >
      <el-form-item label="用户头像">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          accept="image/*"
          :on-change="handleAvatarChange"
        >
          <img v-if="formData.avatar" :src="resolveFileUrl(formData.avatar)" class="avatar-preview" alt="头像" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="formData.userName" placeholder="请输入用户账号" />
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickName">
        <el-input v-model="formData.nickName" placeholder="请输入用户昵称" />
      </el-form-item>
      <el-form-item label="用户邮箱" prop="email">
        <el-input v-model="formData.email" placeholder="请输入用户邮箱" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="formData.password"
          type="password"
          show-password
          placeholder="请输入密码"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="formData.roleIds"
          multiple
          collapse-tags
          :loading="roleOptionsLoading"
          placeholder="请选择角色"
          style="width: 100%"
          @visible-change="handleRoleSelectVisibleChange"
        >
          <el-option
            v-for="role in roleOptions"
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
      @pagination-change="loadUserData"
    >
      <!-- 头像列自定义渲染 -->
      <template #avatar="{ row }">
        <img v-if="row.avatar" :src="resolveFileUrl(row.avatar)" class="table-avatar" alt="头像" />
        <el-avatar v-else :size="40">{{ (row.nickName || row.userName || '?').slice(0, 1) }}</el-avatar>
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
        <el-button link type="danger" @click="handleDelete([row])">删除</el-button>
      </template>
    </DataTablePanel>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormRules, type UploadFile } from 'element-plus'
import SearchPanel from '@/components/SearchPanel.vue'
import DataTablePanel from '@/components/DataTablePanel.vue'
import FormDialog from '@/components/FormDialog.vue'
import { Plus } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/upload'
import { resolveFileUrl } from '@/utils/file'
import { getUserInfo } from '@/api/login'
import { useUserStore } from '@/stores/user'
import {
  listUsers,
  getUserById,
  addUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
} from '@/api/user'
import { listAllRoles } from '@/api/role'
import type { PageResult } from '@/types/result'
import type { SysUserVO } from '@/types/user/sysUserVO'
import type { SysUserDTO } from '@/types/user/sysUserDTO'
import type { SysUserInfo } from '@/types/user/userInfo'
import type { SysRoleVO } from '@/types/user/sysRoleVO'

// ==================== 搜索加载状态 ====================
const loading = ref(false)
const userStore = useUserStore()

// ==================== 查询参数 ====================
const queryParams = reactive({
  userName: '',
  nickName: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})

// ==================== 表格数据 ====================
const tableData = ref<SysUserVO[]>([])
const total = ref(0)

// ==================== 新增/修改弹窗 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const uploading = ref(false)

// 角色选项（用于新增/修改时多选）
const roleOptions = ref<SysRoleVO[]>([])
const roleOptionsLoading = ref(false)

const defaultFormData = (): SysUserDTO => ({
  userName: '',
  nickName: '',
  email: '',
  avatar: '',
  password: '',
  status: '0',
  remark: '',
  roleIds: [],
})

const formData = ref<SysUserDTO>(defaultFormData())

const formRules: FormRules = {
  userName: [{ required: true, trigger: 'blur', message: '请输入用户账号' }],
  nickName: [{ required: true, trigger: 'blur', message: '请输入用户昵称' }],
  email: [{ type: 'email', trigger: 'blur', message: '请输入正确的邮箱地址' }],
}

// ==================== 列配置 ====================
const columns = [
  { label: '头像', prop: 'avatar', slot: 'avatar', width: 80, align: 'center' as const },
  { label: '用户账号', prop: 'userName' },
  { label: '用户昵称', prop: 'nickName' },
  { label: '邮箱', prop: 'email' },
  { label: '状态', prop: 'status', slot: 'status', width: 100, align: 'center' as const },
  { label: '最后登录IP', prop: 'loginIp', width: 140 },
  { label: '最后登录时间', prop: 'loginDate', width: 180 },
  { label: '备注', prop: 'remark' },
]

// ==================== 加载数据 ====================
// 解析当前登录用户ID：优先从全局 store 读取（layout 已写入），否则兜底调用接口
async function resolveCurrentUserId(): Promise<number> {
  const id = userStore.userInfo.userId
  if (id && id !== -1) return id

  const info = (await getUserInfo()) as unknown as SysUserInfo
  if (info?.userId) {
    userStore.setUserInfo({ userId: info.userId })
    return info.userId
  }

  return -1
}

async function loadUserData() {
  loading.value = true
  try {
    // 组装搜索条件，传递给分页查询接口
    const dto: SysUserDTO = {
      userName: queryParams.userName,
      nickName: queryParams.nickName,
      status: queryParams.status,
    }
    const page = (await listUsers(
      queryParams.pageNum,
      queryParams.pageSize,
      dto,
    )) as unknown as PageResult<SysUserVO>

    const sourceRecords = page.records ?? []
    // 非超级管理员（id !== 1）时，隐藏 id 为 1 的超级管理员用户数据
    const currentUserId = await resolveCurrentUserId()
    let records = sourceRecords
    if (currentUserId !== -1 && currentUserId !== 1) {
      records = sourceRecords.filter((item) => item.userId !== 1)
    }

    // 同步调整总数，避免当前页因过滤导致分页统计不一致
    const removedCount = sourceRecords.length - records.length
    tableData.value = records
    total.value = (page.total ?? 0) - removedCount
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 角色选项 ====================
async function loadRoleOptions() {
  // 已加载过则直接复用，避免重复请求
  if (roleOptions.value.length > 0) return
  roleOptionsLoading.value = true
  try {
    roleOptions.value = (await listAllRoles()) as unknown as SysRoleVO[]
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载角色列表失败')
  } finally {
    roleOptionsLoading.value = false
  }
}

// 下拉框展开时懒加载角色列表
function handleRoleSelectVisibleChange(visible: boolean) {
  if (visible) {
    loadRoleOptions()
  }
}

// ==================== 事件处理 ====================
function handleSearch() {
  queryParams.pageNum = 1
  loadUserData()
}

function handleReset() {
  queryParams.userName = ''
  queryParams.nickName = ''
  queryParams.status = ''
  handleSearch()
}

function handleAdd() {
  formData.value = defaultFormData()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

async function handleEdit(row: SysUserVO) {
  try {
    // 根据用户ID查询最新详情，再回显到表单
    const detail = (await getUserById(row.userId)) as unknown as SysUserVO
    formData.value = {
      userId: detail.userId,
      userName: detail.userName,
      nickName: detail.nickName,
      email: detail.email,
      avatar: detail.avatar,
      password: '',
      status: detail.status,
      remark: detail.remark,
      roleIds: detail.roleIds ?? [],
    }
    dialogTitle.value = '修改用户'
    dialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.msg || '获取用户详情失败')
  }
}

function handleCancel() {
  dialogVisible.value = false
}

async function handleAvatarChange(uploadFileItem: UploadFile) {
  const raw = uploadFileItem.raw
  if (!raw) return
  if (raw.size / 1024 / 1024 > 5) {
    ElMessage.warning('头像大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const fileName = (await uploadFile(raw)) as unknown as string
    formData.value.avatar = fileName
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error?.msg || '头像上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (formData.value.userId) {
      await updateUser(formData.value)
      ElMessage.success('修改成功')
    } else {
      await addUser(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadUserData()
  } catch (error: any) {
    ElMessage.error(error?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(rows: SysUserVO[]) {
  if (!rows.length) return

  const userIds = rows
    .map((row) => row.userId)
    .filter((id): id is number => typeof id === 'number')

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${rows.length} 个用户吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )

    if (userIds.length === 1) {
      await deleteUser(userIds[0]!)
    } else {
      await batchDeleteUsers(userIds)
    }

    ElMessage.success('删除成功')
    loadUserData()
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
  loadUserData()
})
</script>

<style scoped>
.user-page {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.avatar-uploader {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader-icon {
  width: 88px;
  height: 88px;
  font-size: 28px;
  color: #8c939d;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.avatar-preview {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.table-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
}
</style>