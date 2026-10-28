package zm.clinic;

public class Patient {

    private String patientId;
    private String fullName;

    public Patient(String patientId, String fullName) {
        setPatientId(patientId);
        setFullName(fullName);
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("Patient ID cannot be empty.");
        }
        this.patientId = patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        this.fullName = fullName;
    }
}
