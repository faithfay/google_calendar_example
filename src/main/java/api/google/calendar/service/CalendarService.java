package api.google.calendar.service;

import api.google.calendar.App;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class CalendarService {
    //日曆分類的ID
    private static final String CALENDAR_ID = "xxxxxxxx@group.calendar.google.com";
    private static final String APPLICATION_NAME = "MyCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String TOKENS_DIRECTORY_PATH = "./tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    //Google Oauth2.0 驗證方式
    public Calendar googleAuth() throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        System.out.println(CALENDAR_ID);

        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        Calendar calendarService = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user"))
                .setApplicationName(APPLICATION_NAME)
                .build();

        return calendarService;
    }

    public void setEvent(Calendar service) throws IOException {
        //事件標題及內容
        Event event = new Event()
                .setSummary("this is title")
                .setDescription("one\ntwo\nthere");

        //我是使用全天的事件,所以只需要填年月日
        DateTime dateTime = new DateTime("2018-09-30");

        //開始
        EventDateTime start = new EventDateTime()
                .setDate(dateTime)
                .setTimeZone("Asia/Taipei");
        event.setStart(start);

        //結束
        EventDateTime end = new EventDateTime()
                .setDate(dateTime)
                .setTimeZone("Asia/Taipei");
        event.setEnd(end);
        event = service.events().insert(CALENDAR_ID, event).execute();

        System.out.printf("success setting: ", event.getHtmlLink());
    }

    public void updateEvent(String eventId) throws IOException, GeneralSecurityException {
        Event event = googleAuth().events().get(CALENDAR_ID,eventId).execute();
        event.setSummary("new summary");
        event.setDescription("new description");

        googleAuth().events().update(CALENDAR_ID, event.getId(), event).execute();
        System.out.println("event update success.");
    }

    public void delEvent(String eventId) throws IOException, GeneralSecurityException {
        googleAuth().events().delete(CALENDAR_ID, eventId).execute();
        System.out.println("event delete success.");
    }

    public void getEvent(String eventId) throws IOException, GeneralSecurityException {
        Event event = googleAuth().events().get(CALENDAR_ID,eventId).execute();

        System.out.println("event date: " + event.getStart().getDate().toString());
        System.out.println("event id: " + event.getId());
        System.out.println("summary: " + event.getSummary());
        System.out.println("description: " + event.getDescription());
    }

    public void getAllEvent() throws IOException, GeneralSecurityException {
        String pageToken = null;
        do {
            Events events = googleAuth().events().list(CALENDAR_ID).setPageToken(pageToken).execute();
            List<Event> items = events.getItems();
            for (Event event:items) {
                System.out.println("event id: " + event.getId());
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
    }

    public void getCalendarId() throws IOException, GeneralSecurityException {
        String pageToken = null;
        do {
            CalendarList calendarList = googleAuth().calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : items) {
                System.out.println("summary: " + calendarListEntry.getSummary() + " id: " + calendarListEntry.getId());
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
    }
}
