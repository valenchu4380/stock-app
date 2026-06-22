/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author User
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product  implements Serializable{
    @Id
    private String name;
    private double price;
    private int stock;
 @Enumerated(EnumType.STRING)
    @Column(name = "category") // Asegúrate que el nombre en BD sea 'category'
    private ProductCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subcategory") // FUERZA el nombre exacto de la columna de tu tabla
    private SubCategory subCategory;
}

