package zm.clinic;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        Javalin app = Javalin.create();

        app.get("/", ctx -> {
            ctx.result("Clinic Queue Management System Backend is Running!");
        });

        app.start(7070);

        System.out.println("Server started at http://localhost:7070");
    }
}
