import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
          meta: { title: 'Trang chủ' },
        },
        {
          path: 'upload',
          name: 'upload',
          component: () => import('@/views/UploadView.vue'),
          meta: { title: 'Upload Hợp Đồng' },
        },
        {
          path: 'contracts',
          name: 'contracts',
          component: () => import('@/views/ContractListView.vue'),
          meta: { title: 'Danh Sách Hợp Đồng' },
        },
        {
          path: 'contracts/:id/report',
          name: 'report',
          component: () => import('@/views/ReportView.vue'),
          meta: { title: 'Báo Cáo Phân Tích' },
          props: true,
        },
        {
          path: 'contracts/:id/analyzing',
          name: 'analyzing',
          component: () => import('@/views/AnalyzingView.vue'),
          meta: { title: 'Đang Phân Tích...' },
          props: true,
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
  scrollBehavior(_, __, savedPosition) {
    return savedPosition || { top: 0 }
  },
})

// Update document title on navigation
router.afterEach((to) => {
  const title = to.meta?.title as string | undefined
  document.title = title ? `${title} – ContractExposed` : 'ContractExposed'
})

export default router
