<template>
  <!-- 无可见子菜单：渲染为普通菜单项 -->
  <template v-if="!hasVisibleChildren">
    <div
      class="menu-item"
      :class="{ active: isActive(item.path) }"
      @click="handleMenuClick(item.path)"
    >
      <el-icon :size="18">
        <component :is="resolveIcon(item.meta?.icon)" />
      </el-icon>
      <span v-show="!collapsed">{{ item.meta?.title }}</span>
    </div>
  </template>

  <!-- 有子菜单：渲染为可展开的父级菜单 -->
  <template v-else>
    <div
      class="menu-item menu-item--group"
      :class="{ active: isGroupActive }"
      @click="toggleExpand"
    >
      <el-icon :size="18">
        <component :is="resolveIcon(item.meta?.icon)" />
      </el-icon>
      <span v-show="!collapsed" class="menu-item__label">{{ item.meta?.title }}</span>
      <el-icon v-show="!collapsed" :size="14" class="menu-item__arrow" :class="{ expanded: isExpanded }">
        <ArrowDown />
      </el-icon>
    </div>

    <div v-show="!collapsed && isExpanded" class="sub-menu">
      <SidebarItem
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :collapsed="collapsed"
        :base-path="resolvePath(item.path)"
      />
    </div>
  </template>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  Setting,
  User,
  UserFilled,
  Menu as MenuIcon,
  Grid,
  Document,
  ArrowDown,
} from '@element-plus/icons-vue'
import type { RouterVO } from '@/types/routers'

// ==================== Props ====================
interface Props {
  item: RouterVO
  collapsed?: boolean
  basePath?: string
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  basePath: '',
})

const route = useRoute()
const router = useRouter()

// ==================== 路径处理 ====================
// 拼接父路径与子路径，得到完整可跳转路径
function resolvePath(childPath: string) {
  if (childPath.startsWith('/')) return childPath
  return `${props.basePath}/${childPath}`.replace(/\/+/g, '/')
}

// 父级菜单是否处于激活状态：路由路径以自身路径为前缀即视为激活
const isGroupActive = computed(() => {
  const parentPath = resolvePath(props.item.path)
  return route.path === parentPath || route.path.startsWith(`${parentPath}/`)
})

// 展开/折叠状态：初始时若当前路由位于该菜单子树内，则自动展开（刷新后保持菜单可见）
const isExpanded = ref(isGroupActive.value)

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

// ==================== 子菜单过滤 ====================
// 只显示 hidden != true 的子菜单
const visibleChildren = computed(() => {
  return props.item.children?.filter((child) => child.hidden !== true) ?? []
})

const hasVisibleChildren = computed(() => visibleChildren.value.length > 0)

// 普通菜单项是否激活
function isActive(path: string) {
  const fullPath = resolvePath(path)
  return route.path === fullPath
}

function handleMenuClick(path: string) {
  router.push(resolvePath(path))
}

// ==================== 图标映射 ====================
// 后端返回的是字符串图标名，这里映射到 Element Plus 图标组件
const iconMap: Record<string, unknown> = {
  home: HomeFilled,
  system: Setting,
  user: User,
  peoples: UserFilled,
  'tree-table': Grid,
  menu: MenuIcon,
  document: Document,
}

function resolveIcon(icon?: string) {
  if (!icon) return MenuIcon
  return iconMap[icon] ?? MenuIcon
}
</script>

<style scoped>
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.menu-item.active {
  background: #fff;
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.14);
}

.menu-item__label {
  flex: 1;
}

.menu-item__arrow {
  transition: transform 0.25s ease;
}

.menu-item__arrow.expanded {
  transform: rotate(180deg);
}

.sub-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 16px;
  margin: 4px 0;
}

.sub-menu .menu-item {
  padding: 10px 12px;
  font-size: 13px;
  color: #f8fafc;
  font-weight: 500;
}

/* 子菜单被选中时保持和首页一致的蓝字效果，覆盖上面的浅色文字 */
.sub-menu .menu-item.active {
  color: #2563eb;
  font-weight: 600;
}
</style>