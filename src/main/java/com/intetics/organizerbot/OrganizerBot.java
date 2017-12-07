package com.intetics.organizerbot;

import com.intetics.organizerbot.context.ContextHolder;
import com.intetics.organizerbot.context.Context;
import com.intetics.organizerbot.context.TypeOfClass;
import com.intetics.organizerbot.entities.Lesson;
import com.intetics.organizerbot.entities.LessonType;
import com.intetics.organizerbot.entities.Professors;
import com.intetics.organizerbot.entities.Subject;
import com.intetics.organizerbot.keyboards.Keyboards;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OrganizerBot extends TelegramLongPollingBot {

    private static final String LOGTAG = "BOT";

    private ResourceBundle botInfo = ResourceBundle.getBundle("botinfo");
    private ResourceBundle buttons = ResourceBundle.getBundle("buttons");
    private ResourceBundle messages = ResourceBundle.getBundle("messages");
    private static ResourceBundle days = ResourceBundle.getBundle("days");

    public String getBotUsername() {
        return botInfo.getString("username");
    }

    DAO dao = new DAO();

    @Override
    public String getBotToken() {
        return botInfo.getString("token");
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery query) {
        Context context = ContextHolder.getInstance().getContext(query.getMessage().getChatId());
        switch (context) {
            case ADD_CLASS_CHOOSE_DATE:
                handleCallbackQueryFromAddClassChooseDate(query);
                break;
            case SHOW_TIMETABLE:
                handleCallbackQueryFormShowTimetable(query);
                break;
        }
    }

    private void handleCallbackQueryFormShowTimetable(CallbackQuery query) {
        String data = query.getData();
        if (data.startsWith("goto:")) {
            resetCalendar(query);
        } else if (data.startsWith("choose:")) {
            LocalDate date = LocalDate.parse(data.split(":")[1]);
            showTimetable(query.getMessage(), date);
            setContext(query.getMessage().getChatId(), Context.MAIN_MENU);
            sendMainMenu(query.getMessage());
        }
    }

    private void handleCallbackQueryFromAddClassChooseDate(CallbackQuery query) {
        String data = query.getData();
        if (data.startsWith("goto:")) {
            resetCalendar(query);
        } else if (data.startsWith("choose:")) {
            LocalDate date = LocalDate.parse(data.split(":")[1]);
            setDate(query, date);
            reply(query.getMessage(), date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
            setContext(query.getMessage().getChatId(), Context.ADD_CLASS_CHOOSE_TIME);
            reply(query.getMessage(), messages.getString("chooseTime"), Keyboards.getReturnToMenuKeyboard());
        }
    }

    private void setDate(CallbackQuery query, LocalDate date) {
        Lesson lesson = (Lesson) getEditingValue(query.getMessage().getChatId());
        lesson.setDate(date);
    }

    private void resetCalendar(CallbackQuery query) {
        EditMessageText editMarkup = new EditMessageText();
        editMarkup.setChatId(query.getMessage().getChatId().toString());
        editMarkup.setInlineMessageId(query.getInlineMessageId());
        editMarkup.enableMarkdown(true);
        editMarkup.setText(messages.getString("chooseDate2"));
        editMarkup.setMessageId(query.getMessage().getMessageId());
        editMarkup.setReplyMarkup(Keyboards.getCalendarKeyboard(YearMonth.parse(query.getData().split(":")[1])));
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            BotLogger.severe(LOGTAG, e);
        }
    }

    private void handleMessage(Message message) {
        Long userId = message.getChatId();
        if (!ContextHolder.getInstance().contains(message.getChatId())) {
            setContext(userId, Context.MAIN_MENU);
        }
        Context chatContext = ContextHolder.getInstance().getContext(userId);
        handleMessageInContext(message, chatContext);
    }

    private void handleMessageInContext(Message message, Context context) {
        switch (context) {
            case MAIN_MENU:
                handleMessageFromMainMenu(message);
                break;
            case ADD_CLASS_CHOOSE_SUBJECT:
                handleMessageFromAddClassChooseSubject(message);
                break;
            case ADD_CLASS_CHOOSE_DATE_OR_DAY:
                handleMessageFromAddClassChooseDateOrDay(message);
                break;
            case ADD_CLASS_CHOOSE_DAY:
                handleMessageFromAddClassChooseDay(message);
                break;
            case ADD_CLASS_CHOOSE_DATE:
                handleMessageFromAddClassChooseDate(message);
                break;
            case ADD_CLASS_CHOOSE_TIME:
                handleMessageFromAddClassChooseTime(message);
                break;
            case ADD_CLASS_CHOOSE_TYPE:
                handleMessageFromAddClassChooseType(message);
                break;
            case ADD_CLASS_CHOOSE_PROFESSOR:
                handleMessageFromAddClassChooseProfessor(message);
                break;
            case ADD_CLASS_CHOOSE_ROOM:
                handleMessageFromAddClassChooseRoom(message);
                break;
            case SHOW_TIMETABLE:
                handleMessageFromShowTimetable(message);
//            case ADD_EVENT_CHOOSE_DATE:
//                handleMessageFromAddEventChooseDate(message);
//                break;
//            case ADD_EVENT_CHOOSE_TIME:
//                handleMessageFromAddEventChooseTime(message);
//                break;
//            case ADD_EVENT_CHOOSE_DESCRIPTION:
//                handleMessageFromAddEventChooseDescription(message);
//                break;
//            case ADD_SUBJECT:
//                handleMessageFromAddSubject(message);
//                break;
//            case REMOVE_SUBJECT:
//                handleMessageFromRemoveSubject(message);
//                break;
        }
    }

    private void handleMessageFromShowTimetable(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else if (buttons.getString("forToday").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            showTimetable(message, LocalDate.now());
        } else if (buttons.getString("forTomorrow").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            showTimetable(message, LocalDate.now().plusDays(1));
        } else if (buttons.getString("forOtherDate").equals(text)) {
            reply(message, messages.getString("chooseDate1"), Keyboards.getReturnToMenuKeyboard());
            reply(message, messages.getString("chooseDate2"), Keyboards.getCalendarKeyboard());
        } else if (buttons.getString("forWeek").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                showTimetable(message, today.plusDays(i));
            }
        } else {
            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseFromMenu");
            reply(message, replyText, Keyboards.getListKeyboard(getProfessors()));
        }
    }

    private void showTimetable(Message message, LocalDate date) {
        List<Lesson> lessons = dao.getLessonByDate(date, Math.toIntExact(message.getChatId()));
        String text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) + "\r\n";
        for (Lesson lesson : lessons) {
            try {
                text += "------------------------\r\n" +
                        lesson.getTime().toString() + "\r\n" +
                        lesson.getSubjects().getSubjectTitle() + "(" +
                        LessonType.values()[lesson.getType()].toString().toLowerCase() + ")\r\n" +
                        lesson.getRoom() + "\r\n" +
                        lesson.getProfessor().getProfessorName() + "\r\n";
            } catch (NullPointerException e) {

            }
        }
        reply(message, text, Keyboards.getMainMenuKeyboard());
    }

    private void handleMessageFromAddClassChooseProfessor(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else {
            setProfessor(message, text);
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_ROOM);
            reply(message, messages.getString("choosePlace"), Keyboards.getReturnToMenuKeyboard());
        }
    }

    private void setProfessor(Message message, String text) {
        Lesson lesson = (Lesson) getEditingValue(message.getChatId());
        Professors professor = new Professors();
        professor.setProfessorName(text);
        lesson.setProfessor(professor);
    }

    private void handleMessageFromAddClassChooseDate(Message message) {
        String text = message.getText();
        System.out.println("message:" + text + "end");
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else {
            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseDate1");
            reply(message, replyText);
        }
    }

    private void handleMessageFromAddClassChooseRoom(Message message) {
        String text = message.getText();
        if (!buttons.getString("mainMenu").equals(text)) {
            setLesson(message, text);
            reply(message, messages.getString("classAdded"));
        }
        setContext(message.getChatId(), Context.MAIN_MENU);
        sendMainMenu(message);
    }

    private void setLesson(Message message, String text) {
        Lesson lesson = (Lesson) getEditingValue(message.getChatId());
        lesson.setRoom(text);
        dao.createLesson(
                Math.toIntExact(message.getChatId()),
                lesson.getSubjects().getSubjectTitle(),
                lesson.getDate(),
                lesson.getTime(),
                lesson.getRoom(),
                lesson.getType(),
                lesson.getProfessor().getProfessorName());
        if (ContextHolder.getInstance().getTypeOfClass(message.getChatId()).equals(TypeOfClass.WEEKLY)) {
            while (lesson.getDate().getYear() == 2017) {
                lesson.setDate(lesson.getDate().plusDays(7));
                dao.createLesson(
                        Math.toIntExact(message.getChatId()),
                        lesson.getSubjects().getSubjectTitle(),
                        lesson.getDate(),
                        lesson.getTime(),
                        lesson.getRoom(),
                        lesson.getType(),
                        lesson.getProfessor().getProfessorName());
            }
        }
    }

    private void handleMessageFromAddClassChooseType(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else {
            LessonType lessonType = LessonType.fromString(text);
            if (lessonType == null) {
                String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseFromMenu");
                reply(message, replyText);
            }
            setClassType(message, lessonType);
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_PROFESSOR);
            reply(message, messages.getString("chooseProfessor"), Keyboards.getListKeyboard(getProfessors()));
        }
    }

    private void setClassType(Message message, LessonType lessonType) {
        Lesson lesson = (Lesson) getEditingValue(message.getChatId());
        lesson.setType(lessonType.ordinal());
    }

    private void handleMessageFromAddClassChooseDateOrDay(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else if (buttons.getString("oneTime").equals(text)) {
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_DATE);
            ContextHolder.getInstance().setTypeOfClass(message.getChatId(), TypeOfClass.ONE_TIME);
            reply(message, messages.getString("chooseDate1"), Keyboards.getReturnToMenuKeyboard());
            reply(message, messages.getString("chooseDate2"), Keyboards.getCalendarKeyboard());
        } else if (buttons.getString("weekly").equals(text)) {
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_DAY);
            ContextHolder.getInstance().setTypeOfClass(message.getChatId(), TypeOfClass.WEEKLY);
            reply(message, messages.getString("chooseDay"), Keyboards.getDaysListKeyboard());
        } else {
            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseFromMenu");
            reply(message, replyText);
        }
    }

//    private void handleMessageFromAddEventChooseDate(Message message) {
//        String text = message.getText();
//        if (buttons.getString("mainMenu").equals(text)) {
//            setContext(message.getChatId(), Context.MAIN_MENU);
//            sendMainMenu(message);
//        } else {
//            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseDate1");
//            reply(message, replyText);
//        }
//    }
//
//    private void handleMessageFromAddEventChooseDescription(Message message) {
//        String text = message.getText();
//        if (buttons.getString("mainMenu").equals(text)) {
//            setContext(message.getChatId(), Context.MAIN_MENU);
//            sendMainMenu(message);
//        } else {
//            Object o = ContextHolder.getInstance().getEditingValue(message.getChatId());
//            ContextHolder.getInstance().setEditingValue(message.getChatId(), o);
//            reply(message, messages.getString("eventAdded"));
//            sendMainMenu(message);
//            setContext(message.getChatId(), Context.MAIN_MENU);
//        }
//    }
//
//    private void handleMessageFromAddEventChooseTime(Message message) {
//        String text = message.getText();
//        if (text.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
//            Object o = ContextHolder.getInstance().getEditingValue(message.getChatId());
//            ContextHolder.getInstance().setEditingValue(message.getChatId(), o);
//            sendResponseOnChooseTimeFromAddEvent(message);
//            setContext(message.getChatId(), Context.ADD_EVENT_CHOOSE_DESCRIPTION);
//        } else if (buttons.getString("mainMenu").equals(text)) {
//            setContext(message.getChatId(), Context.MAIN_MENU);
//            sendMainMenu(message);
//        } else {
//            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseTime");
//            reply(message, replyText);
//        }
//    }
//
//    private void sendResponseOnChooseTimeFromAddEvent(Message inMessage) {
//        SendMessage outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("chooseDescription"));
//        outMessage.setReplyMarkup(Keyboards.getReturnToMenuKeyboard());
//        send(outMessage);
//    }

    private void handleMessageFromAddClassChooseSubject(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else {
            setSubject(message, text);
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_DATE_OR_DAY);
            reply(message, messages.getString("chooseDateOrDay"), Keyboards.getOneTimeOrWeeklyKeyboard());
        }
    }

    private void setSubject(Message message, String text) {
        Lesson lesson = new Lesson();
        Subject subject = new Subject();
        subject.setSubjectTitle(text);
        subject.setUser(dao.getUser(Math.toIntExact(message.getChatId())));
        List<String> subjectNames = getSubjects(message.getChatId());
//        if (!subjectNames.contains(text)) {
//            dao.createSubjects(Math.toIntExact(message.getChatId()), subject.getSubjectTitle());
//        }
        lesson.setSubjects(subject);
        setEditingValue(message.getChatId(), lesson);
    }

    private void handleMessageFromAddClassChooseTime(Message message) {
        String text = message.getText();
        if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else if (validTime(text)) {
            if (text.length() == 4) {
                text = "0" + text;
            }
            setTime(message, text);
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_TYPE);
            reply(message, messages.getString("chooseType"), Keyboards.getClassTypesListKeyboard());
        } else {
            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseTime");
            reply(message, replyText);
        }
    }

    private void setTime(Message message, String text) {
        Lesson lesson = (Lesson) getEditingValue(message.getChatId());
        lesson.setTime(LocalTime.parse(text));
    }

    private boolean validTime(String string) {
        return string.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private List<String> getSubjects(Long id) {//TODO: use dao
        List<String> subjectNames = new ArrayList<>();
        dao.getSubjectsByUserId(Math.toIntExact(id)).forEach(subject -> subjectNames.add(subject.getSubjectTitle()));
        return subjectNames;
    }

    private void handleMessageFromAddClassChooseDay(Message message) {
        String text = message.getText();
        if (validDayOfWeek(text)) {
            setDay(message, text);
            reply(message, messages.getString("chooseTime"), Keyboards.getReturnToMenuKeyboard());
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_TIME);
        } else if (buttons.getString("mainMenu").equals(text)) {
            setContext(message.getChatId(), Context.MAIN_MENU);
            sendMainMenu(message);
        } else {
            String replyText = messages.getString("cannotUnderstand") + ' ' + messages.getString("chooseDay");
            reply(message, replyText);
        }
    }

    private void setDay(Message message, String text) {
        Lesson lesson = (Lesson) getEditingValue(message.getChatId());
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(text.toUpperCase());//TODO: rewrite line
        LocalDate date = LocalDate.now();
        if (date.getDayOfWeek().getValue() > dayOfWeek.getValue()) {
            date = date.plusDays(7 - (date.getDayOfWeek().getValue() - dayOfWeek.getValue()));
        } else {
            date = date.plusDays(dayOfWeek.getValue() - date.getDayOfWeek().getValue());
        }
        lesson.setDate(date);
    }

//    private void sendResponseOnChooseDay(Message inMessage) {
//        SendMessage outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("chooseTime"));
//        outMessage.setReplyMarkup(Keyboards.getReturnToMenuKeyboard());
//        send(outMessage);
//    }

//    private void handleMessageFromRemoveSubject(Message message) {
//        if (buttons.getString("back").equals(message.getText())) {
//            sendMainMenu(message);
//            setContext(message.getChatId(), Context.MAIN_MENU);
//        } else {
//            removeSubject(message);
//        }
//    }

//    private void removeSubject(Message inMessage) {
//
//    }

//    private void handleMessageFromAddSubject(Message message) {
//        if (buttons.getString("back").equals(message.getText())) {
//            sendMainMenu(message);
//            setContext(message.getChatId(), Context.MAIN_MENU);
//        } else {
//            addSubject(message);
//            setContext(message.getChatId(), Context.MAIN_MENU);
//        }
//    }

//    private void addSubject(Message inMessage) {
//
//        sendMainMenu(inMessage, messages.getString("subjectAdded"));
//    }

    private void handleMessageFromMainMenu(Message message) {
//        if (buttons.getString("addEvent").equals(inMessage.getText())) {
//            sendResponseOnAddEvent(inMessage);
//            setContext(inMessage.getChatId(), Context.ADD_EVENT_CHOOSE_DATE);
//        } else
        if (buttons.getString("addClass").equals(message.getText())) {
            List<String> subjects = getSubjects(message.getChatId());
            reply(message, messages.getString("chooseSubject"), Keyboards.getListKeyboard(subjects));
            setContext(message.getChatId(), Context.ADD_CLASS_CHOOSE_SUBJECT);
        } else if (buttons.getString("showTimetable").equals(message.getText())) {
            reply(message, messages.getString("chooseView"), Keyboards.getViewsKeyboard());
            setContext(message.getChatId(), Context.SHOW_TIMETABLE);
//        } else if (buttons.getString("addSubject").equals(inMessage.getText())) {
//            sendResponseOnAddSubject(inMessage);
//            setContext(inMessage.getChatId(), Context.ADD_SUBJECT);
//        } else if (buttons.getString("removeSubject").equals(inMessage.getText())) {
//            sendResponseOnRemoveSubject(inMessage);
//            setContext(inMessage.getChatId(), Context.REMOVE_SUBJECT);
        } else if ("/start".equals(message.getText())) {
            dao.createUser(Math.toIntExact(message.getChatId()), message.getChat().getFirstName(), "no");
            sendMainMenu(message);
        } else {
            sendMainMenu(message, messages.getString("cannotUnderstand") + messages.getString("chooseFromMenu"));
        }
    }

//    private void sendResponseOnAddEvent(Message inMessage) {
//        SendMessage outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("chooseDate1"));
//        outMessage.setReplyMarkup(Keyboards.getReturnToMenuKeyboard());
//        send(outMessage);
//        outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("chooseDate2"));
//        outMessage.setReplyMarkup(Keyboards.getCalendarKeyboard());
//        send(outMessage);
//    }

//    private void sendResponseOnRemoveSubject(Message inMessage) {
//        SendMessage outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("noSubjects"));
//        outMessage.setReplyMarkup(Keyboards.getAddSubjectKeyboard());
//        //outMessage.setReplyMarkup(Keyboards.getSubjectListKeyboard(subjects));
//        send(outMessage);
//    }

//    private void sendResponseOnAddSubject(Message inMessage) {
//        SendMessage outMessage = new SendMessage();
//        outMessage.setChatId(inMessage.getChatId());
//        outMessage.setText(messages.getString("typeSubject"));
//        outMessage.setReplyMarkup(Keyboards.getAddSubjectKeyboard());
//        send(outMessage);
//    }


    private boolean validDayOfWeek(String string) {
        for (String dayKey : days.keySet()) {
            if (days.getString(dayKey).equals(string)) {
                return true;
            }
        }
        return false;
    }

    private void setContext(Long id, Context addSubject) {
        ContextHolder.getInstance().setContext(id, addSubject);
    }

    private void setEditingValue(Long id, Object value) {
        ContextHolder.getInstance().setEditingValue(id, value);
    }

    private Object getEditingValue(Long id) {
        return ContextHolder.getInstance().getEditingValue(id);
    }

    private void reply(Message inMessage, String text) {
        SendMessage outMessage = new SendMessage();
        outMessage.setChatId(inMessage.getChatId());
        outMessage.setText(text);
        send(outMessage);
    }

    private void reply(Message inMessage, String text, ReplyKeyboard keyboard) {
        SendMessage outMessage = new SendMessage();
        outMessage.setChatId(inMessage.getChatId());
        outMessage.setText(text);
        outMessage.setReplyMarkup(keyboard);
        send(outMessage);
    }

    private void sendMainMenu(Message inMessage) {
        sendMainMenu(inMessage, messages.getString("chooseOption"));
    }

    private void sendMainMenu(Message inMessage, String outMessageText) {
        reply(inMessage, outMessageText, Keyboards.getMainMenuKeyboard());
    }

    private void send(SendMessage outMessage) {
        try {
            execute(outMessage);
        } catch (TelegramApiException e) {
            BotLogger.severe(LOGTAG, e);
        }
    }

    public List<String> getProfessors() {
        return new ArrayList<String>() {{
//            add("AA");
//            add("BB");
        }};
    }
}
