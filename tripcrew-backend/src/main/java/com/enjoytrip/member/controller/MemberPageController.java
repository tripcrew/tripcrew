package com.enjoytrip.member.controller;

import com.enjoytrip.member.model.dto.MemberDto;
import com.enjoytrip.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberPageController {

    private final MemberService memberService;

    public MemberPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping(value = "/member", params = "action=loginForm")
    public String loginForm() {
        return "login";
    }

    @GetMapping(value = "/member", params = "action=signupForm")
    public String signupForm() {
        return "signup";
    }

    @GetMapping(value = "/member", params = "action=mypage")
    public String myPage(HttpSession session) {
        return isLoggedIn(session) ? "mypage" : "redirect:/member?action=loginForm";
    }

    @GetMapping(value = "/member", params = "action=updateForm")
    public String updateForm(HttpSession session) {
        return isLoggedIn(session) ? "update" : "redirect:/member?action=loginForm";
    }

    @GetMapping(value = "/member", params = "action=deleteForm")
    public String deleteForm(HttpSession session) {
        return isLoggedIn(session) ? "delete" : "redirect:/member?action=loginForm";
    }

    @GetMapping(value = "/member", params = "action=logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping(value = "/member", params = "action=login")
    public String login(@RequestParam String userId, @RequestParam String userPwd, HttpSession session) {
        MemberDto member = memberService.login(userId, userPwd);
        if (member == null) {
            return "redirect:/member?action=loginForm&error=1";
        }
        session.setAttribute("loginUser", member);
        return "redirect:/";
    }

    @PostMapping(value = "/member", params = "action=signup")
    public String signup(
            @RequestParam String userId,
            @RequestParam String userPwd,
            @RequestParam String userName,
            @RequestParam String email) {
        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(userId);
        memberDto.setUserPassword(userPwd);
        memberDto.setUserName(userName);
        memberDto.setEmail(email);
        memberService.signUp(memberDto);
        return "redirect:/member?action=loginForm&msg=signup";
    }

    @PostMapping(value = "/member", params = "action=update")
    public String update(
            @RequestParam String userPwd,
            @RequestParam String userName,
            @RequestParam String email,
            HttpSession session) {
        MemberDto loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/member?action=loginForm";
        }

        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(loginUser.getUserId());
        memberDto.setUserPassword(userPwd);
        memberDto.setUserName(userName);
        memberDto.setEmail(email);
        memberService.updateMember(memberDto);
        session.setAttribute("loginUser", memberService.getMemberInfo(loginUser.getUserId()));
        return "redirect:/member?action=mypage";
    }

    @PostMapping(value = "/member", params = "action=delete")
    public String delete(@RequestParam String userPwd, HttpSession session) {
        MemberDto loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/member?action=loginForm";
        }
        MemberDto member = memberService.login(loginUser.getUserId(), userPwd);
        if (member == null) {
            return "redirect:/member?action=deleteForm&error=1";
        }
        memberService.deleteMember(loginUser.getUserId());
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping(value = "/member", params = "action=findpwd")
    public String findPwd(@RequestParam String findName, @RequestParam String findEmail) {
        MemberDto member = memberService.findPwd(findName, findEmail);
        if (member == null) {
            return "redirect:/member?action=loginForm&find=0";
        }
        return "redirect:/member?action=loginForm&find=1&userId=" + member.getUserId();
    }

    private boolean isLoggedIn(HttpSession session) {
        return getLoginUser(session) != null;
    }

    private MemberDto getLoginUser(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        return loginUser instanceof MemberDto memberDto ? memberDto : null;
    }
}
