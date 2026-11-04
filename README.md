# ClinicFlow
A clinic queue management system built with Java and Javalin — register patients, issue unique tickets, track statuses, and manage urgent cases digitally.

## GROUP MEMBERS
  |-----------------------------------------------------------|
# | Member Name            | ID          | Programme of study |
  |-----------------------------------------------------------|
  |1. Mercyton Sinkala     | 2420931     | BSE                |
  |-----------------------------------------------------------|
  |2. Prince Kanyembo      | 2420926     | BSE                |
  |-----------------------------------------------------------|
  |3. Amos Kabashi         | 2420961     | BSE                |
  |-----------------------------------------------------------|
  |4. John B Mumba         | 2420949     | BSE                |
  |-----------------------------------------------------------|
  |5. Valarie Kasongo      | 2410073     | BSE                |
  |-----------------------------------------------------------|
  

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
The dashboard's **Clear all** action empties the saved queue after confirmation
and restarts ticket numbering at Q001.
The project uses Maven to provide Javalin's JSON mapper; reload the Maven
project after changing dependencies, then restart the app so the updated
runtime classpath is used.

The registration form sends the patient's name, age, NRC, and ordinary or
emergency status to `POST /api/queue`. The queue page displays those details
and supports attending, completing, and cancelling tickets. Age is persisted
for newly registered patients; previously saved queue records remain readable.
Existing API clients can continue registering with `fullName`; `patientId` and
`urgent=true` (or `patientStatus=Emergency`) remain supported query parameters.
