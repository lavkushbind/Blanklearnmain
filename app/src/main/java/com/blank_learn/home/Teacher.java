package com.blank_learn.home;
import java.util.Map;
public class Teacher {
    private String id;
    private String name;
    private double rating;
    private int experience;
    private int max_students_per_slot;
    private String[] available_slots;
    private int[] preferred_standard;
    private Map<String, String[]> current_students;

    public Teacher() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public double getRating() { return rating; }
    public int getExperience() { return experience; }
    public int getMaxStudentsPerSlot() { return max_students_per_slot; }
    public String[] getAvailableSlots() { return available_slots; }
    public int[] getPreferredStandard() { return preferred_standard; }

    public int getCurrentStudentCount(String timeSlot) {
        return (current_students != null && current_students.containsKey(timeSlot))
                ? current_students.get(timeSlot).length : 0;
    }
}
