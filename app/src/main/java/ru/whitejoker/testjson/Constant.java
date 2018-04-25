package ru.whitejoker.testjson;

import java.util.ArrayList;

public class Constant {
    public static String LOG_TAG = "my_log";
    //public static final String IP_ADR = "http://192.168.1.107:8080/";//house
    public static final String IP_ADR = "http://10.10.10.63:8080/";//work

    public static final String ID_COLUMN = "Номер";
    public static final String SUBJECT_COLUMN = "Тема";
    private static ArrayList<String> ticketNumberArrayList = new ArrayList<String>();//массив номеров заявок

    public static ArrayList<String> clearTicketNumberArrayList() {
        ticketNumberArrayList.clear();
        return ticketNumberArrayList;
    }
    public static ArrayList<String> getTicketNumberArrayList () {
        return ticketNumberArrayList;
    }
    public static ArrayList<String> setAddTicketNumber (String ticketNumber) {
        if (ticketNumberArrayList.contains(ticketNumber)) {
            return null;
        }
        ticketNumberArrayList.add(ticketNumber);
        return ticketNumberArrayList;
    }
    public static String getTicketNumber(int position) {
        String ticketNumber = ticketNumberArrayList.get(position);
        return ticketNumber;
    }

}
