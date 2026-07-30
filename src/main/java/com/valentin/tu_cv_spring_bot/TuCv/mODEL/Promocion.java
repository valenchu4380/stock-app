package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {
    private Long id;
    private String nombre;
    private String descripcion;
    private double descuento;
    private String tipoDescuento;
    private String tipoTarget;
    private String targetValor;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa;
}
