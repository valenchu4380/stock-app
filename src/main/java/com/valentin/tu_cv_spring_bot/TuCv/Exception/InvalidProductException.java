/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.Exception;

/**
 *
 * @author User
 */
public class InvalidProductException extends Exception {

    /**
     * Creates a new instance of <code>InvalidProductException</code> without
     * detail message.
     */
    public InvalidProductException() {
    }

    /**
     * Constructs an instance of <code>InvalidProductException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidProductException(String msg) {
        super(msg);
    }
}
