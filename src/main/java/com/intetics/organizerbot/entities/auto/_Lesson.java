package com.intetics.organizerbot.entities.auto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

import com.intetics.organizerbot.entities.Professors;
import com.intetics.organizerbot.entities.Subject;

/**
 * Class _Lesson was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Lesson extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String LESSON_ID_PK_COLUMN = "LESSON_ID";

    public static final Property<LocalDate> DATE = Property.create("date", LocalDate.class);
    public static final Property<String> ROOM = Property.create("room", String.class);
    public static final Property<LocalTime> TIME = Property.create("time", LocalTime.class);
    public static final Property<Integer> TYPE = Property.create("type", Integer.class);
    public static final Property<Professors> PROFESSOR = Property.create("professor", Professors.class);
    public static final Property<Subject> SUBJECTS = Property.create("subjects", Subject.class);

    public void setDate(LocalDate date) {
        writeProperty("date", date);
    }
    public LocalDate getDate() {
        return (LocalDate)readProperty("date");
    }

    public void setRoom(String room) {
        writeProperty("room", room);
    }
    public String getRoom() {
        return (String)readProperty("room");
    }

    public void setTime(LocalTime time) {
        writeProperty("time", time);
    }
    public LocalTime getTime() {
        return (LocalTime)readProperty("time");
    }

    public void setType(int type) {
        writeProperty("type", type);
    }
    public int getType() {
        Object value = readProperty("type");
        return (value != null) ? (Integer) value : 0;
    }

    public void setProfessor(Professors professor) {
        setToOneTarget("professor", professor, true);
    }

    public Professors getProfessor() {
        return (Professors)readProperty("professor");
    }


    public void setSubjects(Subject subjects) {
        setToOneTarget("subjects", subjects, true);
    }

    public Subject getSubjects() {
        return (Subject)readProperty("subjects");
    }


}
