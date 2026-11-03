# ClinicFlow
A clinic queue management system built with Java and Javalin — register patients, issue unique tickets, track statuses, and manage urgent cases digitally.

## Project structure

```text
pom.xml
src/
  main/
    java/
      App.java
      zm/clinic/
        Main.java
        Patient.java
        QueueTicket.java
        QueueManager.java
        QueueException.java
        FileQueueStorage.java
    resources/
      public/
        app.js
        index.html
        style.css
```

## Queue workflow

`QueueManager` owns ticket registration, ticket numbering, priority ordering,
status updates, cancellation, and persistence. It loads and saves tickets
through `FileQueueStorage`; by default records are stored in `queue-data.txt`
in the application's working directory. Set the `clinicflow.queue.file` Java
system property to use a different file.

Waiting urgent tickets are ordered ahead of standard tickets while retaining
registration order within each priority. Cancelling a ticket marks it
`CANCELLED` and keeps the record for queue history. Run `App` to start the
Javalin application on port 7070; it serves the registration page at `/`.
`App` delegates to `zm.clinic.Main`, which owns the API and queue.
The project uses Maven to provide Javalin's JSON mapper; reload the Maven
project after changing dependencies, then restart the app so the updated
runtime classpath is used.

The registration form sends the patient's name, age, NRC, and ordinary or
emergency status to `POST /api/queue`. The queue page displays those details
and supports attending, completing, and cancelling tickets. Age is persisted
for newly registered patients; previously saved queue records remain readable.
Existing API clients can continue registering with `fullName`; `patientId` and
`urgent=true` (or `patientStatus=Emergency`) remain supported query parameters.
