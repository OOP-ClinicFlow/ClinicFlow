const registrationForm = document.getElementById("registration-form");
const queueBody = document.getElementById("queue-body");
const formMessage = document.getElementById("form-message");
const queueMessage = document.getElementById("queue-message");
const searchInput = document.getElementById("queue-search");
const statusFilter = document.getElementById("status-filter");
const refreshButton = document.getElementById("refresh-queue");
const clearQueueButton = document.getElementById("clear-queue");
const registerButton = document.getElementById("register-button");
let queueTickets = [];

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    const responseText = await response.text();
    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = { message: responseText };
        }
    }

    if (!response.ok) {
        throw new Error(data?.message || `Request failed (${response.status}).`);
    }

    return data;
}

function addCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = value ?? "";
    row.appendChild(cell);
    return cell;
}

function addActionButton(cell, label, action, ticketNumber, style = "") {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.dataset.action = action;
    button.dataset.ticket = ticketNumber;
    button.className = `table-action ${style}`.trim();
    cell.appendChild(button);
}

function statusLabel(status) {
    const labels = {
        WAITING: "Waiting",
        BEING_ATTENDED: "In consultation",
        COMPLETED: "Completed",
        CANCELLED: "Cancelled"
    };
    return labels[status] || status;
}

function updateSummary(tickets) {
    const counts = tickets.reduce((summary, ticket) => {
        if (ticket.status === "WAITING") summary.waiting += 1;
        if (ticket.status === "BEING_ATTENDED") summary.attending += 1;
        if (ticket.status === "COMPLETED") summary.completed += 1;
        return summary;
    }, { waiting: 0, attending: 0, completed: 0 });

    document.getElementById("waiting-count").textContent = counts.waiting;
    document.getElementById("attending-count").textContent = counts.attending;
    document.getElementById("completed-count").textContent = counts.completed;
    document.getElementById("queue-count").textContent =
        `${tickets.length} ${tickets.length === 1 ? "patient" : "patients"}`;
    clearQueueButton.disabled = tickets.length === 0;
}

function filteredTickets() {
    const query = searchInput.value.trim().toLocaleLowerCase();
    const status = statusFilter.value;

    return queueTickets.filter(ticket => {
        const patient = ticket.patient || {};
        const matchesQuery = !query
            || [ticket.ticketNumber, patient.fullName, patient.patientId]
                .some(value => String(value ?? "").toLocaleLowerCase().includes(query));
        return matchesQuery && (status === "all" || ticket.status === status);
    });
}

function renderQueue() {
    queueBody.replaceChildren();
    const tickets = filteredTickets();

    if (!tickets.length) {
        const row = document.createElement("tr");
        const cell = addCell(
            row,
            queueTickets.length ? "No patients match your search." : "Your queue is clear. Register a patient to get started."
        );
        cell.colSpan = 5;
        cell.className = "empty-msg";
        queueBody.appendChild(row);
        return;
    }

    for (const ticket of tickets) {
        const row = document.createElement("tr");
        const patient = ticket.patient || {};
        const ticketCell = addCell(row, ticket.ticketNumber);
        ticketCell.className = "ticket-number";

        const patientCell = addCell(row, patient.fullName);
        patientCell.className = "patient-name";

        const priorityCell = addCell(row, ticket.urgent ? "Emergency" : "Standard");
        priorityCell.className = ticket.urgent ? "priority-cell priority-urgent" : "priority-cell";

        const statusCell = addCell(row, statusLabel(ticket.status));
        statusCell.className = `status-cell status-${String(ticket.status).toLowerCase()}`;

        const actions = document.createElement("td");
        actions.className = "actions-cell";
        if (ticket.status === "WAITING") {
            addActionButton(actions, "Attend", "attend", ticket.ticketNumber, "action-primary");
        } else if (ticket.status === "BEING_ATTENDED") {
            addActionButton(actions, "Complete", "complete", ticket.ticketNumber, "action-primary");
        }
        if (ticket.status !== "CANCELLED" && ticket.status !== "COMPLETED") {
            addActionButton(actions, "Cancel", "cancel", ticket.ticketNumber, "action-muted");
        }
        row.appendChild(actions);
        queueBody.appendChild(row);
    }
}

async function loadQueue() {
    refreshButton.disabled = true;
    queueMessage.textContent = "";
    queueMessage.classList.remove("error");

    try {
        const tickets = await requestJson("/api/queue");
        if (!Array.isArray(tickets)) {
            throw new Error("The queue response was invalid.");
        }
        queueTickets = tickets;
        updateSummary(queueTickets);
        renderQueue();
    } finally {
        refreshButton.disabled = false;
    }
}

function showFormMessage(message, isError = false) {
    formMessage.textContent = message;
    formMessage.classList.toggle("error", isError);
}

registrationForm.addEventListener("submit", async event => {
    event.preventDefault();
    showFormMessage("");
    registerButton.disabled = true;
    registerButton.classList.add("is-loading");

    const formData = new FormData(registrationForm);
    const patient = {
        name: String(formData.get("name")).trim(),
        age: Number(formData.get("age")),
        nrc: String(formData.get("nrc")).trim(),
        patientStatus: formData.get("patientStatus")
    };

    try {
        const ticket = await requestJson("/api/queue", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patient)
        });

        registrationForm.reset();
        showFormMessage(`Patient registered. Ticket ${ticket.ticketNumber} is ready.`);
        try {
            await loadQueue();
        } catch (error) {
            queueMessage.textContent = `Registered as ${ticket.ticketNumber}, but the queue could not refresh: ${error.message}`;
            queueMessage.classList.add("error");
        }
    } catch (error) {
        showFormMessage(error.message, true);
    } finally {
        registerButton.disabled = false;
        registerButton.classList.remove("is-loading");
    }
});

queueBody.addEventListener("click", async event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;

    button.disabled = true;
    queueMessage.textContent = "";
    queueMessage.classList.remove("error");
    try {
        const ticketNumber = encodeURIComponent(button.dataset.ticket);
        const action = button.dataset.action;
        if (action === "cancel") {
            await requestJson(`/api/queue/${ticketNumber}`, { method: "DELETE" });
        } else {
            const status = action === "attend" ? "BEING_ATTENDED" : "COMPLETED";
            await requestJson(
                `/api/queue/${ticketNumber}/status?status=${status}`,
                { method: "PUT" }
            );
        }
        await loadQueue();
    } catch (error) {
        button.disabled = false;
        queueMessage.textContent = error.message;
        queueMessage.classList.add("error");
    }
});

searchInput.addEventListener("input", renderQueue);
statusFilter.addEventListener("change", renderQueue);
refreshButton.addEventListener("click", () => {
    loadQueue().catch(error => {
        queueMessage.textContent = error.message;
        queueMessage.classList.add("error");
    });
});

clearQueueButton.addEventListener("click", async () => {
    if (!window.confirm("Clear the entire queue? This will remove all tickets.")) {
        return;
    }

    clearQueueButton.disabled = true;
    queueMessage.textContent = "";
    queueMessage.classList.remove("error");
    try {
        await requestJson("/api/queue/reset", { method: "POST" });
        await loadQueue();
        queueMessage.textContent = "Queue cleared. The next patient will receive Q001.";
    } catch (error) {
        queueMessage.textContent = error.message;
        queueMessage.classList.add("error");
        clearQueueButton.disabled = queueTickets.length === 0;
    }
});

loadQueue().catch(error => {
    queueMessage.textContent = error.message;
    queueMessage.classList.add("error");
    const row = document.createElement("tr");
    const cell = addCell(row, "Queue unavailable. Please try refreshing.");
    cell.colSpan = 5;
    cell.className = "empty-msg";
    queueBody.replaceChildren(row);
});
