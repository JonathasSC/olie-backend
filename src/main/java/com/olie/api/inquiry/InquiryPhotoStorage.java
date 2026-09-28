package com.olie.api.inquiry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;

/** Guarda as fotos em disco local (fora do banco e do controle de versão), uma pasta por usuário. */
@Component
public class InquiryPhotoStorage {

    private final Path root;

    public InquiryPhotoStorage(InquiryProperties properties) {
        this.root = properties.photos().storageDir().toAbsolutePath().normalize();
    }

    /** Grava de forma atômica (arquivo temporário + move) e devolve o caminho relativo à raiz. */
    public String save(UUID userId, UUID photoId, String extension, byte[] content) {
        Path relative = Path.of(userId.toString(), photoId + "." + extension);
        Path target = resolve(relative.toString());
        try {
            Files.createDirectories(target.getParent());
            Path temp = Files.createTempFile(target.getParent(), "upload-", ".tmp");
            Files.write(temp, content);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao gravar a foto", exception);
        }
        return relative.toString().replace('\\', '/');
    }

    public byte[] read(String relativePath) {
        try {
            return Files.readAllBytes(resolve(relativePath));
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao ler a foto " + relativePath, exception);
        }
    }

    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao remover a foto " + relativePath, exception);
        }
    }

    private Path resolve(String relativePath) {
        Path path = root.resolve(relativePath).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Caminho de foto fora do diretório de armazenamento");
        }
        return path;
    }
}
