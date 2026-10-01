<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { FileText, Upload, List, Home, Shield } from 'lucide-vue-next'

const router = useRouter()
const route  = useRoute()
const mobileOpen = ref(false)

const navLinks = [
  { name: 'Trang chủ',      to: '/',          icon: Home     },
  { name: 'Upload HĐ',      to: '/upload',    icon: Upload   },
  { name: 'Danh sách',      to: '/contracts', icon: List     },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <nav class="bg-white border-b border-gray-200 shadow-sm sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">

        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2 group">
          <div class="w-8 h-8 bg-red-600 rounded-lg flex items-center justify-center shadow group-hover:bg-red-700 transition-colors">
            <Shield class="w-5 h-5 text-white" />
          </div>
          <div class="leading-tight">
            <span class="font-bold text-gray-900 text-lg">ContractExposed</span>
            <p class="text-[10px] text-gray-400 hidden sm:block -mt-0.5">Đọc trước khi ký</p>
          </div>
        </router-link>

        <!-- Desktop nav -->
        <div class="hidden md:flex items-center gap-1">
          <router-link
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive(link.to)
              ? 'bg-red-50 text-red-600'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'"
          >
            <component :is="link.icon" class="w-4 h-4" />
            {{ link.name }}
          </router-link>
        </div>

        <!-- CTA button -->
        <router-link
          to="/upload"
          class="hidden md:flex items-center gap-1.5 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          <Upload class="w-4 h-4" />
          Upload Hợp Đồng
        </router-link>

        <!-- Mobile hamburger -->
        <button
          class="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100"
          @click="mobileOpen = !mobileOpen"
          aria-label="Toggle menu"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="!mobileOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <!-- Mobile menu -->
      <div v-if="mobileOpen" class="md:hidden border-t border-gray-100 py-2 animate-fade-in">
        <router-link
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          class="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors"
          :class="isActive(link.to)
            ? 'bg-red-50 text-red-600'
            : 'text-gray-600 hover:bg-gray-100'"
          @click="mobileOpen = false"
        >
          <component :is="link.icon" class="w-4 h-4" />
          {{ link.name }}
        </router-link>
      </div>
    </div>
  </nav>
</template>
