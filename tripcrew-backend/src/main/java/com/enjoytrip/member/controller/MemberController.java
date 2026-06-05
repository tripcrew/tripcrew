package com.enjoytrip.member.controller;

import com.enjoytrip.member.model.dto.MemberDto;
import com.enjoytrip.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<MemberDto> login(@RequestBody MemberDto loginRequest, HttpSession session) {
        MemberDto member = memberService.login(loginRequest.getUserId(), loginRequest.getUserPassword());
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        session.setAttribute("loginUser", member);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Integer> signup(@RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(memberService.signUp(memberDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<MemberDto> getMemberInfo(@PathVariable String userId) {
        return ResponseEntity.ok(memberService.getMemberInfo(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Integer> updateMember(@PathVariable String userId, @RequestBody MemberDto memberDto) {
        memberDto.setUserId(userId);
        return ResponseEntity.ok(memberService.updateMember(memberDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Integer> deleteMember(@PathVariable String userId) {
        return ResponseEntity.ok(memberService.deleteMember(userId));
    }

    @PostMapping("/find-pwd")
    public ResponseEntity<MemberDto> findPwd(@RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(memberService.findPwd(memberDto.getUserName(), memberDto.getEmail()));
    }
}
