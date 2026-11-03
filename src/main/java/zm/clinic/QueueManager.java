package zm.clinic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueManager {

    private static final Pattern GENERATED_TICKET_NUMBER =
            Pattern.compile("^Q(\\d+)$");
    private static final int SENIOR_AGE_THRESHOLD = 75;

    private final Map<String, QueueTicket> tickets = new LinkedHashMap<>();
    private final FileQueueStorage storage;
    private int nextTicketNumber = 1;

    public QueueManager() {
        this(new FileQueueStorage(
                System.getProperty("clinicflow.queue.file", "queue-data.txt")));
    }

    public QueueManager(FileQueueStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Queue storage is required.");
        }

        this.storage = storage;
        loadStoredTickets();
    }

    public synchronized QueueTicket registerPatient(String fullName) {
        return registerPatient(null, fullName, false);
    }

    public synchronized QueueTicket registerPatient(
            String patientId, String fullName, boolean urgent) {
        return registerPatient(patientId, fullName, urgent, null);
    }

    public synchronized QueueTicket registerPatient(
            String patientId, String fullName, boolean urgent, Integer age) {
        String ticketNumber = formatTicketNumber(nextTicketNumber);
        Patient patient = new Patient(
                patientId == null || patientId.isBlank()
                        ? ticketNumber : patientId.trim(),
                fullName == null ? null : fullName.trim(),
                age);

        QueueTicket ticket = urgent
                ? new UrgentTicket(ticketNumber, patient)
                : new StandardTicket(ticketNumber, patient);
        tickets.put(ticketNumber, ticket);

        try {
            saveTickets();
        } catch (QueueException e) {
            tickets.remove(ticketNumber);
            throw e;
        }

        nextTicketNumber++;
        return ticket;
    }

    public synchronized List<QueueTicket> getAllTickets() {
        List<QueueTicket> result = new ArrayList<>(tickets.values());
        result.sort(Comparator
                .comparingInt((QueueTicket ticket) -> statusOrder(ticket.getStatus()))
                .thenComparingInt(this::waitingPriorityOrder));
        return result;
    }

    private int waitingPriorityOrder(QueueTicket ticket) {
        if (!QueueTicket.WAITING.equals(ticket.getStatus())) {
            return 0;
        }
        if (ticket.isUrgent()) {
            return 0;
        }

        Integer age = ticket.getPatient().getAge();
        if (age != null && age > SENIOR_AGE_THRESHOLD) {
            return 1;
        }
        return ticket.priorityOrder();
    }

    public synchronized QueueTicket findTicket(String ticketNumber) {
        return ticketNumber == null ? null : tickets.get(ticketNumber);
    }

    public synchronized void updateStatus(String ticketNumber, String newStatus) {
        QueueTicket ticket = requireTicket(ticketNumber);
        String previousStatus = ticket.getStatus();

        ticket.setStatus(newStatus);
        try {
            saveTickets();
        } catch (QueueException e) {
            ticket.setStatus(previousStatus);
            throw e;
        }
    }

    public synchronized boolean markUrgent(String ticketNumber) {
        QueueTicket ticket = findTicket(ticketNumber);
        if (ticket == null || ticket.isUrgent()) {
            return ticket != null;
        }

        ticket.setUrgent(true);
        try {
            saveTickets();
        } catch (QueueException e) {
            ticket.setUrgent(false);
            throw e;
        }
        return true;
    }

    public synchronized boolean cancelTicket(String ticketNumber) {
        QueueTicket ticket = findTicket(ticketNumber);
        if (ticket == null || "CANCELLED".equals(ticket.getStatus())) {
            return false;
        }

        String previousStatus = ticket.getStatus();
        ticket.setStatus("CANCELLED");
        try {
            saveTickets();
        } catch (QueueException e) {
            ticket.setStatus(previousStatus);
            throw e;
        }
        return true;
    }

    public synchronized boolean removeTicket(String ticketNumber) {
        return cancelTicket(ticketNumber);
    }

    public synchronized void resetQueue() {
        Map<String, QueueTicket> previousTickets = new LinkedHashMap<>(tickets);
        int previousNextTicketNumber = nextTicketNumber;
        tickets.clear();
        nextTicketNumber = 1;

        try {
            saveTickets();
        } catch (QueueException e) {
            tickets.putAll(previousTickets);
            nextTicketNumber = previousNextTicketNumber;
            throw new IllegalStateException("Could not reset the queue.", e);
        }
    }

    private void loadStoredTickets() {
        try {
            for (QueueTicket ticket : storage.loadTickets()) {
                if (ticket == null || ticket.getTicketNumber() == null
                        || tickets.putIfAbsent(
                                ticket.getTicketNumber(), ticket) != null) {
                    throw new QueueException("Queue data contains duplicate or invalid tickets.");
                }
                nextTicketNumber = Math.max(
                        nextTicketNumber, nextTicketNumberAfter(ticket.getTicketNumber()));
            }
        } catch (QueueOperationException e) {
            throw new QueueException("Could not load the saved queue.", e);
        }
    }

    private QueueTicket requireTicket(String ticketNumber) {
        QueueTicket ticket = findTicket(ticketNumber);
        if (ticket == null) {
            throw new IllegalArgumentException("Queue ticket not found.");
        }
        return ticket;
    }

    private void saveTickets() {
        try {
            storage.saveTickets(new ArrayList<>(tickets.values()));
        } catch (QueueOperationException e) {
            throw new QueueException("Could not save the queue.", e);
        }
    }

    private static int statusOrder(String status) {
        return switch (status) {
            case "WAITING" -> 0;
            case "BEING_ATTENDED" -> 1;
            case "COMPLETED" -> 2;
            case "CANCELLED" -> 3;
            default -> 4;
        };
    }

    private static String formatTicketNumber(int number) {
        return String.format(Locale.ROOT, "Q%03d", number);
    }

    private static int nextTicketNumberAfter(String ticketNumber) {
        Matcher matcher = GENERATED_TICKET_NUMBER.matcher(ticketNumber);
        if (!matcher.matches()) {
            return 1;
        }

        try {
            return Math.addExact(Integer.parseInt(matcher.group(1)), 1);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new QueueException("Saved ticket number is out of range.", e);
        }
    }

}
