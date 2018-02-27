package com.challenge.web;

import com.challenge.service.StatisticsService;
import com.challenge.transfer.TransactionInput;
import com.challenge.web.validation.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class TransactionController {
    @Autowired
    private StatisticsService statisticsService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new TransactionValidator());
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity addTransaction(@RequestBody @Valid TransactionInput request) {
        return statisticsService.addTransaction(request);
    }
}
