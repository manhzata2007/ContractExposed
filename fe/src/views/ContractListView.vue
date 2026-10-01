<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Upload, RefreshCw } from 'lucide-vue-next'
import { useContractStore } from '@/stores/contractStore'
import ContractTable from '@/components/ContractTable.vue'

const store  = useContractStore()
const router = useRouter()
const currentPage = ref(0)
const pageSize    = ref(10)

async function load(page = 0) {
  currentPage.value = page
  await store.fetchList(page, pageSize.value)
}

onMounted(() => load())

function handlePageChange(page: number) {
  load(page - 1) // Element Plus is 1-indexed
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6 animate-fade-in">

    <!-- Page header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Danh Sách Hợp Đồng</h1>
        <p class="text-gray-500 text-sm mt-0.5">
          {{ store.totalElements }} hợp đồng
        </p>
      </div>
      <div class="flex items-center gap-2">
        <button
          @click="load(currentPage)"
          :disabled="store.isLoadingList"
          class="p-2 hover:bg-gray-100 rounded-xl text-gray-500 transition-colors disabled:opacity-50"
          aria-label="Làm mới"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': store.isLoadingList }" />
        </button>
        <button
          @click="router.push('/upload')"
          class="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-xl text-sm font-medium transition-colors"
        >
          <Upload class="w-4 h-4" />
          Upload mới
        </button>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="store.isLoadingList" class="space-y-3">
      <div v-for="i in 5" :key="i" class="bg-white rounded-xl border border-gray-200 h-14 animate-pulse" />
    </div>

    <template v-else>
      <ContractTable
        :contracts="store.contracts"
        @refresh="load(currentPage)"
      />

      <!-- Pagination -->
      <div v-if="store.totalPages > 1" class="flex justify-center mt-6">
        <el-pagination
          :current-page="currentPage + 1"
          :page-size="pageSize"
          :total="store.totalElements"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </template>

  </div>
</template>
