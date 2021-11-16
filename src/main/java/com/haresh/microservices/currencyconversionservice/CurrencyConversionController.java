package com.haresh.microservices.currencyconversionservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class CurrencyConversionController {

    @Autowired
    CurrencyExchangeProxy proxy;

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
//        return new CurrencyConversion(10001L, from, to, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, "");
        HashMap<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        ResponseEntity<CurrencyConversion> entity = new RestTemplate().getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class,
                uriVariables);

        CurrencyConversion currencyConversion = entity.getBody();
        if (currencyConversion == null) {
            throw new RuntimeException("Unable to convert the currency value from " + from + " to " + to);
        }
        currencyConversion.setEnvironment(currencyConversion.getEnvironment() + " from Rest Service");
        currencyConversion.setTotalCalculatedAmount(quantity.multiply(currencyConversion.getConversionMultiple()));

        return currencyConversion;
    }

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {

        CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);
        if (currencyConversion == null) {
            throw new RuntimeException("Unable to convert the currency value from " + from + " to " + to);
        }
        currencyConversion.setEnvironment(currencyConversion.getEnvironment() + " from Feign");
        currencyConversion.setTotalCalculatedAmount(quantity.multiply(currencyConversion.getConversionMultiple()));

        return currencyConversion;
    }
}
