import java.io.Serializable;
import java.util.HashMap;

public class Guest implements Serializable {
    private String name;
    private HashMap<Character, Integer> bookedRooms;
    private int totalInvoice;

    public Guest(String name) {
        this.name = name;
        bookedRooms = new HashMap<Character, Integer>();
        totalInvoice = 0;
    }

    public void addRoom(char type, int numOfRooms, int roomPrice) {
        int totalRooms = numOfRooms;

        // If this guest already has booked rooms of this type
        // add booked rooms with those to be booked
        if (bookedRooms.get(type) != null) {
            totalRooms = bookedRooms.get(type) + numOfRooms;
        }
        bookedRooms.put(type, totalRooms);
        totalInvoice += roomPrice * numOfRooms;
    }

    public void removeRooms(char type, int roomPrice, int numOfRooms) {
        int numOfBooked = bookedRooms.get(type);

        // If booked rooms is more, subtract the number
        if (numOfBooked > numOfRooms) {
            bookedRooms.put(type, bookedRooms.get(type) - numOfRooms);

            totalInvoice -= bookedRooms.get(type) * roomPrice;
        }
        else {
            // else remove the this type of room entirely
            bookedRooms.remove(type);

            totalInvoice -= numOfRooms * roomPrice;
        }

    }

    public String getName() {
        return name;
    }

    public int getTotalInvoice() {
        return totalInvoice;
    }

    public HashMap<Character, Integer> getBookedRooms() {
        return bookedRooms;
    }
}
