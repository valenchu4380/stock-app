package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orden {
    private Long id;
    private String itemsJson;
    private BigDecimal total;
    private String estado;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
