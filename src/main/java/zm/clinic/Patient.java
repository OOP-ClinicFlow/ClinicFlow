package zm.clinic;

public class Patient {

    private String patientId;
    private String fullName;
    private Integer age;

    public Patient(String patientId, String fullName) {
        this(patientId, fullName, null);
    }

    public Patient(String patientId, String fullName, Integer age) {
        setPatientId(patientId);
        setFullName(fullName);
        setAge(age);
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        if (age != null && age < 1) {
            throw new IllegalArgumentException("Patient age must be greater than zero.");
        }
        this.age = age;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        this.fullName = fullName;
    }
}
