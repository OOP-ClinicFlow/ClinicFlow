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
  
  # THE PROBLEM
  Small clinics often manage patient queues manually using paper notebooks, which leads to lost records, unclear waiting times, and missed urgent cases. ClinicFlow solves this for receptionists and clinic staff by providing a digital dashboard that registers patients, issues unique tickets, and automatically organizes the queue so that emergencies and senior citizens are prioritized correctly.

  # HOW TO RUN
  Follow the following steps to run the project locally:

1. Install Prerequisites: Ensure you have Java (JDK 17 or higher) and Maven installed on your computer.

2. Clone the Repository: Open your terminal and run git clone https://github.com/OOP-ClinicFlow/ClinicFlow

3. Open the Project: Open the cloned folder in your preferred IDE (IntelliJ IDEA, Eclipse, or VS Code).

4. Run the Server: Locate the App.java (or Main.java) file and run its main method. Alternatively, run mvn javalin:run in the terminal.

5. Access the Frontend: Open your web browser and navigate to http://localhost:7070. The queue dashboard will load, and you can start registering patients.

# OUR CLASSES
Here is a breakdown of the classes that power our backend:

`Patient`: Stores the patient's personal information (ID, full name, age) and enforces encapsulation through validation in its setters.

`QueueTicket`: The parent class representing a patient's place in the queue, storing the ticket number, status, and urgency.

`StandardTicket`: Inherits from QueueTicket and defines the standard priority order for regular patients.

`UrgentTicket`: Inherits from QueueTicket and overrides the priority order to ensure emergency patients are seen first (Polymorphism).

`QueueManager`: The core logic class that handles registering patients, generating unique ticket numbers, updating statuses, and sorting the queue.

`FileQueueStorage`: Handles data persistence by saving queue records to a local file and loading them back into memory when the server restarts.

`QueueException & QueueOperationException`: Custom exceptions used to handle invalid operations (like empty names or failed file saves) and return clean error messages to the user.

`Main`: Configures the Javalin server, defines all REST API endpoints, and serves the HTML/JS frontend.


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
