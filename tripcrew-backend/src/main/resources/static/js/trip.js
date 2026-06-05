// ─── Utils ───────────────────────────────────────────────────────────────────

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])
  );
}

function getSpotKey(spot) {
  return spot.id || `${spot.title}-${spot.lat}-${spot.lng}`;
}

function toRadians(deg) { return deg * Math.PI / 180; }

function calculateDistanceKm(lat1, lng1, lat2, lng2) {
  const R = 6371;
  const dLat = toRadians(Number(lat2) - Number(lat1));
  const dLng = toRadians(Number(lng2) - Number(lng1));
  const a = Math.sin(dLat / 2) ** 2
    + Math.cos(toRadians(Number(lat1))) * Math.cos(toRadians(Number(lat2))) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function getDistanceLabel(km) {
  if (km === null || km === undefined) return '기준지를 선택해주세요.';
  if (km === 0) return '기준 관광지';
  if (km < 1) return `${Math.round(km * 1000)}m`;
  return `${km.toFixed(1)}km`;
}

// ─── TSP Algorithms ──────────────────────────────────────────────────────────

function buildDistanceMatrix(spots) {
  const n = spots.length;
  const m = Array.from({ length: n }, () => new Array(n).fill(0));
  for (let i = 0; i < n; i++)
    for (let j = i + 1; j < n; j++) {
      const d = calculateDistanceKm(spots[i].lat, spots[i].lng, spots[j].lat, spots[j].lng);
      m[i][j] = d; m[j][i] = d;
    }
  return m;
}

function pathDistance(order, matrix) {
  let total = 0;
  for (let i = 0; i < order.length - 1; i++) total += matrix[order[i]][order[i + 1]];
  return total;
}

function nearestNeighborOrder(matrix, start = 0) {
  const n = matrix.length;
  const visited = new Array(n).fill(false);
  const order = [start]; visited[start] = true;
  let cur = start;
  for (let s = 1; s < n; s++) {
    let next = -1, best = Infinity;
    for (let j = 0; j < n; j++)
      if (!visited[j] && matrix[cur][j] < best) { best = matrix[cur][j]; next = j; }
    visited[next] = true; order.push(next); cur = next;
  }
  return order;
}

function twoOptImprove(order, matrix, maxIter = 50) {
  const path = order.slice();
  const n = path.length;
  if (n < 4) return path;
  let improved = true, iter = 0;
  while (improved && iter < maxIter) {
    improved = false; iter++;
    for (let i = 0; i < n - 2; i++)
      for (let k = i + 1; k < n - 1; k++) {
        const [a, b, c, d] = [path[i], path[i + 1], path[k], path[k + 1]];
        if (matrix[a][c] + matrix[b][d] + 1e-9 < matrix[a][b] + matrix[c][d]) {
          let l = i + 1, r = k;
          while (l < r) { [path[l], path[r]] = [path[r], path[l]]; l++; r--; }
          improved = true;
        }
      }
  }
  return path;
}

// ─── MapService ───────────────────────────────────────────────────────────────

class MapService {
  constructor() {
    this.map = null;
    this.markers = [];
    this.markerByKey = new Map();
    this.infowindow = null;
    this.polyline = null;
    this.overlays = [];
    this._osrmCache = new Map();
    this._reqId = 0;
  }

  async init() {
    await this._loadKakaoScript();
    this.map = new kakao.maps.Map(document.getElementById('map'), {
      center: new kakao.maps.LatLng(36.5, 127.8),
      level: 13
    });
    this.infowindow = new kakao.maps.InfoWindow({ zIndex: 1 });
  }

  _loadKakaoScript() {
    return new Promise((resolve, reject) => {
      if (window.kakao?.maps) { resolve(); return; }
      const script = document.createElement('script');
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${CONFIG.KAKAO_MAP_KEY}&autoload=false`;
      script.onload = () => kakao.maps.load(resolve);
      script.onerror = () => reject(new Error('카카오맵 스크립트 로드 실패'));
      document.head.appendChild(script);
    });
  }

  clearMarkers() {
    this.markers.forEach(m => m.setMap(null));
    this.markers = [];
    this.markerByKey = new Map();
  }

  placeMarkers(spots, onClickCb) {
    spots.forEach(spot => {
      const pos = new kakao.maps.LatLng(Number(spot.lat), Number(spot.lng));
      const marker = new kakao.maps.Marker({ map: this.map, position: pos });
      this.markers.push(marker);
      this.markerByKey.set(getSpotKey(spot), marker);
      kakao.maps.event.addListener(marker, 'click', () => {
        this.infowindow.setContent(
          `<div style="padding:8px;min-width:200px;"><strong>${escapeHtml(spot.title)}</strong><br><small>${escapeHtml(spot.addr)}</small></div>`
        );
        this.infowindow.open(this.map, marker);
        this.map.panTo(pos);
        onClickCb(spot);
      });
    });
  }

  focusSpot(spot) {
    const marker = this.markerByKey.get(getSpotKey(spot));
    const pos = new kakao.maps.LatLng(Number(spot.lat), Number(spot.lng));
    this.map.panTo(pos);
    this.map.setLevel(5);
    if (marker) {
      this.infowindow.setContent(
        `<div style="padding:8px;min-width:200px;"><strong>${escapeHtml(spot.title)}</strong><br><small>${escapeHtml(spot.addr)}</small></div>`
      );
      this.infowindow.open(this.map, marker);
    }
  }

  fitBounds(spots) {
    const bounds = new kakao.maps.LatLngBounds();
    spots.forEach(s => bounds.extend(new kakao.maps.LatLng(Number(s.lat), Number(s.lng))));
    this.map.setBounds(bounds);
  }

  clearPolyline() {
    if (this.polyline) { this.polyline.setMap(null); this.polyline = null; }
    this.overlays.forEach(o => o.setMap(null));
    this.overlays = [];
  }

  _drawCourseOverlays(course) {
    course.forEach((spot, idx) => {
      const content = document.createElement('div');
      content.style.cssText = 'transform:translate(-50%,-120%);background:linear-gradient(135deg,#6fc7ff,#4f8cff);color:#fff;font-weight:800;font-size:12px;padding:3px 8px;border-radius:999px;box-shadow:0 6px 14px rgba(79,140,255,0.32);white-space:nowrap;';
      content.textContent = `${idx + 1}. ${spot.title}`;
      const overlay = new kakao.maps.CustomOverlay({
        position: new kakao.maps.LatLng(Number(spot.lat), Number(spot.lng)),
        content, yAnchor: 1, zIndex: 3
      });
      overlay.setMap(this.map);
      this.overlays.push(overlay);
    });
  }

  async drawCoursePolyline(course) {
    this.clearPolyline();
    if (!this.map || course.length === 0) return;
    this._drawCourseOverlays(course);
    if (course.length < 2) return;

    const reqId = ++this._reqId;
    this.polyline = new kakao.maps.Polyline({
      path: course.map(s => new kakao.maps.LatLng(Number(s.lat), Number(s.lng))),
      strokeWeight: 4, strokeColor: '#9fcfff', strokeOpacity: 0.7, strokeStyle: 'shortdash'
    });
    this.polyline.setMap(this.map);

    const segments = await Promise.all(
      course.slice(0, -1).map((s, i) => this._fetchOsrmSegment(s, course[i + 1]))
    );
    if (reqId !== this._reqId) return;

    const merged = [];
    segments.forEach((coords, idx) => {
      coords.forEach((c, i) => { if (idx === 0 || i > 0) merged.push(c); });
    });

    if (this.polyline) this.polyline.setMap(null);
    this.polyline = new kakao.maps.Polyline({
      path: merged.map(([lng, lat]) => new kakao.maps.LatLng(lat, lng)),
      strokeWeight: 5, strokeColor: '#4f8cff', strokeOpacity: 0.9, strokeStyle: 'solid'
    });
    this.polyline.setMap(this.map);
  }

  async _fetchOsrmSegment(from, to) {
    const key = `${getSpotKey(from)}|${getSpotKey(to)}`;
    if (this._osrmCache.has(key)) return this._osrmCache.get(key);
    const url = `https://router.project-osrm.org/route/v1/driving/${from.lng},${from.lat};${to.lng},${to.lat}?overview=full&geometries=geojson`;
    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`OSRM HTTP ${res.status}`);
      const data = await res.json();
      const coords = data?.routes?.[0]?.geometry?.coordinates;
      if (!Array.isArray(coords) || coords.length < 2) throw new Error('no route geometry');
      this._osrmCache.set(key, coords);
      return coords;
    } catch {
      return [[Number(from.lng), Number(from.lat)], [Number(to.lng), Number(to.lat)]];
    }
  }
}

// ─── TripModel ────────────────────────────────────────────────────────────────

class TripModel {
  constructor() {
    this.spots = [];
    this.course = [];
    this.baseSpot = null;
  }

  setSpots(spots) { this.spots = spots; this.baseSpot = null; }
  setBaseSpot(spot) { this.baseSpot = spot; }

  getSpotsWithDistance() {
    if (!this.baseSpot) return this.spots.map(s => ({ ...s, distanceKm: null }));
    return this.spots
      .map(s => ({
        ...s,
        distanceKm: calculateDistanceKm(this.baseSpot.lat, this.baseSpot.lng, s.lat, s.lng)
      }))
      .sort((a, b) => a.distanceKm - b.distanceKm);
  }

  isInCourse(spot) {
    return this.course.some(s => getSpotKey(s) === getSpotKey(spot));
  }

  toggleCourse(spot) {
    const idx = this.course.findIndex(s => getSpotKey(s) === getSpotKey(spot));
    if (idx >= 0) this.course.splice(idx, 1);
    else this.course.push(spot);
  }

  clearCourse() { this.course = []; }

  optimizeCourse() {
    if (this.course.length < 2) return null;
    const matrix = buildDistanceMatrix(this.course);
    const order = twoOptImprove(nearestNeighborOrder(matrix, 0), matrix);
    const dist = pathDistance(order, matrix);
    this.course = order.map(i => this.course[i]);
    return dist;
  }

  // ─── Plan API ─────────────────────────────────────────────────────────────

  async fetchPlans() {
    const res = await fetch(`${window.CONTEXT_PATH}/tripplan`);
    if (res.status === 401) throw Object.assign(new Error('로그인이 필요합니다.'), { status: 401 });
    if (!res.ok) throw new Error('계획 조회에 실패했습니다.');
    return res.json();
  }

  async savePlan(planName, course) {
    const res = await fetch(`${window.CONTEXT_PATH}/tripplan`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        action: 'save',
        planName,
        course: course.map(s => ({ id: s.id }))
      })
    });
    if (res.status === 401) throw Object.assign(new Error('로그인이 필요합니다.'), { status: 401 });
    if (!res.ok) throw new Error('계획 저장에 실패했습니다.');
    return res.json();
  }

  async deletePlan(planId) {
    const res = await fetch(`${window.CONTEXT_PATH}/tripplan`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'delete', planId })
    });
    if (res.status === 401) throw Object.assign(new Error('로그인이 필요합니다.'), { status: 401 });
    if (!res.ok) throw new Error('계획 삭제에 실패했습니다.');
  }
}

// ─── TripView ─────────────────────────────────────────────────────────────────

class TripView {
  constructor() {
    this.el = {
      navSearch:        document.getElementById('nav-search'),
      navMyPlans:       document.getElementById('nav-myplans'),
      searchSection:    document.getElementById('search-main-section'),
      myPlansSection:   document.getElementById('myplans-section'),
      courseSection:    document.getElementById('course-section'),
      courseChips:      document.getElementById('course-chips'),
      courseMeta:       document.getElementById('course-meta'),
      tripList:         document.getElementById('trip-list'),
      distanceSummary:  document.getElementById('distance-summary'),
      myPlansList:      document.getElementById('myplans-list'),
      areaSelect:       document.getElementById('areaSelect'),
      gugunSelect:      document.getElementById('gugunSelect'),
      contentTypeSelect:document.getElementById('contentTypeSelect'),
      searchBtn:        document.getElementById('searchBtn'),
      optimizeBtn:      document.getElementById('optimizeBtn'),
      resetCourseBtn:   document.getElementById('resetCourseBtn'),
      savePlanBtn:      document.getElementById('savePlanBtn'),
    };
  }

  showPage(page) {
    this.el.searchSection.style.display  = page === 'search'  ? '' : 'none';
    this.el.myPlansSection.style.display = page === 'myplans' ? '' : 'none';
    this.el.navSearch?.classList.toggle('nav-active',  page === 'search');
    this.el.navMyPlans?.classList.toggle('nav-active', page === 'myplans');
  }

  renderTripList(spots, course, baseSpot, { onSelectSpot, onToggleCourse }) {
    const listEl = this.el.tripList;
    listEl.innerHTML = '';
    if (spots.length === 0) {
      listEl.innerHTML = '<p class="text-center p-5 text-muted">검색 결과가 없습니다.</p>';
      return;
    }
    const selectedKey = baseSpot ? getSpotKey(baseSpot) : '';
    spots.forEach((spot, idx) => {
      const isSelected = selectedKey === getSpotKey(spot);
      const inCourse = course.some(s => getSpotKey(s) === getSpotKey(spot));
      const item = document.createElement('div');
      item.className = `trip-item p-3 border-bottom${isSelected ? ' is-selected' : ''}`;
      item.innerHTML = `
        <div class="trip-item-head">
          <div class="fw-bold mb-1">${idx + 1}. ${escapeHtml(spot.title)}</div>
          <span class="trip-distance${isSelected ? ' is-base' : ''}">${getDistanceLabel(spot.distanceKm)}</span>
        </div>
        <div class="small text-muted mb-2">${escapeHtml(spot.addr)}</div>
        ${spot.image ? `<img src="${escapeHtml(spot.image)}" alt="${escapeHtml(spot.title)}">` : ''}
        <div class="trip-item-actions">
          <button type="button" class="course-toggle-btn${inCourse ? ' is-added' : ''}">
            ${inCourse ? '✓ 코스에 추가됨' : '＋ 코스에 추가'}
          </button>
        </div>
      `;
      item.addEventListener('click', () => onSelectSpot(spot));
      item.querySelector('.course-toggle-btn').addEventListener('click', e => {
        e.stopPropagation(); onToggleCourse(spot);
      });
      listEl.appendChild(item);
    });
  }

  renderCourse(course, optimizedDist = null, { onRemove, onFocus } = {}) {
    const { courseSection, courseChips, courseMeta } = this.el;
    if (!courseSection) return;
    if (course.length === 0) {
      courseSection.style.display = 'none';
      courseChips.innerHTML = '';
      return;
    }
    courseSection.style.display = 'block';
    courseChips.innerHTML = '';
    course.forEach((spot, idx) => {
      const chip = document.createElement('div');
      chip.className = 'course-chip';
      chip.innerHTML = `
        <span class="chip-index">${idx + 1}</span>
        <span>${escapeHtml(spot.title)}</span>
        <button type="button" class="chip-remove" aria-label="코스에서 제거">&times;</button>
      `;
      chip.querySelector('.chip-remove').addEventListener('click', e => {
        e.stopPropagation(); onRemove?.(spot);
      });
      chip.addEventListener('click', () => onFocus?.(spot));
      courseChips.appendChild(chip);
    });
    if (course.length >= 2) {
      const matrix = buildDistanceMatrix(course);
      const dist = optimizedDist ?? pathDistance(course.map((_, i) => i), matrix);
      courseMeta.innerHTML = optimizedDist !== null
        ? `<strong>최적화 완료</strong> · 총 이동 거리 <b>${dist.toFixed(2)}km</b>`
        : `총 ${course.length}곳 · 현재 순서 경로 길이 <b>${dist.toFixed(2)}km</b>`;
    } else {
      courseMeta.textContent = '2곳 이상을 추가하면 최적 동선을 계산할 수 있습니다.';
    }
  }

  renderDistanceSummary(baseSpot, nearestSpot) {
    const el = this.el.distanceSummary;
    if (!el) return;
    if (!baseSpot) {
      el.innerHTML = '<span>관광지를 선택하면 가까운 곳을 추천해드립니다!</span>';
      return;
    }
    el.innerHTML = `
      <strong>${escapeHtml(baseSpot.title)} 기준</strong>
      <span>${nearestSpot
        ? `가장 가까운 관광지는 ${escapeHtml(nearestSpot.title)} (${getDistanceLabel(nearestSpot.distanceKm)})입니다.`
        : '비교할 다른 관광지가 없습니다.'
      }</span>
    `;
  }

  renderMyPlans(plans, { onDelete, onLoad }) {
    const listEl = this.el.myPlansList;
    if (!listEl) return;
    if (plans.length === 0) {
      listEl.innerHTML = `
        <div class="myplans-empty">
          <div class="myplans-empty-icon">✈</div>
          <h3>저장된 여행 계획이 없습니다</h3>
          <p>관광지를 검색하고 코스를 구성한 뒤 저장해보세요!</p>
        </div>
      `;
      return;
    }
    listEl.innerHTML = '';
    plans.forEach(plan => {
      const date = new Date(plan.createdAt).toLocaleDateString('ko-KR');
      const card = document.createElement('div');
      card.className = 'plan-card';
      card.innerHTML = `
        <div class="plan-card-head">
          <div class="plan-card-info">
            <h4 class="plan-name">${escapeHtml(plan.planName)}</h4>
            <span class="plan-meta">${date} · ${plan.course.length}곳</span>
          </div>
          <div class="plan-card-actions">
            <button type="button" class="btn-sky btn-sm load-btn">불러오기</button>
            <button type="button" class="btn-soft btn-sm delete-btn">삭제</button>
          </div>
        </div>
        <ol class="plan-course-list">
          ${plan.course.map(s => `
            <li>
              <span class="plan-spot-title">${escapeHtml(s.title)}</span>
              <span class="plan-spot-addr">${escapeHtml(s.addr)}</span>
            </li>
          `).join('')}
        </ol>
      `;
      card.querySelector('.load-btn').addEventListener('click', () => onLoad(plan));
      card.querySelector('.delete-btn').addEventListener('click', () => onDelete(plan.id));
      listEl.appendChild(card);
    });
  }
}

// ─── TripController ───────────────────────────────────────────────────────────

class TripController {
  constructor(model, view, mapService) {
    this.model = model;
    this.view = view;
    this.map = mapService;
  }

  async init() {
    try {
      await this.map.init();
    } catch (err) {
      console.error(err);
      alert('지도를 불러오는 중 오류가 발생했습니다. API 키를 확인하세요.');
    }
    await this._loadSido();
    this._bindEvents();
    this.view.showPage('search');
    this._renderCourse();
  }

  _bindEvents() {
    const { el } = this.view;
    el.navSearch?.addEventListener('click', e => { e.preventDefault(); this._showSearch(); });
    el.navMyPlans?.addEventListener('click', e => { e.preventDefault(); this._showMyPlans(); });
    el.areaSelect?.addEventListener('change', async e => {
      await this._loadGugun(e.target.value);
      await this.onSearch();
    });
    el.gugunSelect?.addEventListener('change', () => this.onSearch());
    el.contentTypeSelect?.addEventListener('change', () => this.onSearch());
    el.searchBtn?.addEventListener('click', () => this.onSearch());
    el.optimizeBtn?.addEventListener('click', () => this.onOptimizeCourse());
    el.resetCourseBtn?.addEventListener('click', () => this.onResetCourse());
    el.savePlanBtn?.addEventListener('click', () => this.onSavePlan());
  }

  async _loadSido() {
    try {
        const response = await fetch(this._url({ action: 'sidos' }));
        const data = await response.json();
        console.log("시도 데이터 전체 구조:", data); // ★ 콘솔에서 데이터가 배열인지 객체인지 확인

        const sel = this.view.el.areaSelect;
        if (!sel) {
            console.error("areaSelect 요소를 찾을 수 없습니다.");
            return;
        }
        
        sel.innerHTML = '<option value="">시도 선택</option>';
        
        // 데이터가 객체 안에 숨어있을 경우를 대비한 방어 코드
        const list = Array.isArray(data) ? data : (data.items || data.sidos || []);
        
        list.forEach(d => { 
            sel.innerHTML += `<option value="${d.sidoCode}">${d.sidoName}</option>`; 
        });
    } catch (err) { 
        console.error('시도 조회 실패:', err); 
    }
}

  async _loadGugun(areaCode) {
    const sel = this.view.el.gugunSelect;
    if (!areaCode) { sel.innerHTML = '<option value="">시군구 선택</option>'; return; }
    try {
      const data = await (await fetch(this._url({ action: 'guguns', sidoCode: areaCode }))).json();
      sel.innerHTML = '<option value="">시군구 선택</option>';
      data.forEach(d => { sel.innerHTML += `<option value="${d.gugunCode}">${d.gugunName}</option>`; });
    } catch (err) { console.error('시군구 조회 실패:', err); }
  }

  _url(params = {}) {
    const url = new URL(`${window.CONTEXT_PATH}/attraction`, window.location.href);
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
    });
    return url.toString();
  }

  async onSearch() {
    const { areaSelect, gugunSelect, contentTypeSelect } = this.view.el;
    if (!areaSelect.value) { alert('시도를 먼저 선택하세요.'); return; }
    try {
      const data = await (await fetch(this._url({
        action: 'list',
        areaCode: areaSelect.value,
        sigunguCode: gugunSelect.value,
        contentTypeId: contentTypeSelect.value,
        numOfRows: 50, pageNo: 1
      }))).json();
      const items = Array.isArray(data) ? data : (data.items || []);
      const spots = items
        .map(item => ({
          id: item.no,
          title: item.title || '이름 없음',
          addr: item.addr || item.addr1 || item.addr2 || '주소 정보 없음',
          lat: item.lat ?? item.latitude,
          lng: item.lng ?? item.longitude,
          image: item.image || item.firstImage || '',
          contentTypeId: item.contentTypeId
        }))
        .filter(s => s.lat !== null && s.lat !== undefined && s.lng !== null && s.lng !== undefined);

      this.model.setSpots(spots);
      this.map.clearMarkers();

      if (spots.length === 0) {
        this.view.el.tripList.innerHTML = '<p class="text-center p-5 text-muted">검색 결과가 없습니다.</p>';
        this.view.renderDistanceSummary(null, null);
        return;
      }

      this.view.renderDistanceSummary(null, null);
      this._refreshTripList();
      this.map.placeMarkers(spots, spot => this.onSelectSpot(spot));
      this.map.fitBounds(spots);
    } catch (err) {
      console.error('관광지 조회 실패:', err);
      alert('관광지 정보를 불러오지 못했습니다.');
    }
  }

  onSelectSpot(spot) {
    this.model.setBaseSpot(spot);
    this._refreshTripList();
    this.map.focusSpot(spot);
  }

  onToggleCourse(spot) {
    this.model.toggleCourse(spot);
    this._refreshTripList();
    this._renderCourse();
    this.map.drawCoursePolyline(this.model.course);
  }

  onOptimizeCourse() {
    if (this.model.course.length < 2) {
      alert('코스에 2개 이상의 관광지를 추가해주세요.');
      return;
    }
    const dist = this.model.optimizeCourse();
    this._renderCourse(dist);
    this.map.drawCoursePolyline(this.model.course);
  }

  onResetCourse() {
    if (this.model.course.length === 0) return;
    if (!confirm('코스를 초기화할까요?')) return;
    this.model.clearCourse();
    this._refreshTripList();
    this._renderCourse();
    this.map.drawCoursePolyline(this.model.course);
  }

  async onSavePlan() {
    if (this.model.course.length === 0) {
      alert('저장할 코스가 없습니다. 관광지를 코스에 추가해주세요.');
      return;
    }
    if (!window.IS_LOGGED_IN) {
      if (confirm('로그인이 필요합니다. 로그인 페이지로 이동할까요?')) {
        window.location.href = `${window.CONTEXT_PATH}/member?action=loginForm`;
      }
      return;
    }
    const name = prompt('여행 계획 이름을 입력하세요:', `여행 ${new Date().toLocaleDateString('ko-KR')}`);
    if (name === null) return;
    try {
      const saved = await this.model.savePlan(
        name.trim() || `여행 ${new Date().toLocaleDateString('ko-KR')}`,
        this.model.course
      );
      alert(`"${saved.planName}" 여행 계획이 저장되었습니다!`);
    } catch (err) {
      this._handleApiError(err);
    }
  }

  async onDeletePlan(id) {
    if (!confirm('이 여행 계획을 삭제할까요?')) return;
    try {
      await this.model.deletePlan(id);
      await this._showMyPlans();
    } catch (err) {
      this._handleApiError(err);
    }
  }

  onLoadPlan(plan) {
    this.model.course = plan.course.map(s => ({
      id: s.id,
      title: s.title,
      addr: s.addr,
      lat: s.lat,
      lng: s.lng,
      image: s.image || ''
    }));
    this._showSearch();
    this._renderCourse();
    this.map.drawCoursePolyline(this.model.course);
  }

  _showSearch() {
    this.view.showPage('search');
  }

  async _showMyPlans() {
    if (!window.IS_LOGGED_IN) {
      if (confirm('로그인이 필요합니다. 로그인 페이지로 이동할까요?')) {
        window.location.href = `${window.CONTEXT_PATH}/member?action=loginForm`;
      }
      return;
    }
    try {
      const plans = await this.model.fetchPlans();
      this.view.showPage('myplans');
      this.view.renderMyPlans(plans, {
        onDelete: id => this.onDeletePlan(id),
        onLoad: plan => this.onLoadPlan(plan),
      });
    } catch (err) {
      this._handleApiError(err);
    }
  }

  _handleApiError(err) {
    if (err.status === 401) {
      if (confirm('로그인이 필요합니다. 로그인 페이지로 이동할까요?')) {
        window.location.href = `${window.CONTEXT_PATH}/member?action=loginForm`;
      }
    } else {
      alert(err.message || '오류가 발생했습니다.');
    }
  }

  _refreshTripList() {
    const spots = this.model.getSpotsWithDistance();
    const nearest = spots.find(s => s.distanceKm > 0) || null;
    this.view.renderDistanceSummary(this.model.baseSpot, nearest);
    this.view.renderTripList(spots, this.model.course, this.model.baseSpot, {
      onSelectSpot: spot => this.onSelectSpot(spot),
      onToggleCourse: spot => this.onToggleCourse(spot),
    });
  }

  _renderCourse(optimizedDist = null) {
    this.view.renderCourse(this.model.course, optimizedDist, {
      onRemove: spot => this.onToggleCourse(spot),
      onFocus: spot => this.map.focusSpot(spot),
    });
  }
}

// ─── Bootstrap ───────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', async () => {
  const model = new TripModel();
  const mapService = new MapService();
  const view = new TripView();
  const controller = new TripController(model, view, mapService);
  await controller.init();
});
