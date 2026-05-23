package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Autowired
    private InvoiceService invoiceService;

    @RequestMapping(value = "/print", method = RequestMethod.POST )
    public void print(@RequestBody final Request request) {
        System.out.println("Request received - " + request.toString());

        double total = request.items().stream().mapToDouble(InvoiceItem::getTotal).sum();
        invoiceService.printInvoice(request.items(), total);
    }
}


