package com.enjoytrip.member.model.mapper;

import com.enjoytrip.member.model.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {
    MemberDto login(@Param("userId") String userId);

    MemberDto getMemberInfo(@Param("userId") String userId);

    int signUp(MemberDto memberDto);

    int updateMember(MemberDto memberDto);

    int deleteMember(@Param("userId") String userId);

    MemberDto findPwd(Map<String, String> params);
}
