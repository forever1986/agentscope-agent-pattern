package com.lin.guardrail.hook;

import com.lin.guardrail.base.InputGuardrail;
import com.lin.guardrail.base.Result;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;

import java.util.List;

public class FourthInputGuardrail extends InputGuardrail {


    public FourthInputGuardrail(String guardrailName) {
        super(guardrailName);
    }

    @Override
    protected Result validate(Msg msg) {
        System.out.println("========="+ getGuardrailName() + "===========");
        return Result.FATAL;
    }

}
