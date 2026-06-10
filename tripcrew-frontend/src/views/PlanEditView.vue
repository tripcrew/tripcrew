<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container plan-edit-layout">
      <p v-if="loading" class="state-msg">불러오는 중…</p>
      <p v-else-if="loadError" class="state-msg state-msg--error">{{ loadError }}</p>

      <template v-else>
        <header class="plan-header">
          <div>
            <div class="status-row">
              <span class="status-chip status--active">계획</span>
              <span class="t-mono muted">v.{{ form.version }}</span>
              <span v-if="saveMsg" class="autosave">● {{ saveMsg }}</span>
            </div>
            <h1 class="t-h1">{{ form.title || '제목 없음' }}</h1>
            <p class="t-caption muted">{{ dateLabel }}</p>
          </div>

          <div class="header-right">
            <BaseButton variant="ghost" @click="$router.push('/plans')">← 목록</BaseButton>
            <BaseButton variant="secondary" :disabled="deleting" @click="removePlan">삭제</BaseButton>
            <BaseButton variant="primary" :disabled="saving" @click="save">
              {{ saving ? '저장 중…' : '저장' }}
            </BaseButton>
          </div>
        </header>

        <!-- F03 편집 폼 -->
        <section class="edit-card">
          <div class="field">
            <label>제목</label>
            <input v-model="form.title" type="text" maxlength="150" placeholder="여행 제목" />
          </div>

          <div class="field">
            <label>설명</label>
            <textarea v-model="form.description" rows="4" placeholder="이번 여행에 대한 메모"></textarea>
          </div>

          <div class="field-row">
            <div class="field">
              <label>시작일</label>
              <input v-model="form.startDate" type="date" />
            </div>
            <div class="field">
              <label>종료일</label>
              <input v-model="form.endDate" type="date" />
            </div>
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>
        </section>

        <!-- F04/F06 미리보기 (아직 미연동) -->
        <div class="preview-note">
          아래 일정·지도·동선 최적화는 <strong>디자인 미리보기</strong>입니다 — F04(동선)·F06(공동편집) 연동 예정.
        </div>

        <div class="plan-grid is-preview">
          <section class="plan-main">
            <div class="day-tabs">
              <button v-for="d in days" :key="d.id" :class="['day-tab', { active: d.active }]">
                <strong>{{ d.label }}</strong>
                <span class="t-caption">{{ d.date }}</span>
              </button>
              <button class="day-tab day-tab--add">+ 일자 추가</button>
            </div>

            <div class="timeline">
              <div v-for="(item, i) in items" :key="item.id" class="timeline-row">
                <div class="time-col">
                  <strong>{{ item.start }}</strong>
                  <span class="t-caption">{{ item.end }}</span>
                </div>
                <div class="dot-col">
                  <div class="time-dot">{{ i + 1 }}</div>
                  <div v-if="i < items.length - 1" class="time-line"></div>
                </div>
                <div class="item-col">
                  <div class="item-card">
                    <div class="item-body">
                      <h3>{{ item.name }}</h3>
                      <p class="t-caption">{{ item.region }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <aside class="plan-map">
            <div class="map-canvas"><div class="map-grad"></div></div>
            <div class="map-info">
              <h4>동선 미리보기</h4>
              <p class="t-mono">F04 연동 예정</p>
            </div>
          </aside>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { tripPlanApi } from '@/api/tripPlans'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const deleting = ref(false)
const formError = ref('')
const saveMsg = ref('')

const form = ref({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  version: 0,
})

const dateLabel = computed(() => {
  const { startDate, endDate } = form.value
  if (!startDate && !endDate) return '날짜 미정'
  if (startDate && endDate) return `${startDate} — ${endDate}`
  return startDate || endDate
})

function fill(plan) {
  form.value = {
    title: plan.title ?? '',
    description: plan.description ?? '',
    startDate: plan.startDate ?? '',
    endDate: plan.endDate ?? '',
    version: plan.version,
  }
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    fill(await tripPlanApi.get(id))
  } catch (e) {
    loadError.value = e.response?.status === 404
      ? '여행계획을 찾을 수 없습니다.'
      : '계획을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function save() {
  if (saving.value) return
  formError.value = ''
  saveMsg.value = ''
  saving.value = true
  try {
    const updated = await tripPlanApi.update(id, {
      title: form.value.title,
      description: form.value.description || null,
      startDate: form.value.startDate || null,
      endDate: form.value.endDate || null,
      version: form.value.version,
    })
    fill(updated)
    saveMsg.value = '저장됨'
  } catch (e) {
    const status = e.response?.status
    if (status === 409) {
      // 낙관적 락 충돌: 다른 사용자가 먼저 수정 → 최신본으로 재동기화
      formError.value = '다른 사용자가 먼저 수정했습니다. 최신 내용을 다시 불러왔어요.'
      await load()
    } else if (status === 400) {
      formError.value = e.response?.data?.message || '입력값을 확인해 주세요.'
    } else {
      formError.value = '저장에 실패했습니다.'
    }
  } finally {
    saving.value = false
  }
}

async function removePlan() {
  if (deleting.value) return
  if (!window.confirm('이 여행계획을 삭제할까요?')) return
  deleting.value = true
  try {
    await tripPlanApi.remove(id)
    router.push('/plans')
  } catch (e) {
    formError.value = '삭제에 실패했습니다.'
    deleting.value = false
  }
}

// --- 아래는 F04/F06 디자인 미리보기용 정적 데이터 (미연동) ---
const days = [
  { id: 1, label: 'Day 1', date: '미정', active: true },
]
const items = [
  { id: 1, start: '09:00', end: '~10:30', name: '일정 예시', region: 'F04에서 장소 추가 예정' },
]

onMounted(load)
</script>

<style scoped>
.plan-edit-layout {
  padding: 32px var(--space-6) 80px;
}

.state-msg {
  padding: 60px 0;
  text-align: center;
  color: var(--ink-soft);
}
.state-msg--error { color: var(--coral); }

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.status-chip {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}
.status--active { background: var(--teal); color: white; }

.autosave { color: var(--success); font-weight: 500; font-size: 13px; }
.muted { color: var(--ink-soft); }

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 편집 폼 */
.edit-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 28px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
}

.field input,
.field textarea {
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.15s;
}

.field input:focus,
.field textarea:focus {
  border-color: var(--teal);
}

.field textarea { resize: vertical; }

.field-row {
  display: flex;
  gap: 16px;
}

.form-error {
  color: var(--coral);
  font-size: 13px;
}

/* 미리보기 배너 */
.preview-note {
  background: var(--bg-soft);
  border: 1px dashed var(--line-2);
  border-radius: 10px;
  padding: 12px 16px;
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}

.is-preview {
  opacity: 0.6;
  pointer-events: none;
}

/* Plan grid (preview) */
.plan-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
  align-items: start;
}

.plan-main {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 24px;
}

.day-tabs {
  display: flex;
  gap: 6px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 20px;
}

.day-tab {
  padding: 10px 18px;
  background: var(--bg-soft);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.day-tab strong { font-size: 14px; font-weight: 700; }
.day-tab.active { background: var(--teal); color: white; }
.day-tab.active .t-caption { color: rgba(255,255,255,0.8); }

.day-tab--add {
  background: transparent;
  border: 1px dashed var(--line-2);
  color: var(--ink-soft);
  font-size: 13px;
  align-items: center;
  justify-content: center;
  flex-direction: row;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-row {
  display: grid;
  grid-template-columns: 80px 32px 1fr;
  gap: 12px;
}

.time-col {
  text-align: right;
  padding-top: 14px;
  display: flex;
  flex-direction: column;
}

.time-col strong {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 700;
}

.time-col .t-caption { font-size: 11px; }

.dot-col {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time-dot {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: var(--teal);
  color: white;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 12px;
  margin-top: 12px;
}

.time-line {
  flex: 1;
  width: 2px;
  background: var(--line-2);
  margin: 4px 0;
  min-height: 30px;
}

.item-col { display: flex; flex-direction: column; }

.item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 12px;
}

.item-body { flex: 1; }
.item-body h3 { font-size: 15px; font-weight: 700; margin-bottom: 2px; }

.plan-map {
  position: sticky;
  top: 88px;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  overflow: hidden;
  height: 320px;
  display: flex;
  flex-direction: column;
}

.map-canvas {
  flex: 1;
  position: relative;
  background: var(--bg-2);
  overflow: hidden;
}

.map-grad {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 30% 40%, var(--teal-soft) 0%, transparent 50%),
    radial-gradient(circle at 70% 70%, var(--coral-tint) 0%, transparent 50%);
}

.map-info {
  padding: 14px 18px;
  background: white;
  border-top: 1px solid var(--line);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-info h4 { font-size: 14px; font-weight: 700; }
</style>
