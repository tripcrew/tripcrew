package com.tripcrew.tripplan.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

/**
 * 낙관적 락 충돌. 클라이언트가 보낸 version 이 서버의 현재 version 과 달라
 * (다른 사용자가 먼저 수정) UPDATE 가 0행을 갱신했을 때 발생. 409.
 */
public class OptimisticLockConflictException extends BusinessException {

    public OptimisticLockConflictException() {
        super(HttpStatus.CONFLICT, "다른 사용자가 먼저 수정했습니다. 최신 내용을 다시 불러와 주세요.");
    }
}
