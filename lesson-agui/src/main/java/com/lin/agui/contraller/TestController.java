package com.lin.agui.contraller;

import io.agentscope.core.agui.encoder.AguiEventEncoder;
import io.agentscope.core.agui.event.AguiEvent;
import io.agentscope.spring.boot.agui.mvc.AguiMvcController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
public class TestController {

    @GetMapping("/send")
    public Boolean send() throws IOException {
        AguiEvent aguiEvent = new AguiEvent.TextMessageContent("session-main", "run-default","test", "已经梳理完任务");
        AguiEventEncoder encoder = new AguiEventEncoder();
        String jsonData = encoder.encodeToJson(aguiEvent);
        AguiMvcController.sseEmitterMap.get("run-default").send(SseEmitter.event().data(jsonData, MediaType.APPLICATION_JSON));
        return true;
    }
}
