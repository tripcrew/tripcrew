<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>EnjoyTrip</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
  <script src="${pageContext.request.contextPath}/js/config.js"></script>
  <script>
    window.CONTEXT_PATH = '${pageContext.request.contextPath}';
    <c:choose>
      <c:when test="${not empty sessionScope.loginUser}">window.IS_LOGGED_IN = true;</c:when>
      <c:otherwise>window.IS_LOGGED_IN = false;</c:otherwise>
    </c:choose>
  </script>
  <script src="${pageContext.request.contextPath}/js/trip.js"></script>
</head>
<body>
  <header class="site-header">
    <div class="container header-inner">
      <div class="logo">
        <a href="${pageContext.request.contextPath}/">
          <h2 class="mb-0">enjoy <span>trip</span></h2>
        </a>
      </div>
      <nav>
        <ul class="nav-list mb-0">
          <li><a href="${pageContext.request.contextPath}/" id="nav-search">지역별 여행지</a></li>
          <li><a href="#" id="nav-myplans">나의 여행계획</a></li>
          <c:choose>
            <c:when test="${not empty sessionScope.loginUser}">
              <li>
                <a href="${pageContext.request.contextPath}/member?action=mypage" class="fw-bold text-primary">
                  ${sessionScope.loginUser.userName}님
                </a>
              </li>
              <li><a href="${pageContext.request.contextPath}/member?action=logout">로그아웃</a></li>
            </c:when>
            <c:otherwise>
              <li><a href="${pageContext.request.contextPath}/member?action=loginForm">로그인</a></li>
              <li><a href="${pageContext.request.contextPath}/member?action=signupForm">회원가입</a></li>
            </c:otherwise>
          </c:choose>
        </ul>
      </nav>
    </div>
  </header>

  <main class="trip-page">
    <div class="container">
      <div id="search-main-section">
        <section class="hero-section">
          <div class="hero-copy">
            <p class="hero-badge">LET'S GO TO TRAVEL</p>
            <h1 class="section-title">어디로 떠나볼까요?</h1>
            <p class="section-desc">
              시도와 시군구, 관광지 유형을 선택하고 나만의 여행지를 찾아보세요.
            </p>
          </div>

          <div class="search-panel">
            <div class="search-grid">
              <div class="select-group">
                <label for="areaSelect">시도</label>
                <select id="areaSelect" class="form-select custom-select">
                  <option value="">시도 선택</option>
                </select>
              </div>
              <div class="select-group">
                <label for="gugunSelect">시군구</label>
                <select id="gugunSelect" class="form-select custom-select">
                  <option value="">시군구 선택</option>
                </select>
              </div>
              <div class="select-group">
                <label for="contentTypeSelect">관광지 유형</label>
                <select id="contentTypeSelect" class="form-select custom-select">
                  <option value="">관광지 유형</option>
                  <option value="12">관광지</option>
                  <option value="14">문화시설</option>
                  <option value="15">공연/행사/축제</option>
                  <option value="25">여행코스</option>
                  <option value="32">숙박</option>
                  <option value="38">쇼핑</option>
                  <option value="39">음식점</option>
                </select>
              </div>
              <button id="searchBtn" class="search-btn" type="button">
                <span>검색</span>
              </button>
            </div>
          </div>
        </section>

        <section class="course-section" id="course-section" style="display:none;">
          <div class="course-head">
            <div class="course-head-copy">
              <h3>여행 코스</h3>
              <p id="course-meta">선택한 관광지를 최적 동선으로 정리해보세요.</p>
            </div>
            <div class="course-actions">
              <button id="optimizeBtn" class="btn-sky" type="button">동선 최적화</button>
              <button id="savePlanBtn" class="btn-mint" type="button">여행 계획 저장</button>
              <button id="resetCourseBtn" class="btn-soft" type="button">코스 초기화</button>
            </div>
          </div>
          <div id="course-chips" class="course-chips"></div>
        </section>

        <section class="content-section">
          <div class="row g-4">
            <div class="col-lg-4">
              <div class="list-wrapper">
                <div class="panel-head">
                  <div>
                    <h3>추천 여행지</h3>
                    <p>선택한 조건에 맞는 장소를 확인해보세요.</p>
                  </div>
                </div>
                <div id="distance-summary" class="distance-summary">
                  <span>관광지를 선택하면 가까운 곳을 추천해드립니다.</span>
                </div>
                <div id="trip-list" class="trip-list-scroll"></div>
              </div>
            </div>
            <div class="col-lg-8">
              <div class="map-wrapper">
                <div class="panel-head map-head">
                  <div>
                    <h3>지도에서 보기</h3>
                    <p>선택한 여행지를 지도에서 확인해보세요.</p>
                  </div>
                </div>
                <div id="map" class="map-box"></div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <section class="myplans-section" id="myplans-section" style="display:none;">
        <div class="myplans-hero">
          <p class="hero-badge">MY TRAVEL PLAN</p>
          <h1 class="section-title">나의 여행 계획</h1>
          <p class="section-desc">저장한 여행 코스를 확인하고 다시 불러올 수 있습니다.</p>
        </div>
        <div id="myplans-list" class="myplans-list"></div>
      </section>
    </div>
  </main>
</body>
</html>
