package com.enjoytrip.member.service;

import com.enjoytrip.member.model.dto.MemberDto;
import com.enjoytrip.member.model.mapper.MemberMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberDto login(String userId, String userPassword) {
        MemberDto member = memberMapper.login(userId);
        if (member != null && passwordEncoder.matches(userPassword, member.getUserPassword())) {
            member.setUserPassword(null);
            return member;
        }
        return null;
    }

    public MemberDto getMemberInfo(String userId) {
        MemberDto member = memberMapper.getMemberInfo(userId);
        if (member != null) {
            member.setUserPassword(null);
        }
        return member;
    }

    public int signUp(MemberDto memberDto) {
        memberDto.setUserPassword(passwordEncoder.encode(memberDto.getUserPassword()));
        return memberMapper.signUp(memberDto);
    }

    public int updateMember(MemberDto memberDto) {
        if (memberDto.getUserPassword() != null && !memberDto.getUserPassword().isBlank()) {
            memberDto.setUserPassword(passwordEncoder.encode(memberDto.getUserPassword()));
        } else {
            memberDto.setUserPassword(null);
        }
        return memberMapper.updateMember(memberDto);
    }

    public int deleteMember(String userId) {
        return memberMapper.deleteMember(userId);
    }

    public MemberDto findPwd(String name, String email) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        MemberDto member = memberMapper.findPwd(params);
        if (member != null) {
            member.setUserPassword(null);
        }
        return member;
    }
}
