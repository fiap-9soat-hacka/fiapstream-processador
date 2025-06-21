package com.fiap.dto;

import com.fiap.enums.EstadoProcessamento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseData extends ResponseData {
    String key;

    public MessageResponseData(String filename, String uuid, EstadoProcessamento estado) {
        this.filename = filename;
        this.key = uuid;
        this.estado = estado;
    }
}