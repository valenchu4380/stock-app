package com.valentin.tu_cv_spring_bot.TuCv.Utils;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;

public class Validates {
 public static <T extends Number> void validate(T value, String massage) throws InvalidProductException{
        if(value==null){
            throw new InvalidProductException(massage);
        }
    }
    
    public static <T> void  ValidateObject (T obj,String massage) throws InvalidProductException{
         if(obj==null){
            throw new InvalidProductException(massage);
        }
    }
    
    public static void validateText(String txt, String massage) throws InvalidProductException{
        if(txt==null || txt.isEmpty()){
            throw new InvalidProductException(massage);
        }
    }
}
