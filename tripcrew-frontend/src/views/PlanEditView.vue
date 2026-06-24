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
            <div v-if="myRole && roster.length" class="presence-cluster" :title="presenceNames">
              <div class="presence-avatars">
                <span
                  v-for="u in roster"
                  :key="u.userId"
                  class="presence-avatar"
                  :style="{ background: avatarColor(u.userId) }"
                >{{ avatarLetter(u.nickname) }}</span>
              </div>
              <span class="presence-label">
                <span class="presence-dot" :class="{ 'presence-dot--off': !connected }"></span>
                {{ roster.length }}명 편집 중
              </span>
            </div>
            <BaseButton variant="ghost" @click="$router.push('/plans')">← 목록</BaseButton>
            <BaseButton v-if="myRole" variant="ghost" @click="openShare">공유</BaseButton>
            <BaseButton v-if="isOwner" variant="secondary" :disabled="deleting" @click="removePlan">삭제</BaseButton>
            <BaseButton v-if="canEdit" variant="primary" :disabled="saving" @click="save">
              {{ saving ? '저장 중…' : '저장' }}
            </BaseButton>
          </div>
        </header>

        <p v-if="myRole && !canEdit" class="readonly-banner">
          읽기 전용(뷰어)입니다. 편집하려면 소유자에게 권한을 요청하세요.
        </p>

        <!-- F03 편집 폼 -->
        <section class="edit-card">
          <!-- F06 P2b — 다른 사용자가 메타를 저장했는데 내가 편집 중이면, 덮어쓰지 않고 선택권을 준다 -->
          <div v-if="metaConflict.open" class="meta-conflict">
            <span class="meta-conflict__msg">{{ metaConflict.by }}님이 계획 정보를 수정했어요. 내 변경을 유지할까요?</span>
            <div class="meta-conflict__actions">
              <button type="button" class="mini-btn" @click="loadServerMeta">최신 내용 불러오기</button>
              <button type="button" class="mini-btn mini-btn--primary" @click="keepMyMeta">내 변경 유지</button>
            </div>
          </div>

          <div class="field">
            <label>제목</label>
            <input v-model="form.title" type="text" maxlength="150" placeholder="여행 제목" :disabled="!canEdit" @input="touchMeta" />
          </div>

          <div class="field">
            <label>설명</label>
            <textarea v-model="form.description" rows="4" placeholder="이번 여행에 대한 메모" :disabled="!canEdit" @input="touchMeta"></textarea>
          </div>

          <div class="field-row">
            <div class="field">
              <label>시작일</label>
              <input v-model="form.startDate" type="date" min="0001-01-01" max="9999-12-31" :disabled="!canEdit" @change="touchMeta" />
            </div>
            <div class="field">
              <label>종료일</label>
              <input v-model="form.endDate" type="date" min="0001-01-01" max="9999-12-31" :disabled="!canEdit" @change="touchMeta" />
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

              <button
                v-if="canEdit"
                type="button"
                class="optimize-btn"
                :disabled="selectedDay === null || visiblePlaces.length < 2 || optimizing"
                @click="optimizeRoute"
              >
                {{ optimizing ? '최적화 중…' : '동선 최적화' }}
              </button>
            </div>

            <section v-if="canEdit" class="place-search">
              <form class="place-search__form" @submit.prevent="searchAttractions">
                <div class="field place-search__field">
                  <label>관광지 추가</label>
                  <input
                    v-model.trim="attractionKeyword"
                    type="search"
                    maxlength="100"
                    placeholder="추가할 관광지명을 검색하세요"
                  />
                </div>
                <BaseButton variant="secondary" :disabled="attractionSearching || placeSaving" type="submit">
                  {{ attractionSearching ? '검색 중…' : '검색' }}
                </BaseButton>
              </form>
              <p class="place-search__hint">선택한 관광지의 위치 정보가 일정과 지도에 자동으로 반영됩니다.</p>
              <p v-if="attractionSearchError" class="form-error">{{ attractionSearchError }}</p>

              <div v-if="attractionResults.length" class="attraction-results">
                <button
                  v-for="attraction in attractionResults"
                  :key="attraction.no"
                  type="button"
                  class="attraction-result"
                  :disabled="placeSaving"
                  @click="addAttractionPlace(attraction)"
                >
                  <img v-if="attraction.imageUrl" :src="attraction.imageUrl" :alt="cleanDisplayName(attraction.title)" />
                  <span v-else class="attraction-result__image">🗺️</span>
                  <span class="attraction-result__body">
                    <strong>{{ cleanDisplayName(attraction.title) }}</strong>
                    <small>{{ [attraction.sido, attraction.gugun].filter(Boolean).join(' ') || attraction.address || '지역 정보 없음' }}</small>
                  </span>
                  <span class="attraction-result__add">
                    {{ placeSaving ? '추가 중…' : selectedDay === null ? '보관함 추가' : `Day ${selectedDay} 추가` }}
                  </span>
                </button>
              </div>
              <nav v-if="attractionSearchTotalPages > 1" class="attraction-pagination" aria-label="관광지 검색 결과 페이지">
                <button
                  type="button"
                  :disabled="attractionSearching || attractionSearchPage <= 1"
                  @click="changeAttractionSearchPage(attractionSearchPage - 1)"
                >
                  이전
                </button>
                <button
                  v-for="page in attractionSearchPages"
                  :key="page"
                  type="button"
                  :class="{ active: page === attractionSearchPage }"
                  :disabled="attractionSearching"
                  @click="changeAttractionSearchPage(page)"
                >
                  {{ page }}
                </button>
                <button
                  type="button"
                  :disabled="attractionSearching || attractionSearchPage >= attractionSearchTotalPages"
                  @click="changeAttractionSearchPage(attractionSearchPage + 1)"
                >
                  다음
                </button>
              </nav>
            </section>
            <p v-if="placeMsg" class="place-msg">{{ placeMsg }}</p>
            <p v-if="placeError" class="form-error">{{ placeError }}</p>

            <div class="timeline">
              <div v-if="visiblePlaces.length === 0" class="empty-places">
                <strong>{{ selectedDay === null ? '보관함에 장소가 없습니다.' : `Day ${selectedDay}에 추가된 장소가 없습니다.` }}</strong>
                <span>위에서 관광지를 검색해 {{ selectedDay === null ? '보관함' : `Day ${selectedDay}` }}에 추가해 보세요.</span>
              </div>
              <div
                v-for="(item, i) in visiblePlaces"
                :key="item.id"
                :class="['timeline-row', {
                  'timeline-row--dragging': draggedPlaceId === item.id,
                  'timeline-row--drag-over': dragOverPlaceId === item.id,
                }]"
                :draggable="!placeReordering"
                @dragstart="startPlaceDrag($event, item)"
                @dragover.prevent="dragOverPlace($event, item)"
                @drop.prevent="dropPlace($event, item)"
                @dragend="endPlaceDrag"
              >
                <div class="time-col">
                  <strong>{{ String(i + 1).padStart(2, '0') }}</strong>
                  <span class="t-caption">방문</span>
                </div>
                <div class="dot-col">
                  <div class="time-dot">{{ i + 1 }}</div>
                  <div v-if="i < visiblePlaces.length - 1" class="time-line"></div>
                </div>
                <div class="item-col">
                  <div
                    :class="['item-card', { 'item-card--mappable': hasCoordinates(item) }]"
                    @click="focusMapPlace(item)"
                  >
                    <div class="item-body">
                      <h3>{{ cleanDisplayName(item.name) }}</h3>
                      <p class="t-caption">
                        {{ item.attractionId ? '관광지' : '직접 추가' }}
                        <span v-if="item.memo"> · {{ item.memo }}</span>
                      </p>
                    </div>
                    <div v-if="canEdit" class="item-actions" @click.stop @dragstart.stop>
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
                    <span class="drag-handle" title="드래그해 순서 변경" aria-label="드래그해 순서 변경">↕</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <aside class="plan-map">
            <div ref="mapElement" class="map-canvas">
              <div v-if="mapState !== 'ready'" class="map-fallback">
                <strong>{{ mapFallbackTitle }}</strong>
                <span>{{ mapFallbackText }}</span>
              </div>
              <button
                v-else-if="mapPlaces.length > 1"
                type="button"
                class="map-fit-btn"
                @click="fitMapToPlaces"
              >
                전체 보기
              </button>
              <div v-if="drivingRouteSegments.length > 0" class="map-traffic-legend" aria-label="교통 상황 범례">
                <span><i class="traffic-dot traffic-dot--clear"></i>원활</span>
                <span><i class="traffic-dot traffic-dot--slow"></i>서행</span>
                <span><i class="traffic-dot traffic-dot--busy"></i>혼잡</span>
              </div>
            </div>
            <div class="map-info">
              <h4>{{ selectedMapPlaceName || `장소 ${visiblePlaces.length}개` }}</h4>
              <p class="t-mono">{{ selectedDay === null ? 'Storage' : `Day ${selectedDay}` }}</p>
            </div>
            <section v-if="drivingRouteSummary" class="route-cost-card" aria-label="예상 차량 이동 비용">
              <div class="route-cost-card__head">
                <span>예상 차량 이동 비용</span>
                <small>실시간 교통 반영</small>
              </div>
              <div class="route-cost-card__metrics">
                <span>{{ formatDistance(drivingRouteSummary.distanceMeter) }}</span>
                <span>{{ formatDrivingDuration(drivingRouteSummary.durationMillis) }}</span>
                <span>통행료 {{ formatWon(drivingRouteSummary.tollFare) }}</span>
              </div>
              <div class="route-cost-card__total">
                <span>예상 유류비 포함</span>
                <strong>{{ formatWon(routeCostTotal) }}</strong>
              </div>
              <p>일반 승용차 기준 API 예상값이며 실제 비용과 다를 수 있습니다.</p>
            </section>
          </aside>
        </div>
      </template>
    </main>

    <!-- F06 공동편집 — 공유/멤버 다이얼로그 -->
    <div v-if="shareOpen" class="share-overlay" @click.self="closeShare">
      <section class="share-modal">
        <header class="share-modal__head">
          <h2 class="t-h3">공유 · 멤버</h2>
          <button type="button" class="share-close" @click="closeShare">✕</button>
        </header>

        <p v-if="membersLoading" class="state-msg">불러오는 중…</p>
        <p v-else-if="membersError" class="form-error">{{ membersError }}</p>

        <ul v-else class="member-list">
          <li v-for="member in members" :key="member.userId" class="member-row">
            <div class="member-info">
              <strong>{{ member.email }}</strong>
              <span v-if="member.userId === myUserId" class="member-me">나</span>
              <span class="member-role">{{ roleText(member.role) }}</span>
              <span v-if="member.status === 'PENDING'" class="member-pending">대기중</span>
            </div>
            <div class="member-actions">
              <select
                v-if="isOwner && member.role !== 'OWNER'"
                :value="member.role"
                @change="changeMemberRole(member, $event.target.value)"
              >
                <option value="EDITOR">편집자</option>
                <option value="VIEWER">뷰어</option>
              </select>
              <button
                v-if="isOwner && member.role !== 'OWNER'"
                type="button"
                class="mini-btn mini-btn--danger"
                @click="kickMember(member)"
              >
                내보내기
              </button>
              <button
                v-else-if="!isOwner && member.userId === myUserId"
                type="button"
                class="mini-btn mini-btn--danger"
                @click="kickMember(member)"
              >
                나가기
              </button>
            </div>
          </li>
        </ul>

        <form v-if="isOwner" class="invite-form" @submit.prevent="submitInvite">
          <h3 class="t-caption">멤버 초대</h3>
          <div class="invite-row">
            <input
              v-model.trim="inviteEmail"
              type="email"
              placeholder="초대할 사용자 이메일"
              autocomplete="off"
            />
            <select v-model="inviteRole">
              <option value="EDITOR">편집자</option>
              <option value="VIEWER">뷰어</option>
            </select>
            <BaseButton variant="primary" type="submit" :disabled="inviteBusy">
              {{ inviteBusy ? '초대 중…' : '초대' }}
            </BaseButton>
          </div>
          <p v-if="inviteError" class="form-error">{{ inviteError }}</p>
        </form>
      </section>
    </div>

    <div v-if="optimizePanelVisible" class="optimize-overlay">
      <section class="optimize-modal" role="status" aria-live="polite">
        <div class="optimize-mark">
          <span class="optimize-pulse"></span>
          <svg width="34" height="34" viewBox="0 0 34 34" fill="none" aria-hidden="true">
            <path d="M6 19h7l4-11 5 18 4-9h4" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <h2>{{ optimizePanelTitle }}</h2>
        <p>
          {{ optimizePanelDescription }}
        </p>

        <div class="optimize-progress">
          <div class="optimize-progress__bar" :style="{ width: `${optimizeProgress}%` }"></div>
        </div>

        <div class="optimize-meta">
          <span>{{ optimizeStageLabel }}</span>
          <span>경과 {{ optimizeElapsedLabel }}</span>
        </div>

        <ol class="optimize-steps">
          <li
            v-for="(step, index) in optimizeSteps"
            :key="step"
            :class="{
              done: index < optimizeActiveStep || optimizeCompleted,
              active: index === optimizeActiveStep && !optimizeCompleted && !optimizeFailed,
              failed: optimizeFailed && index === optimizeActiveStep,
            }"
          >
            <span class="step-dot"></span>
            <span>{{ step }}</span>
          </li>
        </ol>
      </section>
    </div>

    <!-- F06 P2a — 공동 편집 실시간 알림 토스트 -->
    <transition name="edit-toast">
      <div v-if="toastVisible" class="edit-toast" role="status" aria-live="polite">{{ toastMsg }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { attractionApi } from '@/api/attractions'
import { tripPlanApi } from '@/api/tripPlans'
import { useAuthStore } from '@/stores/auth'
import { usePresence } from '@/composables/usePresence'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const id = route.params.id

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const deleting = ref(false)
const formError = ref('')
const saveMsg = ref('')
const places = ref([])
const placeSaving = ref(false)
const optimizing = ref(false)
const optimizePanelVisible = ref(false)
const optimizeCompleted = ref(false)
const optimizeFailed = ref(false)
const optimizeElapsedMs = ref(0)
const placeError = ref('')
const placeMsg = ref('')
const attractionKeyword = ref('')
const attractionResults = ref([])
const attractionSearching = ref(false)
const attractionSearchError = ref('')
const attractionSearchPage = ref(0)
const attractionSearchTotalPages = ref(0)
const draggedPlaceId = ref(null)
const dragOverPlaceId = ref(null)
const placeReordering = ref(false)
const selectedDay = ref(1)
const dayJumpValue = ref(1)
const storageTargets = ref({})
const mapElement = ref(null)
const mapState = ref('idle')
const mapInstance = ref(null)
const mapMarkers = ref([])
const routeLines = ref([])
const routeBounds = ref(null)
const drivingRoutePath = ref([])
const drivingRouteSegments = ref([])
const drivingRouteSummary = ref(null)
const selectedMapPlaceName = ref('')
let activeInfoWindow = null

const form = ref({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  version: 0,
})

// F06 공동편집 — 내 역할 + 멤버 공유 다이얼로그
const myRole = ref(null)
const shareOpen = ref(false)
const members = ref([])
const membersLoading = ref(false)
const membersError = ref('')
const inviteEmail = ref('')
const inviteRole = ref('EDITOR')
const inviteBusy = ref(false)
const inviteError = ref('')

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
const DEFAULT_CENTER = { lat: 37.5666103, lng: 126.9783882 }
const OPTIMIZE_ESTIMATE_MS = 2400
let naverMapsScriptPromise = null
let optimizeTimer = null
let optimizeStartedAt = 0
let drivingRouteRequest = 0

const myUserId = computed(() => (auth.user ? auth.user.id : null))
const isOwner = computed(() => myRole.value === 'OWNER')
const canEdit = computed(() => myRole.value === 'OWNER' || myRole.value === 'EDITOR')
const routeCostTotal = computed(() => {
  if (!drivingRouteSummary.value) return 0
  return Number(drivingRouteSummary.value.tollFare || 0) + Number(drivingRouteSummary.value.fuelPrice || 0)
})

// F06 P2a — 실시간 협업: 접속자 프레즌스 + 장소 변경 동기화(broadcast-refetch)
const PRESENCE_PALETTE = ['var(--teal)', 'var(--coral)', 'var(--violet)', 'var(--info)', 'var(--warning)']
const PLACE_ACTION_TEXT = {
  ADDED: '장소를 추가했어요',
  SCHEDULED: '장소 배치를 변경했어요',
  REORDERED: '장소 순서를 변경했어요',
  OPTIMIZED: '동선을 최적화했어요',
  REMOVED: '장소를 삭제했어요',
  SAVED: '저장을 완료했어요',
}
const toastMsg = ref('')
const toastVisible = ref(false)
let toastTimer = null

// F06 P2b — 메타(제목/설명/날짜) 변경 보호.
// metaDirty:   내가 메타를 편집했는데 아직 저장 안 함(미저장 입력).
// metaTouched: 이 세션에서 메타를 한 번이라도 편집/저장함(= 내 기여). 저장해도 유지하고,
//              상대 내용을 받아들일 때만 해제 → 내가 저장한 내용이 남에게 덮어써지는 것도 알려준다.
// metaConflict: 들어온 저장이 내 화면과 다른데 내 기여가 있으면 → 덮어쓰지 않고 선택 배너(plan=서버 최신본).
const metaDirty = ref(false)
const metaTouched = ref(false)
const metaConflict = ref({ open: false, by: '', plan: null })

// 메타 입력 시: 미저장 + 기여 표시
function touchMeta() {
  metaDirty.value = true
  metaTouched.value = true
}

// 들어온 서버 메타가 지금 내 폼과 같은지(빈값 null/'' 정규화). 같으면 충돌이 아니라 그냥 반영.
function metaMatches(plan) {
  const norm = (v) => v ?? ''
  return norm(plan.title) === norm(form.value.title)
    && norm(plan.description) === norm(form.value.description)
    && norm(plan.startDate) === norm(form.value.startDate)
    && norm(plan.endDate) === norm(form.value.endDate)
}

const presenceNames = computed(() =>
  roster.value
    .map((u) => (u.userId === myUserId.value ? `${u.nickname} (나)` : u.nickname))
    .join(', '),
)

function avatarColor(userId) {
  return PRESENCE_PALETTE[(userId || 0) % PRESENCE_PALETTE.length]
}

function avatarLetter(nickname) {
  return nickname ? nickname.charAt(0) : '?'
}

function showToast(message) {
  toastMsg.value = message
  toastVisible.value = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastVisible.value = false
  }, 3600)
}

// 내가 직접 장소를 조작(드래그/순서변경·추가삭제 저장·동선 최적화)하는 동안에는
// 원격 갱신으로 목록을 갈아끼우지 않는다(조작이 어긋나는 것 방지). 끝나면 미뤄둔 갱신을 반영.
const localBusy = computed(
  () => placeReordering.value || placeSaving.value || optimizing.value || draggedPlaceId.value !== null,
)
let pendingRefetch = false
let pendingPlanRefetch = false // SAVED: 장소뿐 아니라 계획 메타(제목/날짜)도 다시 불러와야 함
let savedByNickname = '' // SAVED 알림을 보낸 사람(충돌 배너 문구용)

async function applyRemoteRefetch() {
  const reloadPlan = pendingPlanRefetch
  pendingRefetch = false
  pendingPlanRefetch = false
  try {
    if (reloadPlan) {
      const plan = await tripPlanApi.get(id)
      // 내 기여(편집 중이거나 이미 저장함)가 있고, 들어온 내용이 내 화면과 다르면 → 선택권을 준다(P2b).
      if ((metaDirty.value || metaTouched.value) && !metaMatches(plan)) {
        metaConflict.value = { open: true, by: savedByNickname, plan }
      } else {
        fill(plan) // 상대가 저장한 제목/설명/날짜·version 을 반영
        if (savedByNickname) showToast(`${savedByNickname}님이 ${PLACE_ACTION_TEXT.SAVED}`)
      }
    }
    await loadPlaces()
  } catch {
    // 재조회 실패는 조용히 무시 — 다음 변경/새로고침 때 다시 동기화된다.
  }
}

watch(localBusy, (busy) => {
  if (!busy && (pendingRefetch || pendingPlanRefetch)) applyRemoteRefetch()
})

// 다른 접속자의 변경 알림 → 내용을 다시 불러오고 누가 바꿨는지 토스트로 안내.
async function handlePlaceChange(event) {
  if (!event || event.actorId === myUserId.value) return // 내 변경은 이미 반영됨
  // 내가 내보내진 경우: 접근 권한을 잃었으니 안내와 함께 내 계획 목록으로 이동. 대상이 아니면 무시.
  if (event.action === 'MEMBER_REMOVED') {
    if (event.targetUserId === myUserId.value) {
      router.push({ path: '/plans', query: { left: 'removed' } })
    }
    return
  }
  if (event.action === 'SAVED') {
    savedByNickname = event.actorNickname
    pendingPlanRefetch = true
    // 토스트 vs 선택 배너는 applyRemoteRefetch 에서 충돌 여부로 결정(중복 안내 방지)
  } else {
    showToast(`${event.actorNickname}님이 ${PLACE_ACTION_TEXT[event.action] || '계획을 수정했어요'}`)
  }
  if (localBusy.value) {
    pendingRefetch = true // 내 조작이 끝나면 watch 에서 한 번에 반영
    return
  }
  await applyRemoteRefetch()
}

const { connected, roster, connect } = usePresence(id, { onPlaceChange: handlePlaceChange })

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
const attractionSearchPages = computed(() => {
  const total = attractionSearchTotalPages.value
  const current = attractionSearchPage.value
  const start = Math.max(1, Math.min(current - 2, total - 4))
  const end = Math.min(total, start + 4)
  return Array.from({ length: Math.max(0, end - start + 1) }, (_, index) => start + index)
})
const visiblePlaces = computed(() =>
  places.value
    .filter((place) => (selectedDay.value === null ? place.visitDay == null : place.visitDay === selectedDay.value))
    .slice()
    .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0) || a.id - b.id),
)

const mapPlaces = computed(() =>
  visiblePlaces.value
    .map((place) => ({
      ...place,
      lat: Number(place.latitude),
      lng: Number(place.longitude),
    }))
    .filter((place) => Number.isFinite(place.lat) && Number.isFinite(place.lng)),
)

const mapFallbackTitle = computed(() => {
  if (mapState.value === 'missing-key') return '네이버 지도 키가 필요합니다'
  if (mapState.value === 'error') return '지도를 불러오지 못했습니다'
  return '지도를 준비하고 있습니다'
})

const mapFallbackText = computed(() => {
  if (mapState.value === 'missing-key') return 'VITE_NAVER_MAP_CLIENT_ID 값을 .env.local에 등록해 주세요.'
  if (mapState.value === 'error') return 'Client ID와 Web 서비스 URL 등록 상태를 확인해 주세요.'
  return '잠시만 기다려 주세요.'
})

const optimizeSteps = [
  '장소별 좌표 확인',
  '네이버 Directions 5 거리행렬 호출',
  '최단 이동시간 기준 경로 재정렬',
  '결과를 일정과 지도에 반영',
]

const optimizeProgress = computed(() => {
  if (optimizeCompleted.value || optimizeFailed.value) return 100
  return Math.min(92, 12 + Math.round((optimizeElapsedMs.value / OPTIMIZE_ESTIMATE_MS) * 80))
})

const optimizeActiveStep = computed(() => {
  if (optimizeCompleted.value) return optimizeSteps.length
  if (optimizeProgress.value >= 78) return 3
  if (optimizeProgress.value >= 48) return 2
  if (optimizeProgress.value >= 24) return 1
  return 0
})

const optimizeStageLabel = computed(() => {
  if (optimizeFailed.value) return '최적화 실패'
  if (optimizeCompleted.value) return '결과 반영 완료'
  return optimizeSteps[optimizeActiveStep.value]
})

const optimizeElapsedLabel = computed(() => `${(optimizeElapsedMs.value / 1000).toFixed(1)}s`)
const optimizePanelTitle = computed(() =>
  optimizeFailed.value
    ? '동선 최적화에 실패했습니다'
    : optimizeCompleted.value
      ? '동선을 최적화했어요'
      : '동선을 최적화하고 있어요',
)
const optimizePanelDescription = computed(() => {
  const dayLabel = selectedDay.value === null ? '보관함' : `Day ${selectedDay.value}`
  return `${dayLabel}의 ${visiblePlaces.value.length}개 장소를 실제 자동차 이동시간 기준으로 재배열 중입니다.`
})

watch(dayCount, (maxDay) => {
  if (selectedDay.value != null && selectedDay.value > maxDay) {
    selectedDay.value = maxDay
  }
  dayJumpValue.value = clampDay(dayJumpValue.value)
})

watch(selectedDay, (day) => {
  if (day !== null) {
    dayJumpValue.value = day
  }
})

watch(mapPlaces, () => {
  renderMapPlaces()
}, { flush: 'post' })

watch([selectedDay, mapPlaces], () => {
  loadDrivingRoute()
}, { flush: 'post' })

function fill(plan) {
  form.value = {
    title: plan.title ?? '',
    description: plan.description ?? '',
    startDate: plan.startDate ?? '',
    endDate: plan.endDate ?? '',
    version: plan.version,
  }
  if (plan.myRole !== undefined) myRole.value = plan.myRole
  metaDirty.value = false // 서버값으로 동기화됨 → 미저장 변경 없음
}

// F06 공동편집 — 멤버(협업자) 관리
function roleText(role) {
  if (role === 'OWNER') return '소유자'
  if (role === 'EDITOR') return '편집자'
  if (role === 'VIEWER') return '뷰어'
  return role
}

async function openShare() {
  shareOpen.value = true
  inviteError.value = ''
  await loadMembers()
}

function closeShare() {
  shareOpen.value = false
}

async function loadMembers() {
  membersLoading.value = true
  membersError.value = ''
  try {
    members.value = await tripPlanApi.listMembers(id)
  } catch (e) {
    membersError.value = '멤버를 불러오지 못했습니다.'
  } finally {
    membersLoading.value = false
  }
}

async function submitInvite() {
  if (inviteBusy.value) return
  const email = inviteEmail.value.trim()
  if (!email) {
    inviteError.value = '이메일을 입력하세요.'
    return
  }
  inviteBusy.value = true
  inviteError.value = ''
  try {
    await tripPlanApi.inviteMember(id, { email, role: inviteRole.value })
    inviteEmail.value = ''
    await loadMembers()
  } catch (e) {
    const status = e.response?.status
    if (status === 404) inviteError.value = '해당 이메일의 사용자를 찾을 수 없습니다.'
    else if (status === 409) inviteError.value = '이미 참여 중인 멤버입니다.'
    else if (status === 400) inviteError.value = e.response?.data?.message || '초대할 수 없습니다.'
    else if (status === 403) inviteError.value = '초대 권한이 없습니다.'
    else inviteError.value = '초대에 실패했습니다.'
  } finally {
    inviteBusy.value = false
  }
}

async function changeMemberRole(member, role) {
  membersError.value = ''
  try {
    await tripPlanApi.updateMemberRole(id, member.userId, role)
    await loadMembers()
  } catch (e) {
    membersError.value = '역할 변경에 실패했습니다.'
  }
}

async function kickMember(member) {
  const leavingSelf = member.userId === myUserId.value
  const message = leavingSelf
    ? '이 여행 계획에서 나가시겠어요? 다시 참여하려면 초대를 받아야 해요.'
    : `${member.email} 님을 이 계획에서 내보낼까요?`
  if (!window.confirm(message)) return
  membersError.value = ''
  try {
    await tripPlanApi.removeMember(id, member.userId)
    if (leavingSelf) {
      // 더 이상 멤버가 아니므로 이 계획에 머무를 수 없다 → 내 계획 목록으로 이동
      router.push('/plans')
      return
    }
    await loadMembers()
  } catch (e) {
    membersError.value = leavingSelf ? '계획에서 나가지 못했습니다.' : '멤버 제거에 실패했습니다.'
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

function loadNaverMapsScript() {
  const clientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID
  if (!clientId || clientId === 'your_naver_map_client_id') {
    mapState.value = 'missing-key'
    return Promise.reject(new Error('missing naver map client id'))
  }

  if (window.naver?.maps) return Promise.resolve(window.naver.maps)
  if (naverMapsScriptPromise) return naverMapsScriptPromise

  naverMapsScriptPromise = new Promise((resolve, reject) => {
    const existing = document.getElementById('naver-map-sdk')
    if (existing) {
      existing.addEventListener('load', () => resolve(window.naver.maps), { once: true })
      existing.addEventListener('error', reject, { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = 'naver-map-sdk'
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}`
    script.async = true
    script.onload = () => resolve(window.naver.maps)
    script.onerror = reject
    document.head.appendChild(script)
  })

  return naverMapsScriptPromise
}

async function initNaverMap() {
  if (!mapElement.value || mapInstance.value) return
  mapState.value = 'loading'
  try {
    const maps = await loadNaverMapsScript()
    mapInstance.value = new maps.Map(mapElement.value, {
      center: new maps.LatLng(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng),
      zoom: 12,
      minZoom: 6,
      scaleControl: true,
      mapDataControl: false,
      logoControlOptions: {
        position: maps.Position.BOTTOM_LEFT,
      },
    })
    mapState.value = 'ready'
    renderMapPlaces()
    setTimeout(() => {
      maps.Event.trigger(mapInstance.value, 'resize')
      renderMapPlaces()
    }, 0)
  } catch (error) {
    if (mapState.value !== 'missing-key') mapState.value = 'error'
  }
}

function renderMapPlaces() {
  const maps = window.naver?.maps
  const map = mapInstance.value
  if (!maps || !map) return

  clearMapOverlays()

  if (mapPlaces.value.length === 0) {
    map.setCenter(new maps.LatLng(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng))
    map.setZoom(12)
    return
  }

  const bounds = new maps.LatLngBounds()
  const path = []

  mapPlaces.value.forEach((place, index) => {
    const position = new maps.LatLng(place.lat, place.lng)
    bounds.extend(position)
    path.push(position)

    const marker = new maps.Marker({
      map,
      position,
      title: cleanDisplayName(place.name),
    })

    maps.Event.addListener(marker, 'click', () => {
      if (place.attractionId) {
        router.push(`/attractions/${place.attractionId}`)
        return
      }
      selectedMapPlaceName.value = cleanDisplayName(place.name)
      map.morph(position, 15, {
        duration: 250,
        easing: 'easeOutCubic',
      })
    })
    maps.Event.addListener(marker, 'mouseover', () => {
      const name = cleanDisplayName(place.name)
      openMarkerInfoWindow(maps, map, marker, name)
    })
    maps.Event.addListener(marker, 'mouseout', closeMarkerInfoWindow)

    mapMarkers.value.push(marker)
  })

  const routePath = drivingRoutePath.value.length > 1
    ? drivingRoutePath.value.map((point) => new maps.LatLng(point.latitude, point.longitude))
    : path

  routePath.forEach((position) => bounds.extend(position))
  const trafficSegments = drivingRouteSegments.value
    .map((segment) => ({
      congestion: Number(segment.congestion) || 0,
      path: Array.isArray(segment.path)
        ? segment.path.map((point) => new maps.LatLng(point.latitude, point.longitude))
        : [],
    }))
    .filter((segment) => segment.path.length > 1)

  if (trafficSegments.length > 0) {
    // Directions의 교통 section은 주요 도로 구간만 포함할 수 있다.
    // 전체 경로를 바탕선으로 먼저 그려 section 사이도 끊기지 않게 연결한다.
    routeLines.value.push(new maps.Polyline({
      map,
      path: routePath,
      strokeColor: '#60706B',
      strokeWeight: 4,
      strokeOpacity: 0.92,
      strokeStyle: 'solid',
    }))
    trafficSegments.forEach((segment) => {
      routeLines.value.push(new maps.Polyline({
        map,
        path: segment.path,
        strokeColor: trafficColor(segment.congestion),
        strokeWeight: 5,
        strokeOpacity: 0.9,
        strokeStyle: 'solid',
      }))
    })
  } else if (routePath.length > 1) {
    routeLines.value.push(new maps.Polyline({
      map,
      path: routePath,
      strokeColor: '#109A8E',
      strokeWeight: 4,
      strokeOpacity: 0.82,
      strokeStyle: 'solid',
    }))
  }

  if (mapPlaces.value.length === 1) {
    map.setCenter(path[0])
    map.setZoom(14)
  } else {
    routeBounds.value = bounds
    fitMapToPlaces()
  }
}

async function loadDrivingRoute() {
  const request = ++drivingRouteRequest
  drivingRoutePath.value = []
  drivingRouteSegments.value = []
  drivingRouteSummary.value = null
  renderMapPlaces()
  if (selectedDay.value === null || mapPlaces.value.length < 2) return

  try {
    const route = await tripPlanApi.getDrivingRoute(id, selectedDay.value)
    if (request !== drivingRouteRequest) return
    drivingRoutePath.value = Array.isArray(route.path) ? route.path : []
    drivingRouteSegments.value = Array.isArray(route.segments) ? route.segments : []
    drivingRouteSummary.value = route.summary || null
    renderMapPlaces()
  } catch {
    // Directions를 사용할 수 없는 개발 환경에서는 기존 좌표 연결선으로 표시한다.
    if (request === drivingRouteRequest) {
      drivingRoutePath.value = []
      drivingRouteSegments.value = []
      drivingRouteSummary.value = null
      renderMapPlaces()
    }
  }
}

function trafficColor(congestion) {
  if (congestion === 1) return '#22A66F'
  if (congestion === 2) return '#E89A3C'
  if (congestion === 3) return '#DC3545'
  return '#109A8E'
}

function formatWon(value) {
  return `${new Intl.NumberFormat('ko-KR').format(Math.max(0, Number(value) || 0))}원`
}

function formatDistance(value) {
  const meter = Math.max(0, Number(value) || 0)
  return meter >= 1000 ? `${(meter / 1000).toFixed(1)}km` : `${meter}m`
}

function formatDrivingDuration(value) {
  const minutes = Math.max(0, Math.round((Number(value) || 0) / 60000))
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60
  if (hours === 0) return `${remainingMinutes}분`
  return remainingMinutes > 0 ? `${hours}시간 ${remainingMinutes}분` : `${hours}시간`
}

function fitMapToPlaces() {
  const map = mapInstance.value
  if (!map || !routeBounds.value) return
  map.fitBounds(routeBounds.value, { top: 48, right: 48, bottom: 48, left: 48 })
}

function hasCoordinates(place) {
  return Number.isFinite(Number(place.latitude)) && Number.isFinite(Number(place.longitude))
}

function focusMapPlace(place) {
  if (!hasCoordinates(place)) {
    placeError.value = '좌표가 없는 장소는 지도에서 이동할 수 없습니다.'
    return
  }

  const maps = window.naver?.maps
  const map = mapInstance.value
  if (!maps || !map) return

  placeError.value = ''
  selectedMapPlaceName.value = cleanDisplayName(place.name)
  const position = new maps.LatLng(Number(place.latitude), Number(place.longitude))
  map.morph(position, 15, {
    duration: 250,
    easing: 'easeOutCubic',
  })
}

function openMarkerInfoWindow(maps, map, marker, name) {
  closeMarkerInfoWindow()
  activeInfoWindow = new maps.InfoWindow({
    content: `<div class="naver-plan-info">${escapeHtml(name)}</div>`,
    borderWidth: 0,
    anchorSize: new maps.Size(8, 8),
  })
  activeInfoWindow.open(map, marker)
}

function closeMarkerInfoWindow() {
  if (!activeInfoWindow) return
  activeInfoWindow.close()
  activeInfoWindow = null
}

function clearMapOverlays() {
  closeMarkerInfoWindow()
  mapMarkers.value.forEach((marker) => marker.setMap(null))
  mapMarkers.value = []
  routeLines.value.forEach((line) => line.setMap(null))
  routeLines.value = []
  routeBounds.value = null
  selectedMapPlaceName.value = ''
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
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
  if (saving.value || !canEdit.value) return
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
    saveMsg.value = '저장을 완료했어요'
  } catch (e) {
    const status = e.response?.status
    if (status === 409) {
      // 낙관적 락 충돌: 덮어쓰지 않고 최신본을 받아 선택 배너로 안내(내 입력 보존)
      try {
        const latest = await tripPlanApi.get(id)
        metaConflict.value = { open: true, by: '다른 사용자', plan: latest }
      } catch {
        formError.value = '다른 사용자가 먼저 수정했습니다. 새로고침 후 다시 시도해 주세요.'
      }
    } else if (status === 400) {
      formError.value = e.response?.data?.message || '입력값을 확인해 주세요.'
    } else {
      formError.value = '저장에 실패했습니다.'
    }
  } finally {
    saving.value = false
  }
}

// 충돌 배너 — 최신 내용 불러오기(서버본 적용, 내 변경/기여 버림)
function loadServerMeta() {
  if (metaConflict.value.plan) fill(metaConflict.value.plan)
  metaTouched.value = false // 상대 내용을 받아들임 → 더는 내 기여 아님
  metaConflict.value = { open: false, by: '', plan: null }
}

// 충돌 배너 — 내 변경 유지(최신 version 을 채택 → 다음 저장이 상대 변경을 덮어쓴다)
function keepMyMeta() {
  if (metaConflict.value.plan) form.value.version = metaConflict.value.plan.version
  metaConflict.value = { open: false, by: '', plan: null }
}

async function removePlan() {
  if (deleting.value || !isOwner.value) return
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

async function searchAttractions() {
  const keyword = attractionKeyword.value.trim()
  attractionSearchError.value = ''
  attractionResults.value = []
  attractionSearchPage.value = 0
  attractionSearchTotalPages.value = 0
  if (keyword.length < 2) {
    attractionSearchError.value = '관광지명을 두 글자 이상 입력해 주세요.'
    return
  }

  await fetchAttractions(1)
}

async function changeAttractionSearchPage(page) {
  if (attractionSearching.value || page < 1 || page > attractionSearchTotalPages.value || page === attractionSearchPage.value) return
  await fetchAttractions(page)
}

async function fetchAttractions(page) {
  attractionSearching.value = true
  try {
    const data = await attractionApi.search({
      keyword: attractionKeyword.value.trim(),
      page,
      size: 6,
    })
    attractionResults.value = data.items || []
    attractionSearchPage.value = data.page || page
    attractionSearchTotalPages.value = data.totalPages || 0
    if (attractionResults.value.length === 0) {
      attractionSearchError.value = '검색 결과가 없습니다. 다른 검색어를 입력해 주세요.'
    }
  } catch (e) {
    attractionSearchError.value = e.response?.data?.message || '관광지를 검색하지 못했습니다.'
  } finally {
    attractionSearching.value = false
  }
}

async function addAttractionPlace(attraction) {
  if (placeSaving.value) return
  placeError.value = ''
  placeMsg.value = ''

  placeSaving.value = true
  try {
    await tripPlanApi.addPlace(id, {
      attractionId: attraction.no,
      visitDay: selectedDay.value === null ? null : selectedDay.value,
    })
    attractionResults.value = []
    attractionKeyword.value = ''
    attractionSearchPage.value = 0
    attractionSearchTotalPages.value = 0
    await loadPlaces()
    placeMsg.value = `${cleanDisplayName(attraction.title)}을(를) 추가했습니다.`
  } catch (e) {
    placeError.value = e.response?.data?.message || '관광지 추가에 실패했습니다.'
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

function startPlaceDrag(event, place) {
  if (placeReordering.value) {
    event.preventDefault()
    return
  }
  draggedPlaceId.value = place.id
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', String(place.id))
}

function dragOverPlace(event, place) {
  if (!draggedPlaceId.value || draggedPlaceId.value === place.id) return
  event.dataTransfer.dropEffect = 'move'
  dragOverPlaceId.value = place.id
}

async function dropPlace(event, targetPlace) {
  const sourceId = draggedPlaceId.value || Number(event.dataTransfer.getData('text/plain'))
  endPlaceDrag()
  if (!sourceId || sourceId === targetPlace.id || placeReordering.value) return

  const placeIds = visiblePlaces.value.map((place) => place.id)
  const sourceIndex = placeIds.indexOf(sourceId)
  const targetIndex = placeIds.indexOf(targetPlace.id)
  if (sourceIndex < 0 || targetIndex < 0) return

  placeIds.splice(sourceIndex, 1)
  // 아래로 끌면 대상 뒤, 위로 끌면 대상 앞으로 이동한다.
  const insertIndex = targetIndex
  placeIds.splice(insertIndex, 0, sourceId)

  placeError.value = ''
  placeReordering.value = true
  try {
    places.value = await tripPlanApi.reorderPlaces(id, {
      visitDay: selectedDay.value,
      placeIds,
    })
  } catch (e) {
    placeError.value = e.response?.data?.message || '장소 순서 변경에 실패했습니다.'
  } finally {
    placeReordering.value = false
  }
}

function endPlaceDrag() {
  draggedPlaceId.value = null
  dragOverPlaceId.value = null
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

async function optimizeRoute() {
  if (optimizing.value || selectedDay.value === null) return
  placeError.value = ''
  placeMsg.value = ''

  if (visiblePlaces.value.length < 2) {
    placeError.value = '동선 최적화는 장소가 2개 이상일 때 가능합니다.'
    return
  }

  optimizing.value = true
  startOptimizePanel()
  try {
    places.value = await tripPlanApi.optimizePlaces(id, {
      visitDay: selectedDay.value,
    })
    placeMsg.value = `Day ${selectedDay.value} 동선을 최적화했습니다.`
    finishOptimizePanel(true)
  } catch (e) {
    placeError.value = e.response?.data?.message || '동선 최적화에 실패했습니다.'
    finishOptimizePanel(false)
  } finally {
    optimizing.value = false
  }
}

function startOptimizePanel() {
  clearOptimizeTimer()
  optimizePanelVisible.value = true
  optimizeCompleted.value = false
  optimizeFailed.value = false
  optimizeElapsedMs.value = 0
  optimizeStartedAt = Date.now()
  optimizeTimer = window.setInterval(() => {
    optimizeElapsedMs.value = Date.now() - optimizeStartedAt
  }, 100)
}

function finishOptimizePanel(success) {
  optimizeElapsedMs.value = Date.now() - optimizeStartedAt
  optimizeCompleted.value = success
  optimizeFailed.value = !success
  clearOptimizeTimer()

  const visibleFor = Math.max(700, 1100 - optimizeElapsedMs.value)
  window.setTimeout(() => {
    optimizePanelVisible.value = false
    optimizeCompleted.value = false
    optimizeFailed.value = false
  }, visibleFor)
}

function clearOptimizeTimer() {
  if (!optimizeTimer) return
  window.clearInterval(optimizeTimer)
  optimizeTimer = null
}

function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}

onMounted(async () => {
  await load()
  await nextTick()
  if (!loadError.value) initNaverMap()
  // 멤버일 때만 실시간 협업 연결(프레즌스 + 장소 동기화). 비멤버는 load()에서 이미 막힘.
  if (!loadError.value && myRole.value) connect()
})

onBeforeUnmount(() => {
  clearMapOverlays()
  clearOptimizeTimer()
  if (toastTimer) clearTimeout(toastTimer)
  mapInstance.value = null
})
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

/* F06 P2a — 실시간 접속자 클러스터 */
.presence-cluster {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 6px;
  margin-right: 4px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 999px;
}

.presence-avatars {
  display: flex;
}

.presence-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-size: 11px;
  font-weight: 700;
  border: 2px solid white;
  margin-left: -8px;
}

.presence-avatar:first-child {
  margin-left: 0;
}

.presence-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-3);
  white-space: nowrap;
}

.presence-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--success);
  animation: presence-pulse 1.6s ease-in-out infinite;
}

.presence-dot--off {
  background: var(--ink-soft);
  animation: none;
}

@keyframes presence-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* F06 P2a — 변경 알림 토스트 */
.edit-toast {
  position: fixed;
  left: 50%;
  bottom: 28px;
  transform: translateX(-50%);
  z-index: 700;
  padding: 11px 20px;
  border-radius: 999px;
  background: var(--ink, #16242a);
  color: white;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 8px 28px rgba(20, 38, 46, 0.28);
}

.edit-toast-enter-active,
.edit-toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.edit-toast-enter-from,
.edit-toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
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
  grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.95fr);
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
.storage-tab,
.optimize-btn {
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
.storage-tab:hover,
.optimize-btn:hover:not(:disabled) { border-color: var(--line-2); }
.storage-tab.active { background: var(--teal); color: white; }

.optimize-btn {
  margin-left: auto;
  background: var(--teal);
  color: white;
}

.optimize-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.place-search {
  padding: 16px;
  margin-bottom: 18px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
}

.place-search__form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
}

.place-search__field {
  min-width: 0;
}

.place-search__hint {
  margin-top: 8px;
  color: var(--ink-soft);
  font-size: 12px;
}

.attraction-results {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.attraction-pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 14px;
}

.attraction-pagination button {
  min-width: 32px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid var(--line-2);
  border-radius: 7px;
  background: white;
  color: var(--ink-3);
  font: inherit;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.attraction-pagination button.active {
  border-color: var(--teal);
  background: var(--teal);
  color: white;
}

.attraction-pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.attraction-result {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--bg-soft);
  color: var(--ink);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.attraction-result:hover:not(:disabled) {
  border-color: var(--teal);
  background: var(--teal-soft);
}

.attraction-result:disabled {
  cursor: wait;
  opacity: 0.65;
}

.attraction-result img,
.attraction-result__image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.attraction-result__image {
  display: grid;
  place-items: center;
  background: white;
}

.attraction-result__body {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.attraction-result__body strong,
.attraction-result__body small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attraction-result__body strong { font-size: 14px; }
.attraction-result__body small { color: var(--ink-soft); }

.attraction-result__add {
  color: var(--teal-3);
  font-size: 12px;
  font-weight: 800;
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

.empty-places strong,
.empty-places span {
  display: block;
}

.empty-places span {
  margin-top: 6px;
  font-size: 13px;
}

.timeline-row {
  display: grid;
  grid-template-columns: 56px 28px 1fr;
  gap: 10px;
  cursor: grab;
}

.timeline-row--dragging {
  opacity: 0.45;
  cursor: grabbing;
}

.timeline-row--drag-over .item-card {
  border-color: var(--teal);
  box-shadow: 0 0 0 2px var(--teal-soft);
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

.drag-handle {
  width: 10px;
  color: var(--ink-soft);
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
  text-align: center;
  user-select: none;
}

.item-card--mappable {
  cursor: pointer;
}

.item-card--mappable:hover {
  border-color: var(--teal);
  box-shadow: var(--sh-1);
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

.mini-btn--primary {
  background: var(--teal);
  border-color: var(--teal);
  color: white;
}

/* F06 P2b — 메타 충돌(상대 저장 vs 내 미저장 변경) 선택 배너 */
.meta-conflict {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--teal);
  background: var(--teal-soft);
  border-radius: 10px;
}

.meta-conflict__msg {
  font-size: 13px;
  font-weight: 600;
  color: var(--teal-3);
}

.meta-conflict__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.plan-map {
  position: sticky;
  top: 88px;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  overflow: hidden;
  height: clamp(460px, calc(100vh - 128px), 680px);
  display: flex;
  flex-direction: column;
}

.map-canvas {
  flex: 1;
  position: relative;
  background: var(--bg-2);
  overflow: hidden;
}

.map-fallback {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: grid;
  place-content: center;
  gap: 6px;
  padding: 24px;
  text-align: center;
  background: var(--bg-soft);
  color: var(--ink-soft);
}

.map-fallback strong {
  color: var(--ink-3);
  font-size: 14px;
}

.map-fallback span {
  font-size: 12px;
}

.map-fit-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 2;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--line-2);
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.94);
  color: var(--ink-3);
  box-shadow: var(--sh-1);
  font-size: 12px;
  font-weight: 800;
}

.map-fit-btn:hover {
  border-color: var(--teal);
  color: var(--teal-3);
}

.map-traffic-legend {
  position: absolute;
  left: 14px;
  bottom: 14px;
  z-index: 2;
  display: flex;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--sh-1);
  color: var(--ink-3);
  font-size: 11px;
  font-weight: 700;
}

.map-traffic-legend span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.traffic-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.traffic-dot--clear { background: #22A66F; }
.traffic-dot--slow { background: #E89A3C; }
.traffic-dot--busy { background: #DC3545; }

@media (max-width: 480px) {
  .map-traffic-legend {
    gap: 7px;
    padding: 7px 8px;
    font-size: 10px;
  }
}

:global(.naver-plan-info) {
  max-width: 180px;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: white;
  color: var(--ink);
  box-shadow: var(--sh-2);
  font-size: 12px;
  font-weight: 700;
  white-space: normal;
  overflow-wrap: anywhere;
  pointer-events: none;
}

.map-info {
  padding: 14px 18px;
  background: white;
  border-top: 1px solid var(--line);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-info h4 {
  min-width: 0;
  padding-right: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
}

.map-info p {
  flex-shrink: 0;
}

.route-cost-card {
  padding: 14px 18px 12px;
  border-top: 1px solid var(--line);
  background: linear-gradient(135deg, var(--teal-tint), #fff);
}

.route-cost-card__head,
.route-cost-card__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.route-cost-card__head > span {
  color: var(--teal-3);
  font-size: 13px;
  font-weight: 800;
}

.route-cost-card__head small {
  color: var(--ink-soft);
  font-size: 11px;
}

.route-cost-card__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.route-cost-card__metrics span {
  padding: 4px 7px;
  border: 1px solid rgba(15, 110, 86, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--ink-3);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
}

.route-cost-card__total {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed rgba(15, 110, 86, 0.2);
  color: var(--ink-3);
  font-size: 12px;
  font-weight: 700;
}

.route-cost-card__total strong {
  color: var(--teal-3);
  font-family: var(--font-mono);
  font-size: 16px;
}

.route-cost-card > p {
  margin-top: 7px;
  color: var(--ink-soft);
  font-size: 10px;
  line-height: 1.4;
}

/* F06 공동편집 — 읽기 전용 배너 + 공유/멤버 다이얼로그 */
.readonly-banner {
  margin: 0 0 16px;
  padding: 10px 16px;
  border-radius: 12px;
  background: var(--coral-tint, #fff1ec);
  color: var(--coral, #e06a4f);
  font-size: 14px;
}

.share-overlay {
  position: fixed;
  inset: 0;
  z-index: 600;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(22, 34, 40, 0.55);
  backdrop-filter: blur(3px);
}

.share-modal {
  width: min(520px, 100%);
  padding: 28px;
  border-radius: 22px;
  background: white;
  box-shadow: 0 24px 80px rgba(20, 38, 46, 0.24);
}

.share-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.share-close {
  border: none;
  background: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--muted, #7b8a91);
}

.member-list {
  list-style: none;
  margin: 0 0 20px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(20, 38, 46, 0.08);
  border-radius: 12px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.member-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-me {
  flex-shrink: 0;
  padding: 1px 7px;
  border-radius: 8px;
  background: var(--coral-tint, #fff1ec);
  color: var(--coral, #e06a4f);
  font-size: 11px;
  font-weight: 600;
}

.member-role {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--muted, #7b8a91);
}

/* 수락 대기 중인 초대(아직 미참여) */
.member-pending {
  flex-shrink: 0;
  padding: 1px 7px;
  border-radius: 8px;
  background: var(--bg-2, #f1f4f5);
  color: var(--ink-3, #7b8a91);
  font-size: 11px;
  font-weight: 600;
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.member-actions select,
.invite-row select {
  padding: 4px 6px;
  border: 1px solid rgba(20, 38, 46, 0.16);
  border-radius: 8px;
  font-size: 13px;
}

.invite-form {
  border-top: 1px solid rgba(20, 38, 46, 0.08);
  padding-top: 16px;
}

.invite-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.invite-row input {
  flex: 1;
  min-width: 0;
  padding: 8px 12px;
  border: 1px solid rgba(20, 38, 46, 0.16);
  border-radius: 10px;
}

.optimize-overlay {
  position: fixed;
  inset: 0;
  z-index: 500;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(22, 34, 40, 0.55);
  backdrop-filter: blur(3px);
}

.optimize-modal {
  width: min(760px, 100%);
  padding: 54px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 26px;
  background: white;
  box-shadow: 0 24px 80px rgba(20, 38, 46, 0.24);
}

.optimize-mark {
  position: relative;
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  margin-bottom: 28px;
  border-radius: 24px;
  background: var(--coral-tint);
  color: var(--coral);
}

.optimize-pulse {
  position: absolute;
  inset: 15px;
  border-radius: 18px;
  background: rgba(224, 91, 58, 0.16);
  animation: optimizePulse 1.35s ease-in-out infinite;
}

.optimize-mark svg {
  position: relative;
  z-index: 1;
}

.optimize-modal h2 {
  margin-bottom: 10px;
  color: var(--ink);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0;
}

.optimize-modal p {
  max-width: 620px;
  margin-bottom: 34px;
  color: var(--ink-soft);
  font-size: 16px;
  line-height: 1.7;
}

.optimize-progress {
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--bg-2);
}

.optimize-progress__bar {
  height: 100%;
  border-radius: inherit;
  background: var(--coral);
  transition: width 0.22s ease;
}

.optimize-meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 14px 0 28px;
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 800;
}

.optimize-steps {
  display: flex;
  flex-direction: column;
  gap: 13px;
  padding-top: 26px;
  border-top: 1px solid var(--line);
}

.optimize-steps li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--ink-soft);
  font-size: 15px;
  font-weight: 700;
}

.step-dot {
  width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  flex: 0 0 18px;
  border: 2px solid var(--line-2);
  border-radius: 50%;
}

.optimize-steps li.done {
  color: var(--ink-2);
}

.optimize-steps li.done .step-dot {
  border-color: var(--success);
  background: var(--success);
}

.optimize-steps li.done .step-dot::after {
  content: '';
  width: 8px;
  height: 5px;
  border-left: 2px solid white;
  border-bottom: 2px solid white;
  transform: rotate(-45deg) translateY(-1px);
}

.optimize-steps li.active {
  color: var(--ink);
}

.optimize-steps li.active .step-dot {
  border-color: var(--coral);
}

.optimize-steps li.active .step-dot::after {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--coral);
}

.optimize-steps li.failed {
  color: var(--coral);
}

.optimize-steps li.failed .step-dot {
  border-color: var(--coral);
  background: var(--coral);
}

@keyframes optimizePulse {
  0%, 100% { transform: scale(0.86); opacity: 0.45; }
  50% { transform: scale(1.22); opacity: 0.9; }
}

@media (max-width: 1000px) {
  .plan-grid { grid-template-columns: 1fr; }
  .plan-map {
    position: static;
    height: clamp(360px, 58vh, 520px);
  }
  .place-search__form { grid-template-columns: minmax(220px, 1fr) 142px; }
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

  .place-search__form,
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
  .storage-tab,
  .optimize-btn {
    width: 100%;
  }

  .time-col,
  .dot-col {
    display: none;
  }

  .optimize-modal {
    padding: 34px 24px;
    border-radius: 20px;
  }

  .optimize-meta {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
