<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>EnjoyTrip - 회원정보 수정</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
<div class="page-shell">
  <div class="container">
    <div class="account-wrap">
      <div class="account-hero">
        <div class="account-badge">ACCOUNT UPDATE</div>
        <h1 class="account-title">회원정보 수정</h1>
        <p class="account-desc">이름, 비밀번호, 이메일을 새로운 정보로 변경할 수 있습니다.</p>
      </div>

      <div class="account-card">
        <div class="account-card-head">
          <h3>정보 수정</h3>
          <p>아이디는 변경할 수 없고, 나머지 정보만 수정 가능합니다.</p>
        </div>
        <div class="account-card-body">
          <c:if test="${not empty requestScope.errorMsg}">
            <div class="alert alert-danger py-2 small mb-3">${requestScope.errorMsg}</div>
          </c:if>

          <form action="${pageContext.request.contextPath}/member" method="POST" class="account-form">
            <input type="hidden" name="action" value="update">

            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input type="text" class="form-control" value="${sessionScope.loginUser.userId}" readonly />
            </div>
            <div class="mb-3">
              <label for="userPwd" class="form-label">비밀번호</label>
              <input type="password" id="userPwd" name="userPwd" class="form-control" required />
            </div>
            <div class="mb-3">
              <label for="userName" class="form-label">이름</label>
              <input type="text" id="userName" name="userName" class="form-control" value="${sessionScope.loginUser.userName}" required />
            </div>
            <div class="mb-4">
              <label for="email" class="form-label">이메일</label>
              <input type="email" id="email" name="email" class="form-control" value="${sessionScope.loginUser.email}" required />
            </div>

            <div class="account-actions">
              <button type="submit" class="btn-sky">수정 완료</button>
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
