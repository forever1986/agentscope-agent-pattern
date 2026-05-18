package com.lin.quickstart.springboot.controller;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    // 自动注入ReActAgent
    @Autowired
    private ReActAgent reActAgent;

    // 开放一个call接口
    @GetMapping("/call")
    public String call(@RequestParam(name="msg") String msg){
        // 调用call方法返回数据
        Msg response = reActAgent.call(Msg.builder()
                .textContent(msg)
                .build()).block();

        return response==null?"":response.getTextContent();
    }
}
