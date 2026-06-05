<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>EnjoyTrip - 회원가입</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-bg">
<div class="container">
  <div class="row justify-content-center min-vh-100 align-items-center">
    <div class="col-md-6 col-lg-5">
      <div class="card shadow-lg my-5">
        <div class="card-body p-5">
          <div class="text-center mb-4">
            <a href="${pageContext.request.contextPath}/" class="text-decoration-none">
              <h2 class="section-title">회원가입</h2>
            </a>
            <p class="text-muted">EnjoyTrip와 함께 여행을 시작해보세요.</p>
          </div>

          <c:if test="${not empty requestScope.errorMsg}">
            <div class="alert alert-danger py-2 text-center small">${requestScope.errorMsg}</div>
          </c:if>

          <form action="${pageContext.request.contextPath}/member" method="POST">
            <input type="hidden" name="action" value="signup">
            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input type="text" name="userId" class="form-control" placeholder="아이디" required>
            </div>
            <div class="mb-3">
              <label class="form-label">비밀번호</label>
              <input type="password" name="userPwd" class="form-control" placeholder="비밀번호" required>
            </div>
            <div class="mb-3">
              <label class="form-label">이름</label>
              <input type="text" name="userName" class="form-control" placeholder="이름" required>
            </div>
            <div class="mb-4">
              <label class="form-label">이메일</label>
              <input type="email" name="email" class="form-control" placeholder="example@tripcrew.com" required>
            </div>
            <button type="submit" class="btn btn-primary w-100 shadow-sm">가입 완료</button>
          </form>

          <div class="mt-4 text-center">
            <small>이미 계정이 있나요?
              <a href="${pageContext.request.contextPath}/member?action=loginForm" class="text-decoration-none">로그인하기</a>
            </small>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
