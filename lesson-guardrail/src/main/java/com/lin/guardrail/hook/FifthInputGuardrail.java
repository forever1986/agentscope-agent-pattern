package com.lin.guardrail.hook;

import com.lin.guardrail.base.InputGuardrail;
import com.lin.guardrail.base.Result;
import io.agentscope.core.message.Msg;

public class FifthInputGuardrail extends InputGuardrail {


    public FifthInputGuardrail(String guardrailName) {
        super(guardrailName);
    }

    @Override
    protected Result validate(Msg msg) {
        System.out.println("========="+ getGuardrailName() + "===========");
        return Result.SUCCESS;
    }

}
