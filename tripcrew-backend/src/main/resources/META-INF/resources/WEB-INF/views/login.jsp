<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>EnjoyTrip - 로그인</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-bg">
<div class="container">
  <div class="row justify-content-center min-vh-100 align-items-center">
    <div class="col-md-5 col-lg-4">
      <div class="card shadow-lg">
        <div class="card-body p-5">
          <div class="text-center mb-4">
            <a href="${pageContext.request.contextPath}/" class="text-decoration-none">
              <h2 class="section-title">EnjoyTrip</h2>
            </a>
            <p class="text-muted">다시 만나서 반가워요!</p>
          </div>

          <c:if test="${param.msg == 'signup'}">
            <div class="alert alert-success py-2 text-center small">회원가입이 완료되었습니다. 로그인해주세요.</div>
          </c:if>

          <c:if test="${param.error == '1'}">
            <div class="alert alert-danger py-2 text-center small">아이디 또는 비밀번호가 올바르지 않습니다.</div>
          </c:if>

          <c:if test="${param.find == '1'}">
            <div class="alert alert-info py-2 text-center small">일치하는 회원을 찾았습니다. 관리자에게 비밀번호 확인을 요청하세요.</div>
          </c:if>

          <c:if test="${param.find == '0'}">
            <div class="alert alert-warning py-2 text-center small">일치하는 회원 정보를 찾지 못했습니다.</div>
          </c:if>

          <form action="${pageContext.request.contextPath}/member" method="POST">
            <input type="hidden" name="action" value="login">
            <div class="mb-3">
              <label for="userId" class="form-label">아이디</label>
              <input type="text" id="userId" name="userId" class="form-control" placeholder="아이디를 입력하세요" required>
            </div>
            <div class="mb-4">
              <label for="userPwd" class="form-label">비밀번호</label>
              <input type="password" id="userPwd" name="userPwd" class="form-control" placeholder="비밀번호를 입력하세요" required>
            </div>
            <button type="submit" class="btn btn-primary w-100 shadow-sm mb-3">로그인</button>
          </form>

          <div class="text-center">
            <a href="#" class="text-decoration-none me-3" data-bs-toggle="modal" data-bs-target="#findPwdModal">비밀번호 찾기</a>
            <small class="text-muted">|</small>
            <a href="${pageContext.request.contextPath}/member?action=signupForm" class="text-decoration-none ms-3">회원가입</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="findPwdModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">비밀번호 찾기</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form action="${pageContext.request.contextPath}/member" method="POST">
          <input type="hidden" name="action" value="findpwd">
          <p class="text-muted small">가입 시 등록한 이름과 이메일을 입력해주세요.</p>
          <div class="mb-3">
            <label for="findName" class="form-label">이름</label>
            <input type="text" class="form-control" id="findName" name="findName" required>
          </div>
          <div class="mb-3">
            <label for="findEmail" class="form-label">이메일</label>
            <input type="email" class="form-control" id="findEmail" name="findEmail" required>
          </div>
          <button type="submit" class="btn btn-primary w-100">찾기</button>
        </form>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
