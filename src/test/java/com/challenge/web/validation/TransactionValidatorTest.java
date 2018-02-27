package com.challenge.web.validation;

import com.challenge.domain.Statistic;
import com.challenge.transfer.TransactionInput;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TransactionValidatorTest {
    private Validator transactionValidator;

    @Before
    public void setUp() {
        transactionValidator = new TransactionValidator();
    }

    @Test
    public void testSupports() {
        assertThat("Should support TransactionInput class", transactionValidator.supports(TransactionInput.class), is(true));
        assertThat("Should not support Class class", transactionValidator.supports(Class.class), is(false));
        assertThat("Should not support Object class", transactionValidator.supports(Object.class), is(false));
        assertThat("Should not support Statistic class", transactionValidator.supports(Statistic.class), is(false));
    }

    @Test
    public void testValidateWithValidInput() {
        TransactionInput transactionInput = new TransactionInput(10.56, Instant.now().toEpochMilli());
        BindException errors = new BindException(transactionInput, "transactionInput");
        ValidationUtils.invokeValidator(transactionValidator, transactionInput, errors);
        assertThat("Input should be valid", errors.hasErrors(), is(false));

        transactionInput.setAmount(34567);
        ValidationUtils.invokeValidator(transactionValidator, transactionInput, errors);
        assertThat("Input should be valid", errors.hasErrors(), is(false));
    }

    @Test
    public void testValidateWithInvalidAmount() {
        TransactionInput transactionInput = new TransactionInput(-10.56, Instant.now().toEpochMilli());
        BindException errors = new BindException(transactionInput, "transactionInput");
        ValidationUtils.invokeValidator(transactionValidator, transactionInput, errors);
        assertThat("Input should be invalid", errors.hasErrors(), is(true));
        assertThat(errors.getGlobalErrorCount(), is(1));
        assertThat(errors.getGlobalError().getCode(), is(TransactionValidator.AMOUNT));
        assertThat(errors.getGlobalError().getDefaultMessage(), is(TransactionValidator.INVALID_VALUE));
    }

    @Test
    public void testValidateWithInvalidTimestamp() {
        TransactionInput transactionInput = new TransactionInput(10.56, -100);
        BindException errors = new BindException(transactionInput, "transactionInput");
        ValidationUtils.invokeValidator(transactionValidator, transactionInput, errors);
        assertThat("Input should be invalid", errors.hasErrors(), is(true));
        assertThat(errors.getGlobalErrorCount(), is(1));
        assertThat(errors.getGlobalError().getCode(), is(TransactionValidator.TIMESTAMP));
        assertThat(errors.getGlobalError().getDefaultMessage(), is(TransactionValidator.INVALID_VALUE));
    }

    @Test
    public void testValidateWithInvalidAmountAndTimestamp() {
        TransactionInput transactionInput = new TransactionInput(0, 0);
        BindException errors = new BindException(transactionInput, "transactionInput");
        ValidationUtils.invokeValidator(transactionValidator, transactionInput, errors);
        assertThat("Input should be invalid", errors.hasErrors(), is(true));
        assertThat(errors.getGlobalErrorCount(), is(2));

    }
}
