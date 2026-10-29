package zm.clinic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileQueueStorage {

    private final Path filePath;

    public FileQueueStorage(String fileName) {
        this.filePath = Path.of(fileName);
    }

    public void saveTickets(List<QueueTicket> tickets)
            throws QueueOperationException {

        List<String> lines = new ArrayList<>();

        for (QueueTicket ticket : tickets) {
            Patient patient = ticket.getPatient();

            String line =
                    encode(ticket.getTicketNumber()) + "|"
                    + encode(patient.getPatientId()) + "|"
                    + encode(patient.getFullName()) + "|"
                    + ticket.getStatus() + "|"
                    + ticket.isUrgent();

            lines.add(line);
        }

        try {
            Files.write(filePath, lines,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new QueueOperationException(
                    "Could not save queue records.", e);
        }
    }

    public List<QueueTicket> loadTickets()
            throws QueueOperationException {

        List<QueueTicket> tickets = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return tickets;
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath, StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length != 5) {
                    throw new IllegalArgumentException(
                            "Invalid record format.");
                }

                String ticketNumber = decode(parts[0]);
                String patientId = decode(parts[1]);
                String fullName = decode(parts[2]);
                String status = parts[3];

                if (!parts[4].equals("true")
                        && !parts[4].equals("false")) {
                    throw new IllegalArgumentException(
                            "Invalid urgent value.");
                }

                boolean urgent =
                        Boolean.parseBoolean(parts[4]);

                Patient patient =
                        new Patient(patientId, fullName);

                QueueTicket ticket =
                        new QueueTicket(ticketNumber, patient);

                ticket.setStatus(status);
                ticket.setUrgent(urgent);

                tickets.add(ticket);
            }

            return tickets;

        } catch (IOException | IllegalArgumentException e) {
            throw new QueueOperationException(
                    "Could not load queue records.", e);
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(
                Base64.getDecoder().decode(value),
                StandardCharsets.UTF_8);
    }
}
