package com.tripcrew.user.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.user.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * users 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 * created_at / updated_at 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String email;
    private String password;   // BCrypt 해시
    private String nickname;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
