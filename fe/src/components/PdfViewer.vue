<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ChevronLeft, ChevronRight, ZoomIn, ZoomOut, Maximize2 } from 'lucide-vue-next'

// Dynamic import of pdfjs-dist to avoid SSR issues
let pdfjsLib: typeof import('pdfjs-dist') | null = null

const props = defineProps<{
  pdfUrl: string
  highlightPage?: number
}>()

const canvasRef   = ref<HTMLCanvasElement | null>(null)
const containerRef = ref<HTMLDivElement | null>(null)
const currentPage = ref(1)
const totalPages  = ref(0)
const scale       = ref(1.2)
const isLoading   = ref(true)
const error       = ref('')
let pdfDoc: import('pdfjs-dist').PDFDocumentProxy | null = null
let renderTask: import('pdfjs-dist').RenderTask | null = null

async function loadPdfJs() {
  if (!pdfjsLib) {
    pdfjsLib = await import('pdfjs-dist')
    pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
      'pdfjs-dist/build/pdf.worker.mjs',
      import.meta.url
    ).toString()
  }
  return pdfjsLib
}

async function loadPdf() {
  if (!props.pdfUrl) return
  isLoading.value = true
  error.value     = ''
  try {
    const lib = await loadPdfJs()
    pdfDoc = await lib.getDocument(props.pdfUrl).promise
    totalPages.value = pdfDoc.numPages
    currentPage.value = props.highlightPage
      ? Math.min(props.highlightPage, totalPages.value)
      : 1
    await renderPage(currentPage.value)
  } catch (e) {
    error.value = 'Không thể tải PDF. Vui lòng thử lại.'
    console.error(e)
  } finally {
    isLoading.value = false
  }
}

async function renderPage(pageNum: number) {
  if (!pdfDoc || !canvasRef.value) return
  if (renderTask) { renderTask.cancel(); renderTask = null }

  const page     = await pdfDoc.getPage(pageNum)
  const viewport = page.getViewport({ scale: scale.value })
  const canvas   = canvasRef.value
  const ctx      = canvas.getContext('2d')!

  canvas.height = viewport.height
  canvas.width  = viewport.width

  renderTask = page.render({ canvasContext: ctx, viewport })
  try {
    await renderTask.promise
  } catch (e: unknown) {
    // RenderingCancelledException is expected on rapid navigation
    if ((e as { name?: string })?.name !== 'RenderingCancelledException') console.error(e)
  }
}

async function goToPage(n: number) {
  if (n < 1 || n > totalPages.value) return
  currentPage.value = n
  await renderPage(n)
}

async function zoomIn()  { scale.value = Math.min(scale.value + 0.25, 3);    await renderPage(currentPage.value) }
async function zoomOut() { scale.value = Math.max(scale.value - 0.25, 0.5);  await renderPage(currentPage.value) }

watch(() => props.pdfUrl,         loadPdf)
watch(() => props.highlightPage,  (p) => p && goToPage(p))
onMounted(loadPdf)
onBeforeUnmount(() => { renderTask?.cancel(); pdfDoc?.destroy() })
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden flex flex-col">

    <!-- Toolbar -->
    <div class="flex items-center justify-between px-4 py-2.5 border-b border-gray-100 bg-gray-50">
      <div class="flex items-center gap-1">
        <button
          :disabled="currentPage <= 1"
          @click="goToPage(currentPage - 1)"
          class="p-1.5 rounded-lg hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
          aria-label="Trang trước"
        >
          <ChevronLeft class="w-4 h-4 text-gray-600" />
        </button>
        <span class="text-sm text-gray-600 min-w-[80px] text-center">
          {{ currentPage }} / {{ totalPages }}
        </span>
        <button
          :disabled="currentPage >= totalPages"
          @click="goToPage(currentPage + 1)"
          class="p-1.5 rounded-lg hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
          aria-label="Trang sau"
        >
          <ChevronRight class="w-4 h-4 text-gray-600" />
        </button>
      </div>

      <div class="flex items-center gap-1">
        <button @click="zoomOut" class="p-1.5 rounded-lg hover:bg-gray-200 transition-colors" aria-label="Thu nhỏ">
          <ZoomOut class="w-4 h-4 text-gray-600" />
        </button>
        <span class="text-xs text-gray-500 w-10 text-center">{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomIn" class="p-1.5 rounded-lg hover:bg-gray-200 transition-colors" aria-label="Phóng to">
          <ZoomIn class="w-4 h-4 text-gray-600" />
        </button>
      </div>
    </div>

    <!-- Canvas area -->
    <div ref="containerRef" class="overflow-auto flex-1 bg-gray-100 p-4 flex justify-center min-h-[500px]">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center gap-3 text-gray-400 w-full">
        <div class="w-8 h-8 border-2 border-red-300 border-t-red-600 rounded-full animate-spin" />
        <p class="text-sm">Đang tải PDF...</p>
      </div>
      <!-- Error -->
      <div v-else-if="error" class="flex items-center justify-center text-red-500 text-sm w-full">
        {{ error }}
      </div>
      <!-- PDF canvas -->
      <canvas
        v-show="!isLoading && !error"
        ref="canvasRef"
        class="shadow-lg max-w-full"
        style="border-radius: 4px;"
      />
    </div>
  </div>
</template>
