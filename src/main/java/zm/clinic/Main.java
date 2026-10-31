package zm.clinic;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

public class Main {

    public static void main(String[] args) {

        QueueManager queueManager = new QueueManager();
        AuthManager authManager = new AuthManager();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        });

        app.get("/", ctx -> {
            ctx.result("Clinic Queue Management System Backend is Running!");
        });

        // LOGIN
        app.post("/api/auth/login", ctx -> {
            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);

            String token = authManager.login(
                    request.getUsername(),
                    request.getPassword()
            );

            if (token == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new MessageResponse("Incorrect username or password."));
                return;
            }

            AuthManager.StaffAccount staff =
                    authManager.getStaffForToken(token);

            ctx.json(new LoginResponse(
                    token,
                    staff.getUsername(),
                    staff.getRole()
            ));
        });

        // SIGN OUT
        app.post("/api/auth/logout", ctx -> {
            String token = getToken(ctx.header("Authorization"));

            if (token == null || !authManager.logout(token)) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new MessageResponse("You are not signed in."));
                return;
            }

            ctx.json(new MessageResponse("Signed out successfully."));
        });

        // Protect queue routes that include a path parameter or sub-route.
        app.before("/api/queue/*", ctx -> {
            String token = getToken(ctx.header("Authorization"));

            if (authManager.getStaffForToken(token) == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new MessageResponse("Please sign in to use the queue."));
                ctx.skipRemainingHandlers();
            }
        });

        // Protect the exact /api/queue route as well.
        app.before("/api/queue", ctx -> {
            String token = getToken(ctx.header("Authorization"));

            if (authManager.getStaffForToken(token) == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new MessageResponse("Please sign in to use the queue."));
                ctx.skipRemainingHandlers();
            }
        });

        // REGISTER PATIENT
        app.post("/api/queue", ctx -> {
            String fullName = ctx.queryParam("fullName");

            try {
                QueueTicket ticket = queueManager.registerPatient(fullName);
                ctx.status(HttpStatus.CREATED);
                ctx.json(ticket);
            } catch (QueueException e) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result(e.getMessage());
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
                    "Queue ticket " + ticketNumber + " removed successfully."
            ));
        });

        app.start(7070);
        System.out.println("Server started at http://localhost:7070");
    }

    private static String getToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private final String token;
        private final String username;
        private final String role;

        public LoginResponse(String token, String username, String role) {
            this.token = token;
            this.username = username;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
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
