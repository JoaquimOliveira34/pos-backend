package com.example.demo;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {
    private static final String printerName = "Toshiba recibos";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int WIDTH = 48;


    public void printInvoice(List<InvoiceItem> items, double totalPrice) {
        try {
            // 1. Get the printer from Windows
            PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
            if (printService == null) {
                throw new RuntimeException("Printer not found: " + printerName);
            }

            // 2. Initialize escpos-coffee connection
            PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
            EscPos escpos = new EscPos(printerOutputStream);

            // 3. Create Styles
            Style titleStyle = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true);

            Style subtitleStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            Style boldStyle = new Style().setBold(true);

            // 4. Header
            escpos.writeLF(titleStyle, "Festa do trigal");
            escpos.writeLF(subtitleStyle, "Ass pais EB de Trigal de Santa Maria");
            escpos.writeLF(subtitleStyle, LocalDateTime.now().format(formatter));
            escpos.writeLF("-".repeat(WIDTH));

            // 5. Column Headers
            // %-20s = Left-aligned string with 20 spaces
            // %4s = Right-aligned string with 4 spaces
            // %7s = Right-aligned string with 7 spaces
            final String headerFormat = "%-27s %4s %6s %6s";
            final String lineFormat   = "%-27s %4d %6.2f %6.2f";

            // 5. Column Headers
            escpos.writeLF(boldStyle, String.format(headerFormat, "Item", "Qtd", "P.Unit", "Total"));
            escpos.writeLF("-".repeat(WIDTH));

            // 6. Print Items
            for (InvoiceItem item : items) {
                // Increase truncation limit to match new column size
                String shortName = item.name().length() > 24
                        ? item.name().substring(0, 24)
                        : item.name();

                escpos.writeLF(String.format(lineFormat,
                        shortName,
                        item.quantity(),
                        item.unitPrice(),
                        item.getTotal()));
            }

            escpos.writeLF("-".repeat(WIDTH));


            // 7. Final Total
            // Using the full width: Total is 39 characters wide (24+4+9+2),
            // then the price takes the last 9.
            Style totalStyle = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setBold(true);
            String totalLine = String.format("%-15s %6.2f", "TOTAL:", totalPrice);
            escpos.writeLF(totalStyle, totalLine);

            // 8. Footer and Paper Cut
            escpos.feed(2); // Feed paper a bit
            //escpos.writeLF(subtitleStyle, "Obrigado\n");
            escpos.feed(4); // Feed enough paper to pass the cutter

            // Automatically cut the paper
            escpos.cut(EscPos.CutMode.FULL);

            // 9. Close and send to printer
            escpos.close();
            System.out.println("Invoice sent successfully!");

        } catch (IOException e) {
            System.err.println("Printer I/O Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}