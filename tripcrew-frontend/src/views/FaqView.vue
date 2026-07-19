<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="faq-page">
      <div class="faq-page__inner">
        <router-link to="/" class="back-link">← TripCrew 홈으로</router-link>

        <header class="faq-header">
          <p class="faq-header__eyebrow">TRIPCREW · SUPPORT</p>
          <h1>자주 묻는 질문</h1>
          <p>TripCrew를 이용하며 자주 궁금해하는 내용을 모았습니다.</p>
        </header>

        <section class="faq-list" aria-label="자주 묻는 질문 목록">
          <article v-for="(item, index) in faqs" :key="item.question" class="faq-item">
            <h2>
              <button
                :id="`faq-button-${index}`"
                type="button"
                class="faq-question"
                :aria-expanded="activeIndex === index"
                :aria-controls="`faq-answer-${index}`"
                @click="toggleAnswer(index)"
              >
                <span class="faq-question__number">Q{{ String(index + 1).padStart(2, '0') }}</span>
                <span>{{ item.question }}</span>
                <span class="faq-question__icon" aria-hidden="true">{{ activeIndex === index ? '−' : '+' }}</span>
              </button>
            </h2>
            <div
              v-show="activeIndex === index"
              :id="`faq-answer-${index}`"
              class="faq-answer"
              role="region"
              :aria-labelledby="`faq-button-${index}`"
            >
              <p>{{ item.answer }}</p>
              <router-link v-if="item.link" :to="item.link.to" class="faq-answer__link">{{ item.link.label }} →</router-link>
            </div>
          </article>
        </section>

        <aside class="help-box">
          <div>
            <strong>원하는 답변을 찾지 못하셨나요?</strong>
            <p>문의 기능을 준비하고 있습니다. 현재는 공지사항에서 서비스 소식을 확인해 주세요.</p>
          </div>
          <router-link to="/support" class="help-box__link">1:1 문의 안내 보기</router-link>
        </aside>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppFooter from '@/components/common/AppFooter.vue'
import AppHeader from '@/components/common/AppHeader.vue'

const activeIndex = ref(null)

const faqs = [
  {
    question: '회원가입과 로그인은 어떻게 하나요?',
    answer: '상단의 회원가입에서 이메일, 닉네임, 비밀번호를 입력해 계정을 만들 수 있습니다. 이미 계정이 있다면 같은 화면에서 로그인 탭을 선택해 주세요.',
    link: { to: '/auth', label: '회원가입·로그인으로 이동' }
  },
  {
    question: '여행 계획은 어떻게 만들고 동행자와 공유하나요?',
    answer: '로그인 후 내 여행 계획에서 새 계획을 만들고 관광지를 일정에 추가할 수 있습니다. 계획 편집 화면에서 동행자의 이메일을 초대하면 권한 범위 안에서 함께 일정을 편집할 수 있습니다.',
    link: { to: '/plans', label: '내 여행 계획으로 이동' }
  },
  {
    question: '후기와 평점, 사진을 등록하려면 어떻게 하나요?',
    answer: '관광지 상세 화면의 후기 메뉴에서 방문 경험과 평점을 남길 수 있습니다. 사진은 안내된 형식과 개수 제한 안에서 첨부할 수 있으며, 타인의 권리나 개인정보를 침해하는 콘텐츠는 등록하면 안 됩니다.'
  },
  {
    question: '관광지를 찜 목록에 저장할 수 있나요?',
    answer: '관광지 상세에서 가보고 싶은 장소를 찜할 수 있습니다. 저장한 장소는 찜 목록에서 모아 보고 여행 계획에 추가할 수 있습니다.',
    link: { to: '/wishlist', label: '찜 목록으로 이동' }
  },
  {
    question: 'AI 챗봇은 어떻게 사용하나요?',
    answer: '여행할 지역, 기간, 인원, 원하는 분위기처럼 조건을 자연스럽게 입력해 보세요. 챗봇이 여행 계획 초안을 제안하며, 추천 결과는 실제 방문 전 운영 정보와 함께 확인하는 것을 권장합니다.',
    link: { to: '/chat', label: 'AI 챗봇으로 이동' }
  },
  {
    question: '관광 데이터는 어디에서 제공되나요?',
    answer: 'TripCrew는 한국관광공사 TourAPI의 공공 관광 데이터를 바탕으로 여행지 정보를 제공합니다. 정보의 최신성은 원 제공처의 갱신 상황에 따라 달라질 수 있습니다.',
    link: { to: '/data-sources', label: '공공데이터 출처 보기' }
  },
  {
    question: '서비스 변경 사항이나 점검 소식은 어디서 확인하나요?',
    answer: '기능 변경, 점검, 중요 안내 사항은 공지사항에서 안내합니다.',
    link: { to: '/notices', label: '공지사항으로 이동' }
  }
]

function toggleAnswer(index) {
  activeIndex.value = activeIndex.value === index ? null : index
}
</script>

<style scoped>
.faq-page { padding: 56px var(--space-6) 0; }
.faq-page__inner { max-width: 860px; margin: 0 auto; }

.back-link {
  display: inline-flex;
  margin-bottom: 24px;
  color: var(--teal-ink);
  font-size: 14px;
  font-weight: 700;
}

.faq-header, .faq-list, .help-box {
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  background: var(--bg);
}

.faq-header { padding: 48px; }
.faq-header__eyebrow {
  margin-bottom: 12px;
  color: var(--teal);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.8px;
}
.faq-header h1 {
  margin-bottom: 16px;
  color: var(--ink);
  font-size: 36px;
  line-height: 1.25;
  letter-spacing: -1px;
}
.faq-header > p:last-child { color: var(--ink-3); }

.faq-list { margin-top: 20px; overflow: hidden; }
.faq-item + .faq-item { border-top: 1px solid var(--line); }
.faq-question {
  display: grid;
  grid-template-columns: auto 1fr auto;
  width: 100%;
  gap: 16px;
  padding: 22px 24px;
  text-align: left;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}
.faq-question:hover { background: var(--teal-tint); }
.faq-question__number {
  color: var(--teal);
  font-family: var(--font-mono);
  font-size: 13px;
}
.faq-question__icon { color: var(--teal); font-size: 22px; font-weight: 400; line-height: 1; }
.faq-answer {
  padding: 0 56px 24px;
  color: var(--ink-3);
  font-size: 15px;
  font-weight: 400;
  line-height: 1.75;
}
.faq-answer__link {
  display: inline-flex;
  margin-top: 14px;
  color: var(--teal-ink);
  font-size: 14px;
  font-weight: 700;
}

.help-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 20px;
  padding: 24px;
  background: var(--coral-tint);
  border-color: var(--coral-soft);
}
.help-box strong { color: var(--ink); }
.help-box p { margin-top: 4px; color: var(--ink-3); font-size: 14px; }
.help-box__link { color: var(--coral-ink); font-size: 14px; font-weight: 700; white-space: nowrap; }

@media (max-width: 640px) {
  .faq-page { padding: 32px var(--space-4) 0; }
  .faq-header { padding: 32px 24px; }
  .faq-header h1 { font-size: 26px; }
  .faq-question { padding: 20px; gap: 12px; font-size: 15px; }
  .faq-answer { padding: 0 20px 20px; }
  .help-box { align-items: flex-start; flex-direction: column; }
}
</style>
