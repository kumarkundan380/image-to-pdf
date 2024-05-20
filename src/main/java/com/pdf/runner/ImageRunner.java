package com.pdf.runner;

import com.pdf.service.ImageToPDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ImageRunner implements CommandLineRunner {
    @Autowired
    private ImageToPDFService imageToPDFService;

    @Override
    public void run(String... args) throws Exception {
        imageToPDFService.findMultipartFile();
    }
}
