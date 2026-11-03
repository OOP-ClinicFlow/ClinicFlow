package zm.clinic;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;

public class Main {

    public static void main(String[] args) {

        QueueManager queueManager = new QueueManager();
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        });

        // REGISTER PATIENT
        app.post("/api/queue", ctx -> {
            try {
                RegistrationRequest request = ctx.body().isBlank()
                        ? null : ctx.bodyAsClass(RegistrationRequest.class);
                String fullName = request == null
                        ? ctx.queryParam("fullName")
                        : request.name != null ? request.name : request.fullName;
                String patientId = request == null
                        ? ctx.queryParam("patientId")
                        : request.nrc != null ? request.nrc : request.patientId;
                String patientStatus = request == null
                        ? ctx.queryParam("patientStatus") : request.patientStatus;
                String priority = request == null ? null : request.priority;
                boolean urgent = request != null && request.urgent
                        || "true".equalsIgnoreCase(ctx.queryParam("urgent"))
                        || "Emergency".equalsIgnoreCase(patientStatus)
                        || "Emergency".equalsIgnoreCase(priority)
                        || "URGENT".equalsIgnoreCase(priority);
                Integer age = request == null ? null : request.age;
                QueueTicket ticket = queueManager.registerPatient(
                        patientId, fullName, urgent, age);
                ctx.status(HttpStatus.CREATED);
                ctx.json(ticket);
            } catch (IllegalArgumentException e) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new MessageResponse(e.getMessage()));
            }
        });

        // GET ALL TICKETS
        app.get("/api/queue", ctx -> {
            ctx.json(queueManager.getAllTickets());
        });

        // RESET QUEUE
        app.post("/api/queue/reset", ctx -> {
            try {
                queueManager.resetQueue();

                ctx.json(new MessageResponse(
                        "Queue reset successfully. The next patient will receive Q001."
                ));
            } catch (IllegalStateException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(new MessageResponse("Could not reset the queue."));
            }
        });

        // GET ONE TICKET
        app.get("/api/queue/{ticketNumber}", ctx -> {
            String ticketNumber = ctx.pathParam("ticketNumber");
            QueueTicket ticket = queueManager.findTicket(ticketNumber);

            if (ticket == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(new MessageResponse("Queue ticket not found."));
                return;
            }

            ctx.json(ticket);
        });

        // UPDATE STATUS
        app.put("/api/queue/{ticketNumber}/status", ctx -> {
            String ticketNumber = ctx.pathParam("ticketNumber");
            String newStatus = ctx.queryParam("status");

            QueueTicket ticket = queueManager.findTicket(ticketNumber);

            if (ticket == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(new MessageResponse("Queue ticket not found."));
                return;
            }

            if (newStatus == null || newStatus.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new MessageResponse("New status is required."));
                return;
            }

            try {
                queueManager.updateStatus(ticketNumber, newStatus);
                ctx.json(queueManager.findTicket(ticketNumber));
            } catch (IllegalArgumentException e) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new MessageResponse(e.getMessage()));
            }
        });

        // MARK URGENT
        app.put("/api/queue/{ticketNumber}/priority", ctx -> {
            String ticketNumber = ctx.pathParam("ticketNumber");

            boolean updated = queueManager.markUrgent(ticketNumber);

            if (!updated) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(new MessageResponse("Queue ticket not found."));
                return;
            }

            ctx.json(queueManager.findTicket(ticketNumber));
        });

        // DELETE TICKET
        app.delete("/api/queue/{ticketNumber}", ctx -> {
            String ticketNumber = ctx.pathParam("ticketNumber");

            boolean removed = queueManager.removeTicket(ticketNumber);

            if (!removed) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(new MessageResponse("Queue ticket not found."));
                return;
            }

            ctx.json(new MessageResponse(
                    "Queue ticket " + ticketNumber + " cancelled successfully."
            ));
        });

        int port = Integer.getInteger("clinicflow.port", 7070);
        app.start(port);
        System.out.println("Server started at http://localhost:" + port);
    }

    public static class RegistrationRequest {
        public String name;
        public String fullName;
        public Integer age;
        public String nrc;
        public String patientId;
        public String priority;
        public String patientStatus;
        public boolean urgent;
    }

    public static class MessageResponse {
        private final String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
