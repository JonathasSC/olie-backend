package com.olie.api.inquiry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.olie.api.exception.ApiException;

class InquiryPhotoProcessorTest {

    private final InquiryPhotoProcessor processor = new InquiryPhotoProcessor(InquiryTestProperties.create(Path.of("unused")));

    @Test
    void keepsSmallImagesUntouched() throws IOException {
        byte[] png = encode(image(100, 50, Color.RED), "png");

        ProcessedPhoto photo = processor.process(png);

        assertThat(photo.content()).isEqualTo(png);
        assertThat(photo.contentType()).isEqualTo("image/png");
        assertThat(photo.width()).isEqualTo(100);
    }

    @Test
    void shrinksLargeImagesToJpegWithinLimits() throws IOException {
        byte[] png = encode(noisyImage(2000, 1000), "png");

        ProcessedPhoto photo = processor.process(png);

        assertThat(photo.contentType()).isEqualTo("image/jpeg");
        assertThat(photo.width()).isEqualTo(800);
        assertThat(photo.height()).isEqualTo(400);
        assertThat(photo.content().length).isLessThanOrEqualTo(200 * 1024);
    }

    @Test
    void appliesExifRotationWhenReencoding() throws IOException {
        // 1200x600 com orientação 6 (girar 90° horário) deve sair em pé: 400x800
        byte[] jpeg = withExifOrientation(encode(image(1200, 600, Color.BLUE), "jpeg"), 6);

        ProcessedPhoto photo = processor.process(jpeg);

        assertThat(photo.width()).isEqualTo(400);
        assertThat(photo.height()).isEqualTo(800);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(photo.content()));
        assertThat(decoded.getWidth()).isEqualTo(400);
    }

    @Test
    void rejectsNonImages() {
        byte[] text = "não sou uma imagem".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> processor.process(text))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("UNSUPPORTED_PHOTO");
    }

    private static BufferedImage image(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }

    private static BufferedImage noisyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.util.Random random = new java.util.Random(42);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        return image;
    }

    private static byte[] encode(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    /** Insere um segmento APP1/EXIF mínimo (TIFF big-endian com a tag Orientation) logo após o SOI. */
    private static byte[] withExifOrientation(byte[] jpeg, int orientation) {
        byte[] tiff = {
                'M', 'M', 0, 42, 0, 0, 0, 8,        // cabeçalho, IFD0 no offset 8
                0, 1,                                // 1 entrada
                0x01, 0x12, 0, 3, 0, 0, 0, 1,        // tag 274 (Orientation), SHORT, 1 valor
                0, (byte) orientation, 0, 0,         // valor
                0, 0, 0, 0                           // sem próximo IFD
        };
        byte[] exifHeader = {'E', 'x', 'i', 'f', 0, 0};
        int length = 2 + exifHeader.length + tiff.length;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(jpeg, 0, 2);                    // SOI
        output.write(0xFF);
        output.write(0xE1);                          // APP1
        output.write(length >> 8);
        output.write(length & 0xFF);
        output.writeBytes(exifHeader);
        output.writeBytes(tiff);
        output.write(jpeg, 2, jpeg.length - 2);
        return output.toByteArray();
    }
}
