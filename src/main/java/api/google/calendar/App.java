package api.google.calendar;


import api.google.calendar.service.CalendarService;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class App {

    public static void main( String[] args ) {
        try {
            CalendarService calendarService = new CalendarService();
            calendarService.getCalendarId();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
