/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.Exception;

/**
 *
 * @author User
 */
public class ProductNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>ProductNotFoundException</code> without
     * detail message.
     */
    public ProductNotFoundException() {
    }

    /**
     * Constructs an instance of <code>ProductNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ProductNotFoundException(String msg) {
        super(msg);
    }
}
