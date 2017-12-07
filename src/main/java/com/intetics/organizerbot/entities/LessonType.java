package com.intetics.organizerbot.entities;

import java.util.ResourceBundle;

public enum LessonType {
    LECTURE, PRACTICE, TEST, EXAM, COLLOQUIUM;

    private static ResourceBundle classTypes = ResourceBundle.getBundle("classtypes");

    public static LessonType fromString(String string){
        if (classTypes.getString("lecture").equals(string)){
            return LessonType.LECTURE;
        } else if (classTypes.getString("practice").equals(string)){
            return LessonType.PRACTICE;
        } else if (classTypes.getString("exam").equals(string)){
            return LessonType.EXAM;
        } else if (classTypes.getString("test").equals(string)){
            return LessonType.TEST;
        } else if (classTypes.getString("colloquium").equals(string)){
            return LessonType.COLLOQUIUM;
        } else {
            return null;
        }
    }
}
