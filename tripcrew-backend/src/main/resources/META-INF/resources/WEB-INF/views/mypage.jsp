<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>EnjoyTrip - 회원정보 조회</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
<div class="page-shell">
  <div class="container">
    <div class="account-wrap">
      <div class="account-hero">
        <div class="account-badge">MY PAGE</div>
        <h1 class="account-title">회원정보 조회</h1>
        <p class="account-desc">현재 로그인한 회원의 정보를 확인할 수 있습니다.</p>
      </div>

      <div class="account-card">
        <div class="account-card-head">
          <h3>내 정보</h3>
          <p>EnjoyTrip 계정 정보를 확인해보세요.</p>
        </div>
        <div class="account-card-body">
          <div class="profile-grid">
            <div class="profile-item">
              <span class="profile-label">아이디</span>
              <span class="profile-value">${sessionScope.loginUser.userId}</span>
            </div>
            <div class="profile-item">
              <span class="profile-label">이름</span>
              <span class="profile-value">${sessionScope.loginUser.userName}</span>
            </div>
            <div class="profile-item">
              <span class="profile-label">이메일</span>
              <span class="profile-value">${sessionScope.loginUser.email}</span>
            </div>
          </div>

          <div class="account-actions">
            <a href="${pageContext.request.contextPath}/member?action=updateForm" class="btn-sky">회원정보 수정</a>
            <a href="${pageContext.request.contextPath}/member?action=deleteForm" class="btn-danger-soft">회원 탈퇴</a>
            <a href="${pageContext.request.contextPath}/" class="btn-soft">메인으로</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
