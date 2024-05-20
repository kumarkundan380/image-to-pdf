package com.pdf.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.pdf.constant.ImageTOPDFConstant.DIRECTORY_PATH;
import static com.pdf.constant.ImageTOPDFConstant.FILE_FORMAT;
import static com.pdf.constant.ImageTOPDFConstant.FILE_NAME;

@Service
@Slf4j
public class ImageToPDFService {

    @Value("${image.folder.path}")
    private String imageFolder;
    static final File dir = new File(DIRECTORY_PATH);
    static final String[] EXTENSIONS = new String[]{ "gif", "png", "bmp"};
    private final List<File> files = new ArrayList<>();

    public void findMultipartFile() throws FileNotFoundException {
        final FilenameFilter IMAGE_FILTER = (dir, name) -> {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        };
        if (dir.isDirectory()) { // make sure it's a directory
            for (final File f : Objects.requireNonNull(dir.listFiles(IMAGE_FILTER))) {
                files.add(f);
                Collections.sort(files, (o1, o2) -> {
                    int n1 = extractNumber(o1.getName());
                    int n2 = extractNumber(o2.getName());
                    return n1 - n2;
                });
            }
        }
        convertImageIntoPDF(files);
    }

    public void convertImageIntoPDF(List<File> fileList) {
        List<String> imagePaths = new ArrayList<>();
        for (File file : fileList) {
            Objects.requireNonNull(FilenameUtils.getExtension(file.getName())).toLowerCase();
            imagePaths.add(saveImage(file));
        }
        combineImagesIntoPDF(imagePaths);
    }

    @SneakyThrows
    private void combineImagesIntoPDF(List<String> invoicePath){
        String pdfPath = imageFolder + File.separator + FILE_NAME + "_" + System.currentTimeMillis() + FILE_FORMAT;
        try {
            PDDocument doc = new PDDocument();
            for (String invoice : invoicePath) {
                Files.find(Paths.get(invoice),
                                Integer.MAX_VALUE,
                                (path, basicFileAttributes) -> Files.isRegularFile(path))
                        .forEachOrdered(path -> addImageAsNewPage(doc, path.toString()));
            }
            doc.save(pdfPath);
            System.out.println("Image Added:");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addImageAsNewPage(PDDocument doc, String imagePath) {
        try {
            PDImageXObject image = PDImageXObject.createFromFile(imagePath, doc);
            PDRectangle pageSize = PDRectangle.A4;

            int originalWidth  = image.getWidth();
            int originalHeight = image.getHeight();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            float scaledWidth = originalWidth  * ratio;
            float scaledHeight = originalHeight * ratio;
            float x = (pageWidth  - scaledWidth ) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveImage(File invoice) {
        String fileName = invoice.getName();
        String filePath = imageFolder + File.separator + fileName;
        File file = new File(imageFolder);
        if(!file.exists()){
            file.mkdir();
        }
        try {
            InputStream inputStream = new FileInputStream(invoice);
            Files.copy(inputStream,Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private Integer extractNumber(String fileName) {
        String res = "";
        for(int i=0;i<fileName.length();i++){
            if(Character.isDigit(fileName.charAt(i))){
                res+=fileName.charAt(i);
            } else {
                break;
            }
        }
        return Integer.parseInt(res);
    }
}
