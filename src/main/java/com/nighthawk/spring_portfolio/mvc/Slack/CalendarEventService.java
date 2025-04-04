package com.nighthawk.spring_portfolio.mvc.Slack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private SlackService slackService; // Assuming you have a Slack service to send messages

    // Save a new event
    public CalendarEvent saveEvent(CalendarEvent event) {
        CalendarEvent savedEvent = calendarEventRepository.save(event);
        slackService.sendMessage("New Event Added:\n" +
                "Title: " + savedEvent.getTitle() + "\n" +
                "Description: " + savedEvent.getDescription() + "\n" +
                "Date: " + savedEvent.getDate() + "\n" +
                "Type: " + savedEvent.getType() + "\n" +
                "Period: " + savedEvent.getPeriod());
        return savedEvent;
    }

    // Get events by a specific date
    public List<CalendarEvent> getEventsByDate(LocalDate date) {
        return calendarEventRepository.findByDate(date);
    }

    // Update event by id
    public boolean updateEventById(int id, String newTitle, String description, LocalDate date) {
        CalendarEvent event = getEventById(id);
        if (event != null) {
            String oldDetails = "Old Event Details:\n" +
                    "Title: " + event.getTitle() + "\n" +
                    "Description: " + event.getDescription() + "\n" +
                    "Date: " + event.getDate() + "\n" +
                    "Type: " + event.getType() + "\n" +
                    "Period: " + event.getPeriod();

            event.setTitle(newTitle);
            event.setDescription(description);
            event.setDate(date);
            calendarEventRepository.save(event);

            String newDetails = "Updated Event Details:\n" +
                    "Title: " + event.getTitle() + "\n" +
                    "Description: " + event.getDescription() + "\n" +
                    "Date: " + event.getDate() + "\n" +
                    "Type: " + event.getType() + "\n" +
                    "Period: " + event.getPeriod();

            slackService.sendMessage("Event Updated:\n" + oldDetails + "\n\n" + newDetails);
            return true;
        }
        return false;
    }

    // Delete event by id
    public boolean deleteEventById(int id) {
        CalendarEvent event = getEventById(id);
        if (event != null) {
            calendarEventRepository.delete(event);
            slackService.sendMessage("Event Deleted:\n" +
                    "Title: " + event.getTitle() + "\n" +
                    "Description: " + event.getDescription() + "\n" +
                    "Date: " + event.getDate() + "\n" +
                    "Type: " + event.getType() + "\n" +
                    "Period: " + event.getPeriod());
            return true;
        }
        return false;
    }

    // Delete event by title
    public boolean deleteEventByTitle(String title) {
        // Retrieve all events from the repository
        List<CalendarEvent> allEvents = calendarEventRepository.findAll(); 
    
        // Filter events that match the given title
        List<CalendarEvent> eventsToDelete = allEvents.stream()
                .filter(event -> event.getTitle().equals(title))
                .toList();
    
        // If there are events to delete
        if (!eventsToDelete.isEmpty()) {
            // Delete each event manually
            eventsToDelete.forEach(calendarEventRepository::delete);

            return true;
        }
    
        // If no events matched the title, return false
        return false;
    }

    // Get events within a date range
    public List<CalendarEvent> getEventsWithinDateRange(LocalDate startDate, LocalDate endDate) {
        return calendarEventRepository.findByDateBetween(startDate, endDate);
    }

    // Retrieve all events
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    // Get event by id
    public CalendarEvent getEventById(int id) {
        return calendarEventRepository.findById((long) id).orElse(null);
    }

    // Parse Slack message and create events
    public void parseSlackMessage(Map<String, String> jsonMap, LocalDate weekStartDate) {
        List<CalendarEvent> events = extractEventsFromText(jsonMap, weekStartDate);
        for (CalendarEvent event : events) {
            saveEvent(event); // Save each parsed event
        }
    }

    // Test ID's
    //final String CSP_CHANNEL_ID = "C07SJSF6RQA";
    //final String CSA_CHANNEL_ID = "C07RRJDU5M5";
    //final String CSSE_CHANNEL_ID = "C07RRJ5GECX";
    // Official
    final String CSP_CHANNEL_ID = "CUS8E3M6Z";
    final String CSA_CHANNEL_ID = "CRRJL1F1D";
    final String CSSE_CHANNEL_ID = "C05MNRWC2A1";

    private List<CalendarEvent> extractEventsFromText(Map<String, String> jsonMap, LocalDate weekStartDate) {
        String text = jsonMap.get("text");
        List<CalendarEvent> events = new ArrayList<>();
        Pattern dayPattern = Pattern.compile("\\[(Mon|Tue|Wed|Thu|Fri|Sat|Sun)(?: - (Mon|Tue|Wed|Thu|Fri|Sat|Sun))?\\]:\\s*(\\*\\*|\\*)?\\s*(.+)");
        Pattern descriptionPattern = Pattern.compile("(\\*\\*|\\*)?\\s*\\u2022\\s*(.+)");

        String[] lines = text.split("\\n");
        CalendarEvent lastEvent = null;

        for (String line : lines) {
            Matcher dayMatcher = dayPattern.matcher(line);

            if (dayMatcher.find()) {
                String startDay = dayMatcher.group(1);
                String endDay = dayMatcher.group(2) != null ? dayMatcher.group(2) : startDay;
                String asterisks = dayMatcher.group(3);
                String currentTitle = dayMatcher.group(4).trim();
                String period = "0";
                switch(jsonMap.get("channel")) {
                    case(CSP_CHANNEL_ID):
                        period = "CSP";
                        break;
                    case(CSA_CHANNEL_ID):
                        period = "CSA";
                        break;
                    case(CSSE_CHANNEL_ID):
                        period = "CSSE";
                        break;
                }
                String type = "daily plan";
                if ("*".equals(asterisks)) {
                    type = "check-in";
                } else if ("**".equals(asterisks)) {
                    type = "grade";
                }

                for (LocalDate date : getDatesInRange(startDay, endDay, weekStartDate)) {
                    lastEvent = new CalendarEvent(date, currentTitle, "", type, period);
                    events.add(lastEvent);
                }
            } else {
                Matcher descMatcher = descriptionPattern.matcher(line);
                if (descMatcher.find() && lastEvent != null) {
                    String description = descMatcher.group(2).trim();
                    String asterisks = descMatcher.group(1);

                    String type = lastEvent.getType();
                    if ("*".equals(asterisks)) {
                        type = "check-in";
                    } else if ("**".equals(asterisks)) {
                        type = "grade";
                    }

                    lastEvent.setDescription(lastEvent.getDescription() + (lastEvent.getDescription().isEmpty() ? "" : ", ") + description);
                    lastEvent.setType(type);
                }
            }
        }
        return events;
    }

    // Helper to generate dates in the range [startDay - endDay]
    private List<LocalDate> getDatesInRange(String startDay, String endDay, LocalDate weekStartDate) {
        List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        int startIndex = days.indexOf(startDay);
        int endIndex = days.indexOf(endDay);

        List<LocalDate> dateRange = new ArrayList<>();
        if (startIndex != -1 && endIndex != -1) {
            for (int i = startIndex; i <= endIndex; i++) {
                dateRange.add(weekStartDate.plusDays(i - weekStartDate.getDayOfWeek().getValue() + 1));
            }
        }
        return dateRange;
    }
}


