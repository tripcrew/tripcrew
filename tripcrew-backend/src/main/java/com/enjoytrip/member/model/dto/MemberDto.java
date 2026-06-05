package com.enjoytrip.member.model.dto;

import lombok.Data;

@Data
public class MemberDto {

    private String userId;
    private String userPassword;
    private String userName;
    private String email;
    private String createdAt;

    public String getUserPwd() {
        return userPassword;
    }

    public void setUserPwd(String userPwd) {
        this.userPassword = userPwd;
    }

    public String getEmailId() {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex >= 0 ? email.substring(0, atIndex) : email;
    }

    public void setEmailId(String emailId) {
        setEmailParts(emailId, getEmailDomain());
    }

    public String getEmailDomain() {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex >= 0 && atIndex + 1 < email.length() ? email.substring(atIndex + 1) : null;
    }

    public void setEmailDomain(String emailDomain) {
        setEmailParts(getEmailId(), emailDomain);
    }

    private void setEmailParts(String emailId, String emailDomain) {
        if (emailId == null || emailId.isBlank()) {
            this.email = null;
            return;
        }
        if (emailDomain == null || emailDomain.isBlank()) {
            this.email = emailId;
            return;
        }
        this.email = emailId + "@" + emailDomain;
    }
}
