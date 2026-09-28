package com.olie.api.dto.contact;

/** Problema encontrado ao importar o arquivo de contatos, com a linha para o usuário localizar. */
public record ContactFileError(int line, String field, String message) {
}
