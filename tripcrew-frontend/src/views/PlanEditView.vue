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

        <div class="plan-grid">
          <section class="plan-main">
            <div class="day-navigator">
              <button
                type="button"
                class="day-nav-btn"
                :disabled="selectedDay !== null && selectedDay <= 1"
                @click="goToPreviousDay"
              >
                ←
              </button>

              <div class="day-current">
                <strong>{{ selectedDay === null ? '보관함' : `Day ${selectedDay} / ${dayCount}` }}</strong>
                <span class="t-caption">{{ selectedDay === null ? '미배치 장소' : selectedDayDate }}</span>
              </div>

              <button
                type="button"
                class="day-nav-btn"
                :disabled="selectedDay !== null && selectedDay >= dayCount"
                @click="goToNextDay"
              >
                →
              </button>

              <input
                class="day-jump"
                v-model.number="dayJumpValue"
                type="number"
                min="1"
                :max="dayCount"
                @keyup.enter="goToDay(dayJumpValue)"
              />
              <button type="button" class="day-go-btn" @click="goToDay(dayJumpValue)">이동</button>

              <button
                type="button"
                :class="['storage-tab', { active: selectedDay === null }]"
                @click="selectedDay = null"
              >
                보관함
              </button>
            </div>

            <form class="place-form" @submit.prevent="addCustomPlace">
              <div class="field">
                <label>직접 추가</label>
                <input v-model.trim="placeForm.name" type="text" maxlength="255" placeholder="장소 이름" />
              </div>
              <div class="field field--small">
                <label>Day</label>
                <div class="day-field-control">
                  <input
                    v-model.number="placeForm.visitDay"
                    type="number"
                    min="1"
                    :max="dayCount"
                    :disabled="placeForm.visitDay === null"
                  />
                  <button type="button" class="storage-inline" @click="togglePlaceFormStorage">
                    {{ placeForm.visitDay === null ? 'Day로' : '보관' }}
                  </button>
                </div>
              </div>
              <div class="field field--small">
                <label>위도</label>
                <input v-model="placeForm.latitude" type="number" step="0.000001" placeholder="선택" />
              </div>
              <div class="field field--small">
                <label>경도</label>
                <input v-model="placeForm.longitude" type="number" step="0.000001" placeholder="선택" />
              </div>
              <BaseButton variant="secondary" :disabled="placeSaving" type="submit">
                {{ placeSaving ? '추가 중…' : '추가' }}
              </BaseButton>
            </form>
            <p v-if="placeMsg" class="place-msg">{{ placeMsg }}</p>
            <p v-if="placeError" class="form-error">{{ placeError }}</p>

            <div class="timeline">
              <div v-if="visiblePlaces.length === 0" class="empty-places">
                {{ selectedDay === null ? '보관함에 장소가 없습니다.' : `Day ${selectedDay}에 추가된 장소가 없습니다.` }}
              </div>
              <div v-for="(item, i) in visiblePlaces" :key="item.id" class="timeline-row">
                <div class="time-col">
                  <strong>{{ String(i + 1).padStart(2, '0') }}</strong>
                  <span class="t-caption">방문</span>
                </div>
                <div class="dot-col">
                  <div class="time-dot">{{ i + 1 }}</div>
                  <div v-if="i < visiblePlaces.length - 1" class="time-line"></div>
                </div>
                <div class="item-col">
                  <div class="item-card">
                    <div class="item-body">
                      <h3>{{ cleanDisplayName(item.name) }}</h3>
                      <p class="t-caption">
                        {{ item.attractionId ? '관광지' : '직접 추가' }}
                        <span v-if="item.memo"> · {{ item.memo }}</span>
                      </p>
                    </div>
                    <div class="item-actions">
                      <button type="button" class="mini-btn" @click="movePlace(item, 'up')" :disabled="i === 0">↑</button>
                      <button type="button" class="mini-btn" @click="movePlace(item, 'down')" :disabled="i === visiblePlaces.length - 1">↓</button>
                      <div v-if="selectedDay === null" class="assign-control">
                        <input
                          type="number"
                          min="1"
                          :max="dayCount"
                          :value="storageTargets[item.id] ?? defaultDay"
                          @input="storageTargets[item.id] = clampDay($event.target.value)"
                        />
                        <button type="button" class="mini-btn" @click="assignStoredPlace(item)">
                          배치
                        </button>
                      </div>
                      <button v-else type="button" class="mini-btn" @click="togglePlaceSchedule(item)">
                        보관
                      </button>
                      <button type="button" class="mini-btn mini-btn--danger" @click="removePlace(item)">삭제</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <aside class="plan-map">
            <div class="map-canvas"><div class="map-grad"></div></div>
            <div class="map-info">
              <h4>장소 {{ places.length }}개</h4>
              <p class="t-mono">{{ selectedDay === null ? 'Storage' : `Day ${selectedDay}` }}</p>
            </div>
          </aside>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
const places = ref([])
const placeSaving = ref(false)
const placeError = ref('')
const placeMsg = ref('')
const selectedDay = ref(1)
const dayJumpValue = ref(1)
const storageTargets = ref({})
const placeForm = ref({
  name: '',
  latitude: '',
  longitude: '',
  visitDay: 1,
  memo: '',
})

const form = ref({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  version: 0,
})

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

const dateLabel = computed(() => {
  const { startDate, endDate } = form.value
  if (!startDate && !endDate) return '날짜 미정'
  if (startDate && endDate) return `${formatPlanDate(startDate)} - ${formatPlanEndDate(endDate)} · ${dayCount.value}일`
  return formatPlanDate(startDate || endDate)
})

const dayCount = computed(() => {
  if (!form.value.startDate || !form.value.endDate) return 1
  const start = parseDate(form.value.startDate)
  const end = parseDate(form.value.endDate)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) return 1
  return Math.floor((end - start) / 86400000) + 1
})

const selectedDayDate = computed(() =>
  selectedDay.value === null ? '미배치 장소' : formatDayDate(selectedDay.value),
)

const defaultDay = computed(() => selectedDay.value ?? 1)
const visiblePlaces = computed(() =>
  places.value
    .filter((place) => (selectedDay.value === null ? place.visitDay == null : place.visitDay === selectedDay.value))
    .slice()
    .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0) || a.id - b.id),
)

watch(dayCount, (maxDay) => {
  if (selectedDay.value != null && selectedDay.value > maxDay) {
    selectedDay.value = maxDay
  }
  if (placeForm.value.visitDay != null && placeForm.value.visitDay > maxDay) {
    placeForm.value.visitDay = maxDay
  }
  dayJumpValue.value = clampDay(dayJumpValue.value)
})

watch(selectedDay, (day) => {
  if (day !== null) {
    dayJumpValue.value = day
  }
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

function goToPreviousDay() {
  if (selectedDay.value === null) {
    goToDay(dayJumpValue.value)
    return
  }
  if (selectedDay.value <= 1) return
  selectedDay.value -= 1
}

function goToNextDay() {
  if (selectedDay.value === null) {
    goToDay(dayJumpValue.value)
    return
  }
  if (selectedDay.value >= dayCount.value) return
  selectedDay.value += 1
}

function goToDay(value) {
  const day = clampDay(value)
  dayJumpValue.value = day
  selectedDay.value = day
}

function clampDay(value) {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return 1
  return Math.min(Math.max(Math.trunc(numeric), 1), dayCount.value)
}

function formatDayDate(day) {
  const start = form.value.startDate ? parseDate(form.value.startDate) : null
  if (!start || Number.isNaN(start.getTime())) return '미정'
  return formatReadableDate(addDays(start, day - 1))
}

function togglePlaceFormStorage() {
  placeForm.value.visitDay = placeForm.value.visitDay === null ? defaultDay.value : null
}

function parseDate(value) {
  if (!value) return new Date(Number.NaN)
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function addDays(date, amount) {
  const next = new Date(date)
  next.setDate(next.getDate() + amount)
  return next
}

function formatPlanDate(value) {
  const date = value instanceof Date ? value : parseDate(value)
  if (Number.isNaN(date.getTime())) return '날짜 미정'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
}

function formatPlanEndDate(value) {
  const date = value instanceof Date ? value : parseDate(value)
  if (Number.isNaN(date.getTime())) return '날짜 미정'
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}.${day}`
}

function formatReadableDate(value) {
  const date = value instanceof Date ? value : parseDate(value)
  if (Number.isNaN(date.getTime())) return '미정'
  return `${date.getMonth() + 1}월 ${date.getDate()}일 ${WEEKDAYS[date.getDay()]}요일`
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const [plan, placeList] = await Promise.all([
      tripPlanApi.get(id),
      tripPlanApi.listPlaces(id),
    ])
    fill(plan)
    places.value = placeList
  } catch (e) {
    loadError.value = e.response?.status === 404
      ? '여행계획을 찾을 수 없습니다.'
      : '계획을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function loadPlaces() {
  places.value = await tripPlanApi.listPlaces(id)
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

async function addCustomPlace() {
  if (placeSaving.value) return
  placeError.value = ''
  placeMsg.value = ''
  if (!placeForm.value.name) {
    placeError.value = '장소 이름을 입력해 주세요.'
    return
  }

  placeSaving.value = true
  try {
    await tripPlanApi.addPlace(id, {
      name: placeForm.value.name,
      latitude: placeForm.value.latitude === '' ? null : placeForm.value.latitude,
      longitude: placeForm.value.longitude === '' ? null : placeForm.value.longitude,
      visitDay: placeForm.value.visitDay || null,
      memo: placeForm.value.memo || null,
    })
    placeForm.value.name = ''
    placeForm.value.latitude = ''
    placeForm.value.longitude = ''
    await loadPlaces()
    placeMsg.value = '장소를 추가했습니다.'
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 추가에 실패했습니다.'
  } finally {
    placeSaving.value = false
  }
}

async function togglePlaceSchedule(place) {
  placeError.value = ''
  placeMsg.value = ''
  try {
    await tripPlanApi.schedulePlace(id, place.id, {
      visitDay: place.visitDay ? null : defaultDay.value,
      orderIndex: null,
    })
    await loadPlaces()
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 배치 변경에 실패했습니다.'
  }
}

async function assignStoredPlace(place) {
  const targetDay = clampDay(storageTargets.value[place.id] ?? defaultDay.value)
  placeError.value = ''
  placeMsg.value = ''
  try {
    await tripPlanApi.schedulePlace(id, place.id, {
      visitDay: targetDay,
      orderIndex: null,
    })
    delete storageTargets.value[place.id]
    selectedDay.value = targetDay
    await loadPlaces()
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 배치에 실패했습니다.'
  }
}

async function movePlace(place, direction) {
  const list = visiblePlaces.value.map((item) => item.id)
  const index = list.indexOf(place.id)
  const nextIndex = direction === 'up' ? index - 1 : index + 1
  if (nextIndex < 0 || nextIndex >= list.length) return
  ;[list[index], list[nextIndex]] = [list[nextIndex], list[index]]

  placeError.value = ''
  try {
    places.value = await tripPlanApi.reorderPlaces(id, {
      visitDay: selectedDay.value,
      placeIds: list,
    })
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 순서 변경에 실패했습니다.'
  }
}

async function removePlace(place) {
  if (!window.confirm(`"${cleanDisplayName(place.name)}" 장소를 삭제할까요?`)) return
  placeError.value = ''
  try {
    await tripPlanApi.removePlace(id, place.id)
    await loadPlaces()
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 삭제에 실패했습니다.'
  }
}

function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}

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
.field select,
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
.field select:focus,
.field textarea:focus {
  border-color: var(--teal);
}

.field textarea { resize: vertical; }

.field-row {
  display: flex;
  gap: 16px;
}

.form-error,
.place-msg {
  color: var(--coral);
  font-size: 13px;
}

.place-msg { color: var(--success); }

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

.day-navigator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 20px;
}

.day-nav-btn {
  width: 38px;
  height: 38px;
  border: 1px solid var(--line-2);
  border-radius: 10px;
  background: white;
  color: var(--ink-3);
  font-size: 16px;
  font-weight: 800;
}

.day-nav-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.day-current {
  min-width: 150px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.day-current strong {
  font-size: 16px;
  font-weight: 800;
}

.day-jump {
  width: 92px;
  height: 38px;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 0 12px;
  background: white;
  color: var(--ink);
  font: inherit;
}

.day-go-btn,
.storage-tab {
  height: 38px;
  padding: 0 14px;
  background: var(--bg-soft);
  border: 1px solid transparent;
  border-radius: 10px;
  color: var(--ink-3);
  font-size: 13px;
  font-weight: 800;
}

.day-go-btn {
  background: white;
  border-color: var(--line-2);
}

.day-go-btn:hover,
.storage-tab:hover { border-color: var(--line-2); }
.storage-tab.active { background: var(--teal); color: white; }

.place-form {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 142px 112px 112px auto;
  gap: 12px;
  align-items: end;
  padding: 16px;
  margin-bottom: 18px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
}

.field--small {
  flex: none;
}

.day-field-control {
  display: grid;
  grid-template-columns: minmax(54px, 1fr) 58px;
}

.day-field-control input {
  border-radius: 10px 0 0 10px;
  border-right: 0;
  min-width: 0;
}

.field--small .storage-inline {
  height: 40px;
  border: 1px solid var(--line-2);
  border-radius: 0 10px 10px 0;
  background: var(--bg-soft);
  color: var(--ink-3);
  font-size: 12px;
  font-weight: 800;
}

.field--small input:disabled {
  background: var(--bg-2);
  color: var(--ink-soft);
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-places {
  padding: 32px 16px;
  text-align: center;
  color: var(--ink-soft);
  background: var(--bg-soft);
  border: 1px dashed var(--line-2);
  border-radius: 12px;
}

.timeline-row {
  display: grid;
  grid-template-columns: 56px 28px 1fr;
  gap: 10px;
}

.time-col {
  text-align: right;
  padding-top: 16px;
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
  width: 26px; height: 26px;
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
  justify-content: space-between;
  gap: 12px;
  min-height: 70px;
  padding: 14px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.item-body { flex: 1; }
.item-body h3 { font-size: 15px; font-weight: 700; margin-bottom: 2px; }

.item-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.assign-control {
  display: grid;
  grid-template-columns: 54px auto;
}

.assign-control input {
  width: 54px;
  height: 28px;
  min-width: 0;
  border: 1px solid var(--line-2);
  border-right: 0;
  border-radius: 8px 0 0 8px;
  padding: 0 8px;
  background: white;
  color: var(--ink);
  font: inherit;
  font-size: 12px;
  font-weight: 700;
}

.assign-control .mini-btn {
  border-radius: 0 8px 8px 0;
}

.mini-btn {
  min-width: 32px;
  height: 28px;
  padding: 0 9px;
  border: 1px solid var(--line-2);
  border-radius: 8px;
  background: white;
  color: var(--ink-3);
  font-size: 12px;
  font-weight: 700;
}

.mini-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.mini-btn--danger { color: var(--coral); }

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

@media (max-width: 1000px) {
  .plan-grid { grid-template-columns: 1fr; }
  .plan-map { position: static; }
  .place-form { grid-template-columns: minmax(220px, 1fr) 142px; }
}

@media (max-width: 700px) {
  .plan-header,
  .field-row,
  .item-card {
    flex-direction: column;
    align-items: stretch;
  }

  .header-right,
  .item-actions {
    justify-content: flex-start;
  }

  .place-form,
  .timeline-row,
  .day-navigator {
    grid-template-columns: 1fr;
  }

  .day-navigator {
    display: grid;
  }

  .day-nav-btn,
  .day-jump,
  .day-go-btn,
  .storage-tab {
    width: 100%;
  }

  .day-field-control {
    grid-template-columns: 1fr 64px;
  }

  .time-col,
  .dot-col {
    display: none;
  }
}
</style>
