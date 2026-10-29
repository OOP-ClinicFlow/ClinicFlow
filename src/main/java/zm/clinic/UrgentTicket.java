package zm.clinic;

public class UrgentTicket extends QueueTicket {

    public UrgentTicket(String ticketNumber, Patient patient) {
        super(ticketNumber, patient);
        setUrgent(true);
    }

    @Override
    public int priorityOrder() {
        return 1;
    }
}