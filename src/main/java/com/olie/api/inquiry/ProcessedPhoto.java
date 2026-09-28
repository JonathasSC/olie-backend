package com.olie.api.inquiry;

public record ProcessedPhoto(byte[] content, String contentType, String extension, int width, int height) {
}
