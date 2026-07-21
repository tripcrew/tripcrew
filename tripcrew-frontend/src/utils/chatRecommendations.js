/**
 * 챗봇 답변(자유 텍스트 마크다운)에서 "추천 관광지 이름" 후보를 뽑아내고,
 * 우리 DB 검색 결과와 이름이 실제로 겹치는지 판정하는 순수 함수 모음.
 *
 * 배경: 챗봇 응답에는 관광지 no(우리 DB id)가 없다. 그래서 여기서 이름 후보를
 * 추출 → 프론트에서 attractionApi.search(keyword) 로 조회 → isNameMatch 로
 * 실제 매칭된 것만 카드로 만든다. 매칭 안 되는(=DB에 없거나 지어낸) 장소는
 * 자연스럽게 탈락한다(환각 방어).
 */

const MAX_CANDIDATES = 12

// 후보에서 떼어낼 흔한 접미 동작어/조사(장소명 자체가 아닌 꼬리)
const TRAILING_NOISE = /\s*(?:을|를|은|는|이|가|에서|에게|으로|로|에)?\s*(?:방문|관람|관람사|구경|둘러보기|둘러보고|체험|산책|탐방|투어|일정|코스|추천|즐기기|구경하기|감상|이용)?\s*$/

// 후보로 잡혀도 무의미한 일반 단어(장소명이 아님) — 주로 구획 라벨/일반명사
const STOPWORDS = new Set([
  '오전', '오후', '아침', '점심', '저녁', '숙소', '호텔', '식당', '맛집',
  '이동', '출발', '도착', '여행', '코스', '일정', '추천', '주의사항', '요약',
  '근처', '주변', '일대', '방문', '관람', '체험', '산책', '대중교통', '자차',
  '렌터카', '택시', '시내버스', '기차', '버스', 'TripBot', '팁', '참고',
])

// 순수 일차/시간 라벨(1일 차, 2일차, Day 1 등) — 장소가 아님
const DAY_LABEL = /^(?:day\s*)?\d+\s*일?\s*차?$/i

/**
 * 챗봇 답변(마크다운)에서 장소명 후보 배열을 추출한다.
 * Gemini 의 볼드/형식에 의존하지 않는다: 괄호(설명·음식)를 걷어내고, 코스 문장을
 * 화살표·불릿·콜론·중점·슬래시로 잘게 쪼개 후보를 모은다. 잘못 뽑혀도 최종적으로
 * DB 검색 매칭(isNameMatch)이 걸러주므로 "덜 뽑기보다 넉넉히 뽑고 거른다".
 */
export function extractPlaceNames(markdown) {
  let text = String(markdown || '')
  // 괄호/대괄호 내용 제거: (해변 산책)·(닭강정, 씨앗호떡 등)·(케이블카 이용 권장) 등 설명/음식/교통
  text = text.replace(/\([^)]*\)/g, ' ').replace(/\[[^\]]*\]/g, ' ').replace(/\*\*/g, '')

  const raw = []
  text.split(/\r?\n/).forEach((line) => {
    // 화살표·불릿·중점·슬래시·세미콜론·쉼표로 1차 분해
    line.split(/→|⇒|➜|▶|▷|·|\/|;|,|、/).forEach((chunk) => {
      // "1일 차: 속초아이 대관람차" 처럼 콜론이 있으면 콜론 뒤(=실제 장소)만 취함
      const seg = chunk.includes(':') || chunk.includes('：') ? chunk.split(/[:：]/).pop() : chunk
      raw.push(seg)
    })
  })

  const seen = new Set()
  const result = []
  for (const candidate of raw) {
    const cleaned = cleanCandidate(candidate)
    if (!isViableCandidate(cleaned)) continue
    if (seen.has(cleaned)) continue
    seen.add(cleaned)
    result.push(cleaned)
    if (result.length >= MAX_CANDIDATES) break
  }
  return result
}

/** 후보 문자열 정리: 마크다운/시간/불릿·번호/따옴표/꼬리 동작어 제거 */
export function cleanCandidate(value) {
  return String(value || '')
    .replace(/\d{1,2}\s*:\s*\d{2}/g, '') // 09:00 같은 시각
    .replace(/^[\s\d.)\-–—*·•~]+/, '') // 앞쪽 번호/불릿/대시
    .replace(/["'“”‘’`]/g, '')
    .replace(/[.!?…]+$/g, '') // 문장 끝 구두점
    .replace(TRAILING_NOISE, '')
    .replace(/\s+/g, ' ')
    .trim()
}

function isViableCandidate(cleaned) {
  if (!cleaned || cleaned.length < 2) return false
  if (cleaned.length > 20) return false // 문장 통째로 잡힌 것 배제
  if ((cleaned.match(/\s/g) || []).length >= 3) return false // 3어절 이상 = 문장 조각(장소명은 보통 1~2어절)
  if (STOPWORDS.has(cleaned)) return false
  if (DAY_LABEL.test(cleaned)) return false
  // 한글/영문/숫자로 시작하는 명사 형태만 (문장부호로 시작하는 잔여물 제거)
  if (!/^[가-힣A-Za-z0-9]/.test(cleaned)) return false
  // 한글이 전혀 없고 숫자만인 것 등 배제(영문 지명은 허용)
  if (/^\d+$/.test(cleaned)) return false
  return true
}

/** 검색 결과 title 과 후보명이 실제로 같은 장소를 가리키는지 판정 */
export function isNameMatch(candidate, title) {
  const a = normalizeName(candidate)
  const b = normalizeName(title)
  if (!a || !b) return false
  if (a === b) return true
  // 한쪽이 다른 쪽을 포함하되, 짧은 쪽이 최소 3자 이상일 때만 인정(과매칭 방지)
  const shorter = a.length <= b.length ? a : b
  const longer = a.length <= b.length ? b : a
  if (shorter.length >= 3 && longer.includes(shorter)) return true
  return false
}

/** 이름 비교용 정규화: 공백 제거 + 이름 끝/앞의 #숫자(id) 흔적 제거 */
export function normalizeName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s*\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s*)+/g, '')
    .replace(/\s+/g, '')
}
