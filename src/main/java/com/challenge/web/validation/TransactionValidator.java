package com.challenge.web.validation;

import com.challenge.transfer.TransactionInput;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class TransactionValidator implements Validator {

    protected static final String AMOUNT = "amount";
    protected static final String TIMESTAMP = "timestamp";
    protected static final String INVALID_VALUE = "invalid.value";

    @Override
    public boolean supports(Class<?> aClass) {
        return TransactionInput.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TransactionInput input = (TransactionInput) o;
        if (input.getAmount() <= 0) {
            errors.reject(AMOUNT, INVALID_VALUE);
        }

        if (input.getTimestamp() <= 0) {
            errors.reject(TIMESTAMP, INVALID_VALUE);
        }
    }
}
