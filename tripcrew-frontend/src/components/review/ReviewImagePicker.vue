<template>
  <div class="image-picker">
    <div class="picker-grid">
      <div v-for="entry in modelValue" :key="entry.id" class="picker-thumb">
        <img :src="entry.preview" alt="첨부 미리보기" />
        <button type="button" class="remove-btn" aria-label="이미지 제거" @click="remove(entry)">×</button>
      </div>

      <label v-if="modelValue.length < max" class="add-tile">
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          multiple
          hidden
          @change="onPick"
        />
        <span class="add-icon">+</span>
        <span class="add-text">사진 추가</span>
      </label>
    </div>

    <div class="picker-foot">
      <span class="t-caption">JPG·PNG·WEBP·GIF · 최대 {{ max }}장 · 한 장당 10MB</span>
      <span class="t-mono">{{ modelValue.length }} / {{ max }}</span>
    </div>
    <p v-if="error" class="picker-error">{{ error }}</p>
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { toAssetUrl } from '@/api/http'

/**
 * 후기 이미지 선택기. v-model 은 엔트리 배열:
 *   { id, file: File|null, url: string|null, preview }
 * - 기존 이미지(수정 시): { file: null, url: '/uploads/...', preview: 절대URL }
 * - 새로 고른 파일: { file: File, url: null, preview: blob URL }
 * 업로드는 부모가 제출 시점에 처리한다(파일 엔트리만 업로드 → url 로 치환).
 */
const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 5 },
})
const emit = defineEmits(['update:modelValue'])

const MAX_BYTES = 10 * 1024 * 1024
const ALLOWED = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

const error = ref('')
let seq = 0
const objectUrls = []

function makePreviewFromFile(file) {
  const url = URL.createObjectURL(file)
  objectUrls.push(url)
  return url
}

function onPick(e) {
  error.value = ''
  const picked = Array.from(e.target.files || [])
  e.target.value = '' // 같은 파일 다시 선택 가능하도록 초기화

  const room = props.max - props.modelValue.length
  if (room <= 0) return

  const next = [...props.modelValue]
  for (const file of picked) {
    if (next.length - props.modelValue.length >= room) {
      error.value = `이미지는 최대 ${props.max}장까지 첨부할 수 있어요.`
      break
    }
    if (!ALLOWED.includes(file.type)) {
      error.value = '이미지 파일(JPG·PNG·WEBP·GIF)만 첨부할 수 있어요.'
      continue
    }
    if (file.size > MAX_BYTES) {
      error.value = '한 장당 10MB 이하만 첨부할 수 있어요.'
      continue
    }
    next.push({ id: `f${seq++}`, file, url: null, preview: makePreviewFromFile(file) })
  }
  emit('update:modelValue', next)
}

function remove(entry) {
  emit(
    'update:modelValue',
    props.modelValue.filter((x) => x.id !== entry.id),
  )
}

/** 부모가 기존 이미지(수정 모드)를 초기 엔트리로 만들 때 쓰는 헬퍼. */
function urlEntry(url) {
  return { id: `u${seq++}`, file: null, url, preview: toAssetUrl(url) }
}
defineExpose({ urlEntry })

onBeforeUnmount(() => objectUrls.forEach((u) => URL.revokeObjectURL(u)))
</script>

<style scoped>
.image-picker {
  margin-top: 4px;
}

.picker-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.picker-thumb {
  position: relative;
  width: 84px;
  height: 84px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--line);
}

.picker-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  font-size: 16px;
  line-height: 1;
  color: white;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 50%;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.add-tile {
  width: 84px;
  height: 84px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border: 1px dashed var(--line-2);
  border-radius: 10px;
  background: var(--bg-soft);
  color: var(--ink-soft);
  cursor: pointer;
  transition: all 0.15s;
}

.add-tile:hover {
  border-color: var(--teal);
  color: var(--teal);
  background: var(--teal-tint);
}

.add-icon {
  font-size: 22px;
  line-height: 1;
}

.add-text {
  font-size: 11px;
  font-weight: 600;
}

.picker-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-soft);
}

.picker-foot .t-mono {
  color: var(--muted);
}

.picker-error {
  margin-top: 8px;
  font-size: 13px;
  color: var(--danger);
}
</style>
