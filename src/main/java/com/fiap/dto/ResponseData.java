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
public class ResponseData {
    String key;
    String filename;
    EstadoProcessamento estado;
}