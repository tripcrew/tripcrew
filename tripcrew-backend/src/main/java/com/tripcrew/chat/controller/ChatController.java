package com.tripcrew.chat.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.chat.model.dto.ChatMessageRequest;
import com.tripcrew.chat.model.dto.ChatMessageResponse;
import com.tripcrew.chat.service.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public ChatMessageResponse send(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody ChatMessageRequest request) {
        return chatService.send(userId, request);
    }
}
