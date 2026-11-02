package zm.clinic;

public class QueueTicket {

    public static final String WAITING = "WAITING";
    public static final String BEING_ATTENDED = "BEING_ATTENDED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private String ticketNumber;
    private Patient patient;
    private String status;
    private boolean urgent;

    public QueueTicket(String ticketNumber, Patient patient) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number cannot be empty.");
        }
        if (patient == null) {
            throw new IllegalArgumentException("Ticket patient is required.");
        }
        this.ticketNumber = ticketNumber;
        this.patient = patient;
        this.status = WAITING;
        this.urgent = false;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public String getStatus() {
        return status;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public int priorityOrder() {
        return urgent ? 1 : 2;
    }

    public void setStatus(String status) {
        if (!WAITING.equals(status)
                && !BEING_ATTENDED.equals(status)
                && !COMPLETED.equals(status)
                && !CANCELLED.equals(status)) {
            throw new IllegalArgumentException("Invalid queue status.");
        }

        this.status = status;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
}
