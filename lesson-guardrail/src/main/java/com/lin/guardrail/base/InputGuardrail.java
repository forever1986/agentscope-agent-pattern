package com.lin.guardrail.base;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreCallEvent;
import io.agentscope.core.message.Msg;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public abstract class InputGuardrail implements Hook {

    private String guardrailName;
    private String errorsMsg;

    public InputGuardrail(String guardrailName) {
        this.guardrailName = guardrailName;
    }


    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {

        if( event instanceof PreCallEvent preCallEvent){
            Result result = validate(preCallEvent.getInputMessages().getLast());
            if(result.equals(Result.FAIL)){
                setErrorsMsg("审核不通过" + guardrailName);
            }else if(result.equals(Result.FATAL)){
                setErrorsMsg("审核不通过" + guardrailName);
                throwException(preCallEvent);
            }

            // 模拟只实现ReActAgent
            List<Hook> hooks = ((ReActAgent)event.getAgent()).getHooks();
            for(int i = hooks.size()-1; i >=0 ; i--){
                if(hooks.get(i) instanceof InputGuardrail){
                    // 判断自己是不是最后一个
                    if(this.equals(hooks.get(i))){
                        throwException(preCallEvent);
                    }else{
                        break;
                    }
                }
            }
        }
        return Mono.just(event);
    }

    private void throwException(PreCallEvent preCallEvent){
        List<Hook> hooks = ((ReActAgent)preCallEvent.getAgent()).getHooks();
        // 如果是最后一个
        List<String> errorMsgs = new ArrayList<>();
        for(int j = 0; j < hooks.size() ; j++){
            // 加入之前的错误信息
            if(hooks.get(j) instanceof InputGuardrail){
                String msg = ((InputGuardrail)hooks.get(j)).getErrorsMsg();
                if(msg != null)
                    errorMsgs.add(msg);
            }
        }
        throw new GuardrailException(errorMsgs); // 抛出异常
    }

    protected abstract Result validate(Msg msg);

    public String getErrorsMsg() {
        return errorsMsg;
    }

    public void setErrorsMsg(String errorsMsg) {
        this.errorsMsg = errorsMsg;
    }

    public String getGuardrailName() {
        return guardrailName;
    }

    public void setGuardrailName(String guardrailName) {
        this.guardrailName = guardrailName;
    }
}
