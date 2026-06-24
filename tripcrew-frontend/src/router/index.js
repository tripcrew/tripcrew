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
    path: '/terms',
    name: 'terms',
    component: () => import('@/views/LegalView.vue'),
    props: { documentType: 'terms' },
    meta: { title: '이용약관' }
  },
  {
    path: '/privacy',
    name: 'privacy',
    component: () => import('@/views/LegalView.vue'),
    props: { documentType: 'privacy' },
    meta: { title: '개인정보처리방침' }
  },
  {
    path: '/data-sources',
    name: 'data-sources',
    component: () => import('@/views/LegalView.vue'),
    props: { documentType: 'dataSources' },
    meta: { title: '공공데이터 출처' }
  },
  {
    path: '/faq',
    name: 'faq',
    component: () => import('@/views/FaqView.vue'),
    meta: { title: '자주 묻는 질문' }
  },
  {
    path: '/support',
    name: 'support',
    component: () => import('@/views/SupportView.vue'),
    meta: { title: '1:1 문의' }
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
    // F06 P2a — 편집기 일원화: 공동편집을 PlanEditView 로 합침(혼자=일반 편집기, 여럿=실시간 협업).
    // 옛 /co 진입점은 편집 화면으로 리다이렉트. CoEditView 의 충돌 모달 마크업은 P2b 용으로 보존.
    path: '/plans/:id/co',
    redirect: (to) => `/plans/${to.params.id}/edit`
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
    path: '/wishlist',
    name: 'wishlist',
    component: () => import('@/views/WishlistView.vue'),
    meta: { title: '가보고 싶어요 (찜)', requiresAuth: true }
  },
  {
    path: '/attractions/:id/reviews',
    name: 'reviews',
    component: () => import('@/views/ReviewsView.vue'),
    meta: { title: '후기 (SC-10)' }
  },
  {
    path: '/notices',
    name: 'notices',
    component: () => import('@/views/NoticesView.vue'),
    meta: { title: '공지사항' }
  },
  {
    path: '/notices/:id',
    name: 'notice-detail',
    component: () => import('@/views/NoticeDetailView.vue'),
    meta: { title: '공지 상세' }
  },
  {
    path: '/admin/users',
    name: 'admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { title: '관리자 (SC-11)', requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/admin/notices',
    name: 'admin-notices',
    component: () => import('@/views/AdminNoticesView.vue'),
    meta: { title: '관리자 · 공지 관리', requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/admin/reports',
    name: 'admin-reports',
    component: () => import('@/views/AdminReportsView.vue'),
    meta: { title: '관리자 · 신고 관리', requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/admin/banned',
    name: 'admin-banned',
    component: () => import('@/views/AdminBannedView.vue'),
    meta: { title: '관리자 · 정지된 계정', requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
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

function storedRole() {
  try {
    return JSON.parse(localStorage.getItem('tripcrew.user'))?.role || null
  } catch {
    return null
  }
}

router.beforeEach((to) => {
  if (!to.meta?.requiresAuth) return true

  const hasToken = !!localStorage.getItem('tripcrew.accessToken')
  if (!hasToken) {
    return { path: '/auth', query: { mode: 'login', redirect: to.fullPath } }
  }

  // 역할 제한 라우트(예: 관리자): 권한 없는 사용자는 관리자 화면 노출 없이 403 으로.
  // 서버 인가가 진짜 방어선이고, 이건 화면 자체를 안 보여주기 위한 UX 가드.
  const roles = to.meta?.roles
  if (roles && !roles.includes(storedRole())) {
    return { path: '/errors/403' }
  }

  return true
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `TripCrew · ${to.meta.title}`
  }
})

export default router
