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
`CANCELLED` and keeps the record for queue history. Existing API clients can
continue registering with `fullName`; `patientId` and `urgent=true` (or
`patientStatus=Emergency`) are optional query parameters.
