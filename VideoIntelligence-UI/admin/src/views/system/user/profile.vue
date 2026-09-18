<template>
  <div v-loading="loading" class="profile-page">
    <div class="page-header">
      <span class="page-title">个人中心</span>
      <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
    </div>

    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-header">基本资料</span>
      </template>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="profile-form">
        <el-form-item label="用户账号">
          <el-input v-model="formData.userName" disabled />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="formData.nickName" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="用户邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入用户邮箱" />
        </el-form-item>
        <el-form-item label="用户头像">
          <div class="avatar-section">
            <!-- OSS 返回完整地址，直接用于预览；加载失败时 el-avatar 会自动降级为文字头像 -->
            <el-avatar
              :size="80"
              :src="resolveFileUrl(formData.avatar)"
              @error="handleAvatarError"
            >
              {{ (formData.nickName || formData.userName || '?').slice(0, 1) }}
            </el-avatar>
            <el-upload
              class="avatar-upload"
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleAvatarChange"
            >
              <el-button type="primary" plain :loading="uploading">上传头像</el-button>
            </el-upload>
            <span class="avatar-tip">点击上传选择图片后，点击右上角“保存”生效</span>
          </div>
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            placeholder="留空则不修改密码"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="userInfo?.status === '0' ? 'success' : 'danger'">
            {{ userInfo?.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-header">登录信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="最后登录IP">{{ userInfo?.loginIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ userInfo?.loginDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ userInfo?.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ userInfo?.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadFile } from 'element-plus'
import { getUserByOwner, updateUserByOwner } from '@/api/user'
import { getUserInfo } from '@/api/login'
import { uploadFile } from '@/api/upload'
import { resolveFileUrl } from '@/utils/file'
import { useUserStore } from '@/stores/user'
import type { SysUserVO } from '@/types/user/sysUserVO'
import type { SysUserDTO } from '@/types/user/sysUserDTO'
import type { SysUserInfo } from '@/types/user/userInfo'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref<FormInstance>()

const userInfo = ref<SysUserVO | null>(null)

const defaultFormData = (): SysUserDTO => ({
  userId: undefined,
  userName: '',
  nickName: '',
  email: '',
  avatar: '',
  password: '',
  status: '',
  remark: '',
})

const formData = ref<SysUserDTO>(defaultFormData())

const formRules: FormRules = {
  nickName: [{ required: true, trigger: 'blur', message: '请输入用户昵称' }],
  email: [{ type: 'email', trigger: 'blur', message: '请输入正确的邮箱地址' }],
}

// 解析当前登录用户ID：优先从全局 store 读取（layout 已写入），否则兜底调用接口
async function resolveUserId(): Promise<number> {
  const id = userStore.userInfo.userId
  if (id && id !== -1) return id

  const info = (await getUserInfo()) as unknown as SysUserInfo
  if (info?.userId) {
    userStore.setUserInfo({ userId: info.userId })
    return info.userId
  }

  throw new Error('无法获取当前用户ID')
}

async function loadUserInfo() {
  loading.value = true
  try {
    const userId = await resolveUserId()
    const detail = (await getUserByOwner(userId)) as unknown as SysUserVO
    userInfo.value = detail
    formData.value = {
      userId: detail.userId,
      userName: detail.userName,
      nickName: detail.nickName,
      email: detail.email,
      avatar: detail.avatar,
      password: '',
      status: detail.status,
      remark: detail.remark,
    }
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载个人信息失败')
  } finally {
    loading.value = false
  }
}

async function handleAvatarChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size / 1024 / 1024 > 5) {
    ElMessage.warning('头像大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    // 后端已接入 OSS，返回的是完整访问地址（https://<bucket>.<endpoint>/xxx.png），
    // 直接存入 avatar，点击保存时原样提交给后端
    const fileUrl = (await uploadFile(raw)) as unknown as string
    formData.value.avatar = fileUrl
    ElMessage.success('头像上传成功，请点击保存')
  } catch (error: any) {
    ElMessage.error(error?.msg || '头像上传失败')
  } finally {
    uploading.value = false
  }
}

// 头像加载失败（OSS 地址失效 / 被防盗链拦截）时提示，el-avatar 会自动降级为文字头像
function handleAvatarError() {
  ElMessage.warning('头像图片加载失败，请重新上传')
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await updateUserByOwner({
      userId: formData.value.userId,
      nickName: formData.value.nickName,
      email: formData.value.email,
      // OSS 完整访问地址，原样提交（后端直接落库，前端无需拼接）
      avatar: formData.value.avatar,
      password: formData.value.password,
      remark: formData.value.remark,
    })
    // 同步全局 store，保证其他组件（如侧边栏）读到的是最新头像地址
    userStore.setUserInfo({ avatar: formData.value.avatar ?? '' })
    ElMessage.success('保存成功')
    await loadUserInfo()
  } catch (error: any) {
    ElMessage.error(error?.msg || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.profile-card {
  border-radius: 8px;
}

.card-header {
  font-weight: 600;
}

.profile-form {
  max-width: 560px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-upload {
  display: inline-flex;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}
</style>
