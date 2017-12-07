package com.intetics.organizerbot;

import com.intetics.organizerbot.entities.*;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectById;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DAO {
    protected ServerRuntime serverRuntime;
    protected ObjectContext context;
    public DAO(){
        serverRuntime = ServerRuntime.builder()
                .addConfig("cayenne-project.xml")
                .build();
        context = serverRuntime.newContext();
    }
    public void createUser(int id, String name, String info){
        Users user = SelectById.query(Users.class, id)
                .selectOne(context);
        if(user == null){
            user = context.newObject(Users.class);
            user.setId(id);
            user.setUserName(name);
            user.setInfo(info);
            context.commitChanges();
        }
    }
    public void createSubjects(int id, String title){
        Subject subject = ObjectSelect.query(Subject.class)
                .where(Subject.USER_ID.eq(id))
                .and(Subject.SUBJECT_TITLE.eq(title))
                .selectOne(context);
        if(subject == null){
            subject = context.newObject(Subject.class);
            subject.setSubjectTitle(title);
            subject.setUser(
                    SelectById.query(Users.class, id)
                            .selectOne(context)
            );
            context.commitChanges();
        }

    }

    public void createLesson(int id, String subject, LocalDate date, LocalTime time, String room, int type, String professorName){
        Lesson lesson = context.newObject(Lesson.class);
        lesson.setDate(date);
        lesson.setTime(time);
        lesson.setRoom(room);
        lesson.setType(type);

        Subject sub = ObjectSelect.query(Subject.class)
                .where(Subject.SUBJECT_TITLE.eq(subject))
                .and(Subject.USER_ID.eq(id))
                .selectOne(context);

        Professors professor = ObjectSelect.query(Professors.class)
                .where(Professors.PROFESSOR_NAME.eq(professorName))
                .selectOne(context);

        lesson.setSubjects(sub);
        lesson.setProfessor(professor);

        context.commitChanges();
    }

    public void createProfessor(String name){
        Professors professors = context.newObject(Professors.class);
        professors.setProfessorName(name);
        context.commitChanges();
    }

    public List<Lesson> getLessonByDate(LocalDate date, int id){
        List<Lesson> lessons = ObjectSelect.query(Lesson.class)
                .where(Lesson.DATE.eq(date))
                .and(Lesson.SUBJECTS.dot(Subject.USER_ID).eq(id))
                .select(context);

        return lessons;
    }

    public List<Lesson> getLessonByDateandTime(LocalDate date, LocalTime time, int id){
        List<Lesson> lessons = ObjectSelect.query(Lesson.class)
                .where(Lesson.DATE.eq(date))
                .and(Lesson.TIME.eq(time))
                .and(Lesson.SUBJECTS.dot(Subject.USER_ID).eq(id))
                .select(context);

        return lessons;
    }
    public List<Subject> getSubjectsByTitle(String title, int id){
        List<Subject> subjects = ObjectSelect.query(Subject.class)
                .where(Subject.SUBJECT_TITLE.eq(title))
                .and(Subject.USER.dot(Users.USER_ID_PK_COLUMN).eq(id))
                .select(context);

        return subjects;
    }
    public Users getUser(int id){
        return SelectById.query(Users.class, id)
                .selectOne(context);
    }

    public List<Subject> getSubjectsByUserId(int id){
        List<Subject> subjects = ObjectSelect.query(Subject.class)
                .where(Subject.USER_ID.eq(id))
                .select(context);
        return subjects;
    }

    public List<Professors> getProfessors(){
        List<Professors> professorss = ObjectSelect.query(Professors.class)
                .select(context);
        return professorss;
    }
}