package com.brickwork.finance.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfInvoiceService {

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Generates a PDF invoice.
     *
     * @param templateName The name of the Thymeleaf template (e.g., "invoice")
     * @param variables    A map containing the data to inject into the template
     * @return A byte array representing the generated PDF file
     */
    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> variables) {

        // 1. Prepare the Thymeleaf context with our data
        Context context = new Context();
        context.setVariables(variables);

        // 2. Process the HTML template into a String
        String htmlContent = templateEngine.process(templateName, context);

        // 3. Render the HTML string to a PDF byte array
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Openhtmltopdf needs a base URI to resolve relative links/images.
            // Using a dummy file URI since we don't have external assets right now.
            builder.withHtmlContent(htmlContent, "file:///");
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }
}