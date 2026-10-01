<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Upload, Shield, Zap, FileSearch, CheckCircle } from 'lucide-vue-next'
import DashboardStats from '@/components/DashboardStats.vue'

const router = useRouter()

const features = [
  {
    icon: Upload,
    title: 'Upload nhiều định dạng',
    desc: 'PDF, DOCX, DOC, và ảnh scan (PNG/JPG). OCR tự động cho tài liệu scan.',
    color: 'bg-blue-50 text-blue-600',
  },
  {
    icon: Zap,
    title: 'AI phân tích tức thì',
    desc: 'GPT-4o-mini đọc và phát hiện các điều khoản bất lợi trong vài giây.',
    color: 'bg-purple-50 text-purple-600',
  },
  {
    icon: FileSearch,
    title: 'Highlight trực tiếp trên PDF',
    desc: 'Các điều khoản rủi ro được highlight màu ngay trên tài liệu gốc.',
    color: 'bg-yellow-50 text-yellow-600',
  },
  {
    icon: CheckCircle,
    title: 'Checklist đàm phán',
    desc: 'Danh sách việc cần làm trước khi ký, được ưu tiên theo mức độ rủi ro.',
    color: 'bg-green-50 text-green-600',
  },
]

const steps = [
  { num: '01', title: 'Upload hợp đồng',     desc: 'Kéo & thả file hoặc chọn từ máy tính'  },
  { num: '02', title: 'AI trích xuất text',   desc: 'Tự động nhận diện nội dung qua OCR'     },
  { num: '03', title: 'Phân tích điều khoản', desc: 'AI phát hiện rủi ro và giải thích rõ ràng'},
  { num: '04', title: 'Nhận báo cáo',         desc: 'Xem highlight PDF + checklist đàm phán'  },
]
</script>

<template>
  <div class="animate-fade-in">

    <!-- Hero -->
    <section class="bg-gradient-to-br from-red-600 to-red-800 text-white py-16 px-4">
      <div class="max-w-4xl mx-auto text-center">
        <div class="inline-flex items-center gap-2 bg-white/10 rounded-full px-4 py-1.5 text-sm mb-6 backdrop-blur">
          <Shield class="w-4 h-4" />
          Bảo vệ quyền lợi của bạn trước khi ký hợp đồng
        </div>
        <h1 class="text-4xl sm:text-5xl font-bold mb-4 text-balance">
          Phân Tích Hợp Đồng<br />
          <span class="text-red-200">Bằng Trí Tuệ Nhân Tạo</span>
        </h1>
        <p class="text-lg text-red-100 mb-8 max-w-2xl mx-auto text-balance">
          Upload hợp đồng thuê nhà, dịch vụ hoặc lao động — AI sẽ phát hiện các điều khoản bất lợi,
          giải thích rủi ro và tạo checklist đàm phán cho bạn.
        </p>
        <button
          @click="router.push('/upload')"
          class="inline-flex items-center gap-2 bg-white text-red-600 hover:bg-red-50 font-bold px-8 py-4 rounded-2xl text-lg shadow-lg transition-all hover:scale-105"
        >
          <Upload class="w-5 h-5" />
          Upload Hợp Đồng Ngay
        </button>
        <p class="text-red-200 text-sm mt-4">Miễn phí · Bảo mật · Không cần đăng ký</p>
      </div>
    </section>

    <!-- Dashboard stats -->
    <section class="max-w-7xl mx-auto px-4 py-8">
      <DashboardStats />
    </section>

    <!-- How it works -->
    <section class="max-w-7xl mx-auto px-4 pb-10">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Cách hoạt động</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          v-for="step in steps"
          :key="step.num"
          class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm"
        >
          <span class="text-3xl font-black text-red-100">{{ step.num }}</span>
          <h3 class="font-semibold text-gray-900 mt-2">{{ step.title }}</h3>
          <p class="text-sm text-gray-500 mt-1">{{ step.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="max-w-7xl mx-auto px-4 pb-16">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Tính năng nổi bật</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div
          v-for="f in features"
          :key="f.title"
          class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm flex gap-4"
        >
          <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0" :class="f.color">
            <component :is="f.icon" class="w-5 h-5" />
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">{{ f.title }}</h3>
            <p class="text-sm text-gray-500 mt-1">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>
