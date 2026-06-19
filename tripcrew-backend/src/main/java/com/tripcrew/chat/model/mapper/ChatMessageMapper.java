package com.tripcrew.chat.model.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tripcrew.chat.model.dto.ChatMessage;

@Mapper
public interface ChatMessageMapper {

    void insert(ChatMessage message);
}