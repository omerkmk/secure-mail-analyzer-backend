package com.securemail.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotBlank(message = "Analiz edilecek içerik boş olamaz.")
    @Size(max = 10000, message = "Analiz edilecek içerik en fazla 10000 karakter olabilir.")
    private String input;
}