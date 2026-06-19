package com.valentin.tu_cv_spring_bot.TuCv.service;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;

public class ProductValidator {
    public static void validate(Product product) throws InvalidProductException{
        if(product.getPrice()<0){
            throw new InvalidProductException("El price debe ser mayor a 0");
        }
        if(product.getStock()<0){
            throw new InvalidProductException("El stock del producto no puede ser negativo");
        }
    }

}
