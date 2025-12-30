import { createRouter, createWebHistory } from 'vue-router'
import { useDeviceStore } from '@/stores/device'
import { useUserStore } from '@/stores/user'

const routes = [
  // 默认路由重定向到用户登录
  {
    path: '/',
    redirect: '/login'
  },
  
  // 移动端路由
  {
    path: '/home',
    name: 'mobile-layout',
    component: () => import('@/layouts/MobileLayout.vue'),
    redirect: '/home/index',
    children: [
      {
        path: 'index',
        name: 'mobile-home',
        component: () => import('@/views/mobile/Home.vue'),
        meta: { title: '首页', requireAuth: true }
      },
      {
        path: 'recharge',
        name: 'mobile-recharge',
        component: () => import('@/views/mobile/Recharge.vue'),
        meta: { title: '在线充值', requireAuth: true }
      },
      {
        path: 'orders',
        name: 'mobile-orders',
        component: () => import('@/views/mobile/Orders.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'profile',
        name: 'mobile-profile',
        component: () => import('@/views/mobile/Profile.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
    ]
  },
  //区域选择页
      {
        path: '/areas',
        name: 'Areas',
        component: () => import('@/views/mobile/Areas.vue'),
        meta: { title: '选择区域', requireAuth: true }
      },
      {
        path: '/user/session',
        name: 'UserSession',
        component: () => import('@/views/mobile/Session.vue'),
        meta: { 
          title: '我的上机',
          requiresAuth: true // 需要登录
        }
      },
      {
    path: '/area/:id/computers',
    name: 'Computers',
    component: () => import('@/views/mobile/AreaComputers.vue'),
    meta: { title: '选择电脑' }
  },

  // 管理后台路由
  {
    path: '/admin',
    name: 'admin-layout',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'computers',
        name: 'admin-computers',
        component: () => import('@/views/admin/ComputerMgr.vue'),
        meta: { title: '电脑管理' }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/UserMgr.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'admins',
        name: 'admin-admins',
        component: () => import('@/views/admin/AdminMgr.vue'),
        meta: { title: '管理员管理' }
      },
      {
       path: 'recharge-records',
       name: 'RechargeRecords',
       component: () => import('@/views/admin/RechargeRecordMgr.vue'),
       meta: { title: '充值记录管理' }
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: () => import('@/views/admin/OrderMgr.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'area',
        name: 'admin-area',
        component: () => import('@/views/admin/AreaMgr.vue'),
        meta: { title: '区域管理' }
      }
    ]
  },
  
  // 登录注册页面
  {
    path: '/login',
    name: 'user-login',
    component: () => import('@/views/UserLogin.vue'),
    meta: { title: '用户登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '用户注册' }
  },
  
      {
       path: '/edit-profile',
       name: 'mobile-edit-profile',
       component: () => import('@/views/mobile/EditProfile.vue'),
       meta: { title: '修改信息', requireAuth: true }
      },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const deviceStore = useDeviceStore()
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 睿控网咖管理系统`
  }
  
  // 设备类型重定向 - 添加管理员例外
  if (deviceStore.isMobile && to.path.startsWith('/admin') && !to.path.includes('/admin/login') && !userStore.adminInfo) {
    next('/login')
    return
  }
  
  if (!deviceStore.isMobile && to.path === '/login' && userStore.token && userStore.adminInfo) {
    // 如果已经是管理员登录状态，访问登录页时跳转到管理后台
    next('/admin/dashboard')
    return
  }
  
  // 权限检查
  if (to.meta.requireAuth && !userStore.token) {
    if (deviceStore.isMobile) {
      next('/login')
    } else {
      next('/admin/login')
    }
    return
  }
  
  // 管理员权限检查
  if (to.meta.requireAdmin && (!userStore.token || !userStore.adminInfo)) {
    next('/admin/login')
    return
  }
  
  // 如果已登录用户访问登录页，跳转到首页
  if ((to.name === 'user-login' || to.name === 'register') && userStore.token && userStore.userInfo) {
    next('/home')
    return
  }
  
  // 如果已登录管理员访问管理员登录页，跳转到管理后台
  if (to.name === 'admin-login' && userStore.token && userStore.adminInfo) {
    next('/admin/dashboard')
    return
  }
  
  next()
})

export default router