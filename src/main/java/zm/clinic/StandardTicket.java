
package zm.clinic;

public class StandardTicket extends QueueTicket {

    public StandardTicket(String ticketNumber, Patient patient) {
        super(ticketNumber, patient);
    }

    @Override
    public int priorityOrder() {
        return 2;
    }
}