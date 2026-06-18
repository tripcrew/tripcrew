import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'landing',
    component: () => import('@/views/LandingView.vue'),
    meta: { title: '랜딩 (SC-01)' }
  },
  {
    path: '/auth',
    name: 'auth',
    component: () => import('@/views/AuthView.vue'),
    meta: { title: '회원가입/로그인 (SC-02)' }
  },
  {
    path: '/home',
    name: 'dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '대시보드 (SC-03)', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'chatbot',
    component: () => import('@/views/ChatbotView.vue'),
    meta: { title: '챗봇 (SC-04)', requiresAuth: true }
  },
  {
    path: '/attractions',
    name: 'search',
    component: () => import('@/views/SearchView.vue'),
    meta: { title: '관광지 검색 (SC-05)' }
  },
  {
    path: '/attractions/:id',
    name: 'attraction-detail',
    component: () => import('@/views/AttractionDetailView.vue'),
    meta: { title: '관광지 상세 (SC-06)' }
  },
  {
    path: '/plans/:id/edit',
    name: 'plan-edit',
    component: () => import('@/views/PlanEditView.vue'),
    meta: { title: '여행 계획 편집 (SC-07)', requiresAuth: true }
  },
  {
    path: '/plans/:id/co',
    name: 'co-edit',
    component: () => import('@/views/CoEditView.vue'),
    meta: { title: '공동 편집 (SC-08)', requiresAuth: true }
  },
  {
    path: '/plans',
    name: 'my-plans',
    component: () => import('@/views/MyPlansView.vue'),
    meta: { title: '내 여행 계획 (SC-09)', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '마이페이지', requiresAuth: true }
  },
  {
    path: '/attractions/:id/reviews',
    name: 'reviews',
    component: () => import('@/views/ReviewsView.vue'),
    meta: { title: '후기 (SC-10)' }
  },
  {
    path: '/admin/users',
    name: 'admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { title: '관리자 (SC-11)', requiresAuth: true }
  },
  {
    path: '/errors/:type?',
    name: 'errors',
    component: () => import('@/views/ErrorView.vue'),
    meta: { title: '에러 (SC-12)' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/errors/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (!to.meta?.requiresAuth) return true
  const hasToken = !!localStorage.getItem('tripcrew.accessToken')
  if (hasToken) return true
  return {
    path: '/auth',
    query: { mode: 'login', redirect: to.fullPath }
  }
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `TripCrew · ${to.meta.title}`
  }
})

export default router
