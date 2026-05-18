package com.lin.guardrail.base;

import java.util.List;

public class GuardrailException extends RuntimeException {

    private List<String> errorsMsg;

    public GuardrailException(List<String> errorsMsg) {
        this.errorsMsg = errorsMsg;
    }

    public List<String> getErrorsMsg() {
        return errorsMsg;
    }
}
