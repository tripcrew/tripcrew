<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>EnjoyTrip - 회원 탈퇴</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
<div class="page-shell">
  <div class="container">
    <div class="account-wrap">
      <div class="account-hero">
        <div class="account-badge">DELETE ACCOUNT</div>
        <h1 class="account-title danger-title">회원 탈퇴</h1>
        <p class="account-desc">탈퇴 전에 비밀번호를 다시 한 번 확인해주세요.</p>
      </div>

      <div class="account-card">
        <div class="account-card-head">
          <h3>계정 삭제</h3>
          <p>탈퇴 후 계정 정보는 복구할 수 없습니다.</p>
        </div>
        <div class="account-card-body">
          <div class="danger-note">
            <div class="danger-note-icon">!</div>
            <div>
              <strong>주의가 필요해요</strong>
              <p>회원 탈퇴를 진행하면 저장된 회원 정보가 삭제되고, 이후에는 같은 로그인 상태를 유지할 수 없습니다.</p>
            </div>
          </div>

          <c:if test="${not empty requestScope.errorMsg}">
            <div class="alert alert-danger py-2 small mb-3">${requestScope.errorMsg}</div>
          </c:if>

          <form action="${pageContext.request.contextPath}/member" method="POST" class="account-form">
            <input type="hidden" name="action" value="delete">
            <div class="mb-4">
              <label for="userPwd" class="form-label">비밀번호 확인</label>
              <input type="password" id="userPwd" name="userPwd" class="form-control" required />
            </div>
            <div class="account-actions">
              <button type="submit" class="btn-danger-soft">회원 탈퇴</button>
              <a href="${pageContext.request.contextPath}/member?action=mypage" class="btn-soft">취소</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
