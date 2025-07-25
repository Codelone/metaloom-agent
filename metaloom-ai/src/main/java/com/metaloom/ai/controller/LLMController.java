package com.metaloom.ai.controller;


import com.metaloom.model.llm.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
public class LLMController {

    @Autowired
    private ChatClientFactory chatClientFactory;


    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        ChatClient openai = chatClientFactory.getClient("openai", "deepseek-v3");
        String content = openai.prompt("请回复：你好 llm").call().content();
        return content;
    }
}