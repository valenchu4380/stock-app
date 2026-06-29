package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenItem {
    private String name;
    private String subCategory;
    private int cantidad;
    private BigDecimal price;
}
