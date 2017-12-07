package com.intetics.organizerbot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class Keyboards {

    private static ResourceBundle buttons = ResourceBundle.getBundle("buttons");
    private static ResourceBundle days = ResourceBundle.getBundle("days");
    private static ResourceBundle twoLetterDays = ResourceBundle.getBundle("twoletterdays");
    private static ResourceBundle classTypes = ResourceBundle.getBundle("classtypes");

    public static ReplyKeyboardMarkup getReturnToMenuKeyboard() {
        List<List<String>> rows = new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add(buttons.getString("mainMenu"));
            }});
        }};
        return makeKeyboard(rows);
    }

    public static ReplyKeyboardMarkup getMainMenuKeyboard() {
        List<List<String>> rows = new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add(buttons.getString("showTimetable"));
            }});
            add(new ArrayList<String>() {{
                add(buttons.getString("addClass"));
            }});
//            add(new ArrayList<String>() {{
//                add(buttons.getString("addEvent"));
//            }});
//            add(new ArrayList<String>() {{
//                add(buttons.getString("addSubject"));
//            }});
//            add(new ArrayList<String>() {{
//                add(buttons.getString("removeSubject"));
//            }});
        }};
        return makeKeyboard(rows);
    }

//    public static ReplyKeyboardMarkup getAddSubjectKeyboard() {
//        List<List<String>> rows = new ArrayList<List<String>>() {{
//            add(new ArrayList<String>() {{
//                add(buttons.getString("back"));
//            }});
//        }};
//        return makeKeyboard(rows);
//    }

    public static ReplyKeyboardMarkup getListKeyboard(List<String> list) {
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(new ArrayList<String>() {{
            add(item);
        }}));
        rows.add(new ArrayList<String>() {{
            add(buttons.getString("mainMenu"));
        }});
        return makeKeyboard(rows);
    }

    private static ReplyKeyboardMarkup makeKeyboard(List<List<String>> rows) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        rows.forEach(row -> addKeyboardRow(keyboard, row));
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private static void addKeyboardRow(List<KeyboardRow> keyboard, List<String> row) {
        KeyboardRow keyboardRow = new KeyboardRow();
        row.forEach(item -> keyboardRow.add(item));
        keyboard.add(keyboardRow);
    }

    public static ReplyKeyboard getDaysListKeyboard() {
        List<List<String>> rows = new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add(days.getString("monday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("tuesday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("wednesday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("thursday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("friday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("saturday"));
            }});
            add(new ArrayList<String>() {{
                add(days.getString("sunday"));
            }});
            add(new ArrayList<String>() {{
                add(buttons.getString("mainMenu"));
            }});
        }};
        return makeKeyboard(rows);
    }

    public static ReplyKeyboard getCalendarKeyboard() {
        return getCalendarKeyboard(YearMonth.now());
    }

    public static InlineKeyboardMarkup getCalendarKeyboard(YearMonth yearMonth) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<List<InlineKeyboardButton>>();
        addYearMonth(keyboard, yearMonth);
        addDaysOfWeek(keyboard);
        addDates(keyboard, yearMonth);
        addArrows(keyboard, yearMonth);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private static void addArrows(List<List<InlineKeyboardButton>> keyboard, YearMonth yearMonth) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("<--").setCallbackData("no");
        button.setCallbackData("goto:" + yearMonth.minusMonths(1).toString());
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("-->").setCallbackData("no");
        button.setCallbackData("goto:" + yearMonth.plusMonths(1).toString());
        row.add(button);
        keyboard.add(row);
    }

    private static void addDates(List<List<InlineKeyboardButton>> keyboard, YearMonth yearMonth) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        int currentDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();
        for (int i = 1; i < currentDayOfWeek; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            row.add(button.setText(" ").setCallbackData("no"));
        }
        LocalDate date = yearMonth.atDay(1);
        do {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData("choose:" + date.toString());
            button.setText(String.valueOf(date.getDayOfMonth()));
            row.add(button);
            if (currentDayOfWeek == 7 && date.getDayOfMonth() < yearMonth.lengthOfMonth()) {
                currentDayOfWeek = 1;
                keyboard.add(row);
                row = new ArrayList<>();
            } else {
                currentDayOfWeek++;
            }
            date = date.plusDays(1);
        } while (date.getDayOfMonth() != 1);
        for (int i = currentDayOfWeek; i <= 7; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            row.add(button.setText(" ").setCallbackData("no"));
        }
        keyboard.add(row);
    }

    private static void addYearMonth(List<List<InlineKeyboardButton>> keyboard, YearMonth yearMonth) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + " " + yearMonth.getYear());
        button.setCallbackData("no");
        keyboard.add(new ArrayList<InlineKeyboardButton>() {{
            add(button);
        }});
    }

    private static void addDaysOfWeek(List<List<InlineKeyboardButton>> keyboard) {
        keyboard.add(new ArrayList<InlineKeyboardButton>() {{
            add(makeInlineButton(twoLetterDays.getString("monday")));
            add(makeInlineButton(twoLetterDays.getString("tuesday")));
            add(makeInlineButton(twoLetterDays.getString("wednesday")));
            add(makeInlineButton(twoLetterDays.getString("thursday")));
            add(makeInlineButton(twoLetterDays.getString("friday")));
            add(makeInlineButton(twoLetterDays.getString("saturday")));
            add(makeInlineButton(twoLetterDays.getString("sunday")));
        }});
    }

    private static InlineKeyboardButton makeInlineButton(String text){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData("no");
        return button;
    }

    public static ReplyKeyboardMarkup getOneTimeOrWeeklyKeyboard() {
        List<List<String>> rows = new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add(buttons.getString("oneTime"));
            }});
            add(new ArrayList<String>() {{
                add(buttons.getString("weekly"));
            }});
            add(new ArrayList<String>() {{
                add(buttons.getString("mainMenu"));
            }});
        }};
        return makeKeyboard(rows);
    }

    public static ReplyKeyboard getClassTypesListKeyboard() {
        List<String> types = new ArrayList();
        classTypes.keySet().stream().forEach(k -> types.add(classTypes.getString(k)));
        List<List<String>> rows = new ArrayList<>();
        types.forEach(type -> rows.add(new ArrayList<String>() {{
            add(type);
        }}));
        rows.add(new ArrayList<String>() {{
            add(buttons.getString("mainMenu"));
        }});
        return makeKeyboard(rows);
    }

    public static ReplyKeyboard getViewsKeyboard() {
        return getListKeyboard(new ArrayList<String>(){{
            add(buttons.getString("forToday"));
            add(buttons.getString("forTomorrow"));
            add(buttons.getString("forOtherDate"));
            add(buttons.getString("forWeek"));
        }});
    }
}
