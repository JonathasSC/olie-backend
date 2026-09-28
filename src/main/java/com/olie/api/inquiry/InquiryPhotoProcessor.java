package com.olie.api.inquiry;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.olie.api.exception.ApiException;
import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;
import com.twelvemonkeys.imageio.metadata.tiff.TIFF;
import com.twelvemonkeys.imageio.metadata.tiff.TIFFReader;

import lombok.RequiredArgsConstructor;

/**
 * Valida o tipo real da imagem (pelo conteúdo, não pela extensão) e a deixa pronta para o WhatsApp: WEBP vira
 * JPEG (o WhatsApp trata WEBP como figurinha) e imagens grandes são reduzidas, respeitando a rotação do EXIF.
 */
@Component
@RequiredArgsConstructor
public class InquiryPhotoProcessor {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpeg", "png", "webp");
    private static final float[] JPEG_QUALITIES = {0.85f, 0.75f, 0.6f, 0.45f};

    private final InquiryProperties properties;

    public ProcessedPhoto process(byte[] original) {
        String format = detectFormat(original);
        BufferedImage image = decode(original);
        int orientation = "jpeg".equals(format) ? exifOrientation(original) : 1;

        long maxBytes = properties.photos().maxSize().toBytes();
        int maxDimension = properties.photos().maxDimension();
        boolean fits = original.length <= maxBytes && Math.max(image.getWidth(), image.getHeight()) <= maxDimension;

        if (fits && orientation == 1 && !"webp".equals(format)) {
            String contentType = "png".equals(format) ? "image/png" : "image/jpeg";
            String extension = "png".equals(format) ? "png" : "jpg";
            return new ProcessedPhoto(original, contentType, extension, image.getWidth(), image.getHeight());
        }

        BufferedImage prepared = resize(orient(image, orientation), maxDimension);
        byte[] jpeg = encodeJpeg(prepared, maxBytes);
        return new ProcessedPhoto(jpeg, "image/jpeg", "jpg", prepared.getWidth(), prepared.getHeight());
    }

    private String detectFormat(byte[] content) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            while (readers.hasNext()) {
                String format = readers.next().getFormatName().toLowerCase(Locale.ROOT);
                String normalized = "jpg".equals(format) ? "jpeg" : format;
                if (SUPPORTED_FORMATS.contains(normalized)) {
                    return normalized;
                }
            }
        } catch (IOException ignored) {
            // cai no erro abaixo
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PHOTO", "Envie uma imagem JPG, PNG ou WEBP.");
    }

    private BufferedImage decode(byte[] content) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image != null) {
                return image;
            }
        } catch (IOException | RuntimeException ignored) {
            // cai no erro abaixo
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHOTO", "Não foi possível ler a imagem enviada.");
    }

    /** Orientação EXIF (1 = normal). Qualquer problema na leitura é tratado como "sem rotação". */
    private int exifOrientation(byte[] content) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            List<JPEGSegment> segments = JPEGSegmentUtil.readSegments(input, JPEG.APP1, "Exif");
            if (segments.isEmpty()) {
                return 1;
            }
            byte[] exif = segments.getFirst().data().readAllBytes();
            int tiffStart = tiffHeaderOffset(exif);
            if (tiffStart < 0) {
                return 1;
            }
            try (ImageInputStream tiff = ImageIO.createImageInputStream(
                    new ByteArrayInputStream(exif, tiffStart, exif.length - tiffStart))) {
                Directory directory = new TIFFReader().read(tiff);
                Entry entry = directory.getEntryById(TIFF.TAG_ORIENTATION);
                return entry != null ? ((Number) entry.getValue()).intValue() : 1;
            }
        } catch (IOException | RuntimeException ignored) {
            return 1;
        }
    }

    private static int tiffHeaderOffset(byte[] exif) {
        for (int i = 0; i + 3 < exif.length; i++) {
            boolean intel = exif[i] == 'I' && exif[i + 1] == 'I' && exif[i + 2] == 42 && exif[i + 3] == 0;
            boolean motorola = exif[i] == 'M' && exif[i + 1] == 'M' && exif[i + 2] == 0 && exif[i + 3] == 42;
            if (intel || motorola) {
                return i;
            }
        }
        return -1;
    }

    private static BufferedImage orient(BufferedImage image, int orientation) {
        if (orientation < 2 || orientation > 8) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        boolean swap = orientation >= 5;

        // cada chamada é aplicada antes das anteriores (composição à direita)
        AffineTransform transform = new AffineTransform();
        switch (orientation) {
            case 2 -> { // espelhada na horizontal
                transform.translate(width, 0);
                transform.scale(-1, 1);
            }
            case 3 -> { // 180°
                transform.translate(width, height);
                transform.rotate(Math.PI);
            }
            case 4 -> { // espelhada na vertical
                transform.translate(0, height);
                transform.scale(1, -1);
            }
            case 5 -> { // transposta: (x, y) -> (y, x)
                transform.rotate(Math.PI / 2);
                transform.scale(1, -1);
            }
            case 6 -> { // 90° horário: (x, y) -> (h - y, x)
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
            }
            case 7 -> { // transversa: (x, y) -> (h - y, w - x)
                transform.translate(height, width);
                transform.scale(-1, -1);
                transform.rotate(Math.PI / 2);
                transform.scale(1, -1);
            }
            case 8 -> { // 90° anti-horário: (x, y) -> (y, w - x)
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
            }
            default -> {
            }
        }

        BufferedImage oriented = new BufferedImage(swap ? height : width, swap ? width : height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = oriented.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, oriented.getWidth(), oriented.getHeight());
        graphics.drawImage(image, transform, null);
        graphics.dispose();
        return oriented;
    }

    private static BufferedImage resize(BufferedImage image, int maxDimension) {
        double scale = Math.min(1.0, (double) maxDimension / Math.max(image.getWidth(), image.getHeight()));
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));

        // sempre redesenha em RGB sobre fundo branco: JPEG não tem transparência
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static byte[] encodeJpeg(BufferedImage image, long maxBytes) {
        byte[] encoded = null;
        for (float quality : JPEG_QUALITIES) {
            encoded = writeJpeg(image, quality);
            if (encoded.length <= maxBytes) {
                break;
            }
        }
        return encoded;
    }

    private static byte[] writeJpeg(BufferedImage image, float quality) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), params);
            stream.flush();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao gerar JPEG", exception);
        } finally {
            writer.dispose();
        }
    }
}
