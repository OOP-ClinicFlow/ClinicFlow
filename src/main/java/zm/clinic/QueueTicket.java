package zm.clinic;

public class QueueTicket {

    private String ticketNumber;
    private Patient patient;
    private String status;
    private boolean urgent;

    public QueueTicket(String ticketNumber, Patient patient) {
        this.ticketNumber = ticketNumber;
        this.patient = patient;
        this.status = "WAITING";
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
        if (!status.equals("WAITING")
                && !status.equals("BEING_ATTENDED")
                && !status.equals("COMPLETED")) {
            throw new IllegalArgumentException("Invalid queue status.");
        }

        this.status = status;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
}
