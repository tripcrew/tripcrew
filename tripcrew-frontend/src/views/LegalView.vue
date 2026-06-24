<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="legal-page">
      <div class="legal-page__inner">
        <router-link to="/" class="back-link">← TripCrew 홈으로</router-link>

        <header class="legal-header">
          <p class="legal-header__eyebrow">TRIPCREW · LEGAL</p>
          <h1>{{ document.title }}</h1>
          <p>{{ document.description }}</p>
          <p class="legal-header__updated">최종 업데이트: 2026년 6월 24일</p>
        </header>

        <aside v-if="documentType !== 'dataSources'" class="notice-box">
          <strong>안내</strong>
          <span>현재 구현된 TripCrew 기능을 기준으로 작성한 서비스 안내입니다. 상용 서비스 전에는 사업자 정보, 담당자 연락처 및 실제 운영 환경을 반영해 검토·보완해야 합니다.</span>
        </aside>

        <article class="legal-document">
          <section v-for="section in document.sections" :key="section.title" class="legal-section">
            <h2>{{ section.title }}</h2>
            <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
            <ul v-if="section.items">
              <li v-for="item in section.items" :key="item">{{ item }}</li>
            </ul>
          </section>
        </article>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AppFooter from '@/components/common/AppFooter.vue'
import AppHeader from '@/components/common/AppHeader.vue'

const props = defineProps({
  documentType: { type: String, required: true }
})

const documents = {
  terms: {
    title: 'TripCrew 이용약관',
    description: 'TripCrew가 제공하는 여행 계획 및 협업 서비스 이용에 관한 기본 사항을 안내합니다.',
    sections: [
      {
        title: '제1조 (목적 및 적용)',
        paragraphs: [
          '본 약관은 TripCrew(이하 “서비스”)가 제공하는 여행지 탐색, 여행 계획 작성, AI 기반 추천, 공동 편집, 후기 작성 및 관련 기능의 이용 조건과 절차를 정합니다.',
          '회원이 서비스를 이용하거나 회원가입 시 약관에 동의하면 본 약관에 동의한 것으로 봅니다.'
        ]
      },
      {
        title: '제2조 (계정과 회원의 책임)',
        paragraphs: ['회원은 정확한 이메일과 닉네임으로 가입해야 하며, 계정 접근 수단을 안전하게 관리해야 합니다. 타인의 계정을 사용하거나 접근 정보를 공유해서는 안 됩니다.'],
        items: [
          '회원은 자신의 계정으로 이루어진 활동에 대한 책임을 집니다.',
          '계정의 무단 사용이 의심되면 즉시 비밀번호를 변경하고 서비스 운영자에게 알려야 합니다.',
          '서비스 운영을 방해하거나 다른 이용자의 정보를 무단 수집·공개하는 행위는 금지됩니다.'
        ]
      },
      {
        title: '제3조 (서비스의 이용)',
        paragraphs: ['TripCrew는 관광지 정보 조회, 여행 일정 작성·수정, 이동 경로 최적화, 동행자 초대와 공동 편집, 챗봇 추천, 후기 및 사진 등록 기능을 제공합니다. 기능의 범위와 제공 방식은 운영상 필요에 따라 변경될 수 있습니다.'],
        items: [
          '관광 정보와 추천 결과는 여행 참고용이며, 실제 영업 여부·운영 시간·요금·안전 정보는 방문 전 해당 기관에서 다시 확인해야 합니다.',
          '공동 편집에 초대한 사용자는 계획에 포함된 내용을 볼 수 있고 편집 권한 범위 내에서 수정할 수 있습니다.',
          'AI 추천은 입력 내용을 바탕으로 생성되며, 완전성·정확성 또는 특정 결과를 보장하지 않습니다.'
        ]
      },
      {
        title: '제4조 (게시물과 금지 행위)',
        paragraphs: ['후기, 사진, 여행 계획 등 회원이 등록한 콘텐츠의 권리는 원칙적으로 회원에게 있습니다. 다만 서비스 제공, 화면 표시, 백업 및 운영을 위해 필요한 범위에서 해당 콘텐츠를 처리할 수 있습니다.'],
        items: [
          '타인의 권리, 명예, 개인정보 또는 저작권을 침해하는 콘텐츠를 등록해서는 안 됩니다.',
          '불법·유해·차별적 내용, 광고·스팸, 악성 코드 또는 서비스의 정상 운영을 방해하는 행위를 해서는 안 됩니다.',
          '운영자는 신고 또는 약관 위반이 확인된 게시물을 숨기거나 삭제하고, 필요한 경우 이용을 제한할 수 있습니다.'
        ]
      },
      {
        title: '제5조 (서비스 변경 및 책임 제한)',
        paragraphs: ['서비스는 유지보수, 외부 데이터 제공자의 장애, 통신 환경 또는 기술적 사유로 일시 중단되거나 변경될 수 있습니다. TripCrew는 법령이 허용하는 범위에서 무료로 제공되는 정보와 추천 결과의 이용으로 발생한 직접적인 여행 예약·이동·안전상 결정에 책임을 지지 않습니다.'],
      },
      {
        title: '제6조 (약관의 변경 및 문의)',
        paragraphs: ['중요한 약관 변경 사항은 서비스 내 공지로 안내합니다. 본 약관 또는 서비스 이용에 관한 문의는 서비스 내 공지된 문의 채널을 이용해 주세요.']
      }
    ]
  },
  privacy: {
    title: 'TripCrew 개인정보처리방침',
    description: 'TripCrew는 필요한 범위에서만 정보를 처리하고, 여행 계획 서비스 제공을 위해 이용합니다.',
    sections: [
      {
        title: '1. 처리하는 개인정보와 이용 목적',
        paragraphs: ['TripCrew는 회원가입과 서비스 제공에 필요한 최소한의 정보를 처리합니다.'],
        items: [
          '계정 정보: 이메일, 닉네임, 비밀번호 해시값 — 회원 식별, 로그인 및 계정 관리',
          '서비스 이용 정보: 여행 계획, 일정과 장소, 찜, 후기·평점·업로드 이미지 — 여행 계획 저장 및 커뮤니티 기능 제공',
          '협업 정보: 초대 대상 이메일, 공동 편집 이력과 접속 정보 — 동행자 초대 및 실시간 공동 편집 제공',
          '챗봇 입력 내용 및 대화 기록 — AI 여행 추천 응답 생성과 서비스 품질 확인',
          '기술 정보: 로그인 세션·토큰과 서비스 요청 기록 — 인증, 보안 및 장애 대응'
        ]
      },
      {
        title: '2. 개인정보의 보유 및 파기',
        paragraphs: ['개인정보는 서비스 이용 기간 동안 보유하며, 회원 탈퇴 또는 처리 목적이 달성되면 지체 없이 삭제 또는 익명화합니다. 다만 관계 법령에 보존 의무가 있는 정보는 해당 기간 동안 보관할 수 있습니다.', '비밀번호는 원문이 아닌 단방향 해시 형태로 저장합니다.']
      },
      {
        title: '3. 외부 서비스 및 제3자 제공',
        paragraphs: ['TripCrew의 관광지 정보는 서비스 데이터베이스에 사전 적재된 데이터를 조회해 제공합니다. 이동 경로 계산이나 AI 챗봇 기능을 사용할 때는 요청 처리에 필요한 여행 관련 입력값이 해당 기능의 외부 제공자에게 전달될 수 있습니다.', 'TripCrew는 법령상 근거가 있거나 이용자가 동의한 경우를 제외하고 개인정보를 판매하거나 제3자에게 제공하지 않습니다.']
      },
      {
        title: '4. 안전성 확보 조치',
        paragraphs: ['TripCrew는 인증이 필요한 기능을 보호하고, 비밀번호를 해시 처리하며, 접근 권한을 구분해 운영합니다. 다만 인터넷 환경에서는 어떠한 보안 조치도 완전성을 보장할 수 없으므로, 회원도 계정 정보를 안전하게 관리해야 합니다.']
      },
      {
        title: '5. 이용자의 권리와 문의',
        paragraphs: ['이용자는 자신의 개인정보에 대해 열람, 정정, 삭제 및 처리 정지를 요청할 수 있습니다. 계정 정보는 서비스의 마이페이지에서 일부 수정할 수 있으며, 그 밖의 요청은 서비스 내 공지된 문의 채널을 통해 접수할 수 있습니다.', '본 방침은 처리 방식이나 서비스 기능이 변경될 때 함께 개정될 수 있으며, 변경 내용은 서비스 내 공지로 안내합니다.']
      }
    ]
  },
  dataSources: {
    title: '관광지 데이터 안내',
    description: 'TripCrew는 사전 적재된 관광지 데이터와 여행 계획에 필요한 연계 서비스를 활용합니다.',
    sections: [
      {
        title: '사전 적재 관광지 데이터',
        paragraphs: ['관광지의 기본 정보, 지역·분류 정보, 이미지와 좌표 정보는 서비스 데이터베이스에 사전 적재해 조회합니다.', '데이터의 최신성은 적재 시점에 따라 달라질 수 있으므로, 실제 방문 전 운영 정보는 관광지 또는 공식 안내 채널에서 확인해 주세요.']
      },
      {
        title: '외부 연계 서비스',
        paragraphs: ['여행 계획의 동선 최적화와 경로 표시는 네이버 Directions 및 지도 서비스를 활용합니다. AI 챗봇 추천은 설정된 AI 제공자의 응답을 바탕으로 제공됩니다.', '연계 서비스의 제공 범위와 응답 결과는 각 제공처의 운영 상태 및 정책에 영향을 받을 수 있습니다.']
      },
      {
        title: '이용 시 유의 사항',
        paragraphs: ['TripCrew 화면의 관광 정보는 여행 계획을 돕기 위한 참고 정보입니다. 실제 방문 전 운영 시간, 휴무일, 입장료, 예약 여부 및 안전 관련 사항은 관광지 또는 공식 안내 채널에서 확인해 주세요.']
      }
    ]
  }
}

const document = computed(() => documents[props.documentType] || documents.terms)
</script>

<style scoped>
.legal-page {
  padding: 56px var(--space-6) 0;
}

.legal-page__inner {
  max-width: 860px;
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  margin-bottom: 24px;
  color: var(--teal-ink);
  font-size: 14px;
  font-weight: 700;
}

.legal-header,
.legal-document,
.notice-box {
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
}

.legal-header {
  padding: 48px;
}

.legal-header__eyebrow {
  margin-bottom: 12px;
  color: var(--teal);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.8px;
}

.legal-header h1 {
  margin-bottom: 16px;
  color: var(--ink);
  font-size: 36px;
  line-height: 1.25;
  letter-spacing: -1px;
}

.legal-header > p:not(.legal-header__eyebrow):not(.legal-header__updated) {
  color: var(--ink-3);
}

.legal-header__updated {
  margin-top: 24px;
  color: var(--ink-soft);
  font-size: 13px;
}

.notice-box {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  padding: 18px 20px;
  background: var(--teal-tint);
  border-color: var(--teal-soft);
  color: var(--ink-3);
  font-size: 14px;
  line-height: 1.55;
}

.notice-box strong { color: var(--teal-ink); white-space: nowrap; }

.legal-document {
  margin-top: 20px;
  padding: 8px 48px;
}

.legal-section {
  padding: 36px 0;
  border-bottom: 1px solid var(--line);
}

.legal-section:last-child { border-bottom: 0; }

.legal-section h2 {
  margin-bottom: 16px;
  color: var(--ink);
  font-size: 20px;
  letter-spacing: -0.3px;
}

.legal-section p,
.legal-section li {
  color: var(--ink-3);
  font-size: 15px;
  font-weight: 400;
  line-height: 1.75;
}

.legal-section p + p { margin-top: 12px; }

.legal-section ul {
  margin-top: 14px;
  padding-left: 20px;
}

.legal-section li + li { margin-top: 8px; }

@media (max-width: 640px) {
  .legal-page { padding: 32px var(--space-4) 0; }
  .legal-header, .legal-document { padding-left: 24px; padding-right: 24px; }
  .legal-header { padding-top: 32px; padding-bottom: 32px; }
  .legal-header h1 { font-size: 28px; }
  .notice-box { flex-direction: column; }
}
</style>
