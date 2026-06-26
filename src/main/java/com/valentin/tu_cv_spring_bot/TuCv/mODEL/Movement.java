package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    private Long id;
    private String productName;
    private String productSubCategory;
    private String action;
    private Double oldPrice;
    private Double newPrice;
    private Integer oldStock;
    private Integer newStock;
    private LocalDateTime timestamp;
}
