<template>
  <transition name="overlay">
    <div v-if="attraction" class="modal-backdrop" @click.self="close">
      <div class="modal">
        <h3 class="t-h3">여행 계획에 담기</h3>
        <p class="modal-sub t-caption">{{ displayTitle }}</p>

        <p v-if="planLoading" class="state-note">계획을 불러오는 중…</p>

        <template v-else-if="planOptions.length === 0">
          <p class="state-note">아직 만든 여행 계획이 없어요. 먼저 계획을 만들어주세요.</p>
          <div class="modal-actions">
            <BaseButton variant="secondary" @click="close">닫기</BaseButton>
            <BaseButton variant="primary" @click="goToPlans">계획 만들러 가기</BaseButton>
          </div>
        </template>

        <template v-else>
          <div class="form-block">
            <label class="form-label">계획 선택</label>
            <select v-model="selectedPlanId" class="select-input">
              <option v-for="p in planOptions" :key="p.id" :value="p.id">{{ p.title }}</option>
            </select>
          </div>

          <div class="form-block">
            <label class="form-label">며칠째 일정</label>
            <select v-model.number="visitDay" class="select-input">
              <option v-for="d in dayOptions" :key="d" :value="d">{{ d }}일차</option>
            </select>
          </div>

          <p v-if="error" class="form-error">{{ error }}</p>
          <p v-if="message" class="form-ok">{{ message }}</p>

          <div class="modal-actions">
            <BaseButton variant="secondary" @click="close">닫기</BaseButton>
            <BaseButton variant="primary" :disabled="busy" @click="confirm">
              {{ busy ? '담는 중…' : '담기' }}
            </BaseButton>
          </div>
        </template>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import BaseButton from '@/components/common/BaseButton.vue'
import { tripPlanApi } from '@/api/tripPlans'

const props = defineProps({
  // 담을 관광지 { no, title }. null 이면 모달 닫힘.
  attraction: { type: Object, default: null },
})
const emit = defineEmits(['close'])

const router = useRouter()

const planOptions = ref([])
const planLoading = ref(false)
const planLoaded = ref(false)
const selectedPlanId = ref(null)
const visitDay = ref(1)
const busy = ref(false)
const error = ref('')
const message = ref('')

const displayTitle = computed(() => cleanDisplayName(props.attraction?.title))
const selectedPlan = computed(() => planOptions.value.find((p) => p.id === selectedPlanId.value))
const dayOptions = computed(() => {
  const count = planDayCount(selectedPlan.value)
  return Array.from({ length: count }, (_, i) => i + 1)
})

// 모달이 열릴 때(=attraction 이 세팅될 때) 상태 초기화 + 계획 목록 로드
watch(
  () => props.attraction,
  (val) => {
    if (!val) return
    error.value = ''
    message.value = ''
    busy.value = false
    if (planLoaded.value) return
    loadPlans()
  },
)

async function loadPlans() {
  planLoading.value = true
  try {
    planOptions.value = await tripPlanApi.list()
    planLoaded.value = true
    selectedPlanId.value = planOptions.value.length ? planOptions.value[0].id : null
    visitDay.value = dayOptions.value[0] || 1
  } catch (e) {
    error.value = '여행 계획 목록을 불러오지 못했어요.'
  } finally {
    planLoading.value = false
  }
}

async function confirm() {
  if (!selectedPlanId.value || busy.value || !props.attraction) return
  busy.value = true
  error.value = ''
  message.value = ''
  try {
    const days = dayOptions.value
    if (!days.includes(visitDay.value)) visitDay.value = days[0] || 1
    await tripPlanApi.addPlace(selectedPlanId.value, {
      attractionId: props.attraction.no,
      visitDay: visitDay.value || null,
      memo: null,
    })
    message.value = '여행 계획에 담았어요.'
  } catch (e) {
    error.value = e?.response?.data?.message || '계획에 담지 못했어요.'
  } finally {
    busy.value = false
  }
}

function close() {
  emit('close')
}
function goToPlans() {
  router.push('/plans')
}

function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}
function planDayCount(plan) {
  if (!plan || !plan.startDate || !plan.endDate) return 1
  const start = new Date(plan.startDate)
  const end = new Date(plan.endDate)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) return 1
  return Math.floor((end - start) / 86400000) + 1
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: grid;
  place-items: center;
  z-index: 100;
  padding: 16px;
}

.modal {
  background: var(--surface);
  border-radius: var(--r-xl);
  padding: 28px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.2);
}

.modal-sub {
  margin: 6px 0 18px;
  color: var(--teal);
  font-weight: 600;
}

.state-note {
  padding: 16px 0;
  text-align: center;
  color: var(--ink-soft);
  font-size: 14px;
}

.form-block {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 8px;
}

.select-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line-2);
  border-radius: 10px;
  font-size: 14px;
  background: var(--bg-soft);
}

.form-error {
  font-size: 13px;
  color: var(--danger);
  margin-bottom: 8px;
}

.form-ok {
  font-size: 13px;
  color: var(--teal-ink);
  margin-bottom: 8px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.modal-actions :deep(button) {
  flex: 1;
}

@media (max-width: 640px) {
  .modal {
    width: min(94vw, 420px);
    padding: 22px 20px;
    max-height: 88vh;
    overflow-y: auto;
  }
  .select-input {
    padding: 12px;
  }
}
</style>
