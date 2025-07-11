package xyz.xingfeng;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WebpToPdfConverter {

    public WebpToPdfConverter(String jmMun) {
        String zipFilePath = jmMun + ".zip";
        String outputPdfPath = "output.pdf";

        try {
            List<File> webpFiles = extractWebpFilesFromZip(zipFilePath);
            convertWebpToPdf(webpFiles, outputPdfPath);
            System.out.println("PDF 生成成功: " + outputPdfPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<File> extractWebpFilesFromZip(String zipFilePath) throws IOException {
        List<File> webpFiles = new ArrayList<>();
        Path tempDir = Files.createTempDirectory("webp_extract");

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".webp")) {
                    Path outputPath = tempDir.resolve(entry.getName());
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                        webpFiles.add(outputPath.toFile());
                    }
                }
            }
        }
        return webpFiles;
    }

    private static void convertWebpToPdf(List<File> webpFiles, String outputPdfPath) throws IOException {
        try (PdfWriter writer = new PdfWriter(outputPdfPath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            for (File webpFile : webpFiles) {
                // 将 WebP 转为 PNG 字节流（因为 iText 7 不支持直接读取 WebP）
                BufferedImage bufferedImage = ImageIO.read(webpFile);
                if (bufferedImage != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", baos); // 转为 PNG 格式
                    byte[] pngBytes = baos.toByteArray();

                    // 使用 PNG 数据创建 PDF 图片
                    ImageData imageData = ImageDataFactory.create(pngBytes);
                    Image pdfImage = new Image(imageData);
                    document.add(pdfImage);
                }
            }
        }
    }
}

