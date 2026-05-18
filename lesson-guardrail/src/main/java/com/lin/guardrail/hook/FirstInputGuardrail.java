package com.lin.guardrail.hook;

import com.lin.guardrail.base.InputGuardrail;
import com.lin.guardrail.base.Result;
import io.agentscope.core.message.Msg;

public class FirstInputGuardrail extends InputGuardrail {


    public FirstInputGuardrail(String guardrailName) {
        super(guardrailName);
    }

    @Override
    protected Result validate(Msg msg) {
        System.out.println("========="+ getGuardrailName() + "===========");
        return Result.SUCCESS;
    }

}
