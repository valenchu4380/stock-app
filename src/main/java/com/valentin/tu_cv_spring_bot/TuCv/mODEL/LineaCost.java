package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaCost {
    private Linea linea;
    private double costPrice;
    private Integer productCount;
}
