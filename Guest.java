import java.util.HashMap;

public class Guest {
    private String name;
    private HashMap<Character, Integer> bookedRooms;
    private int totalInvoice;

    public Guest(String name) {
        this.name = name;
        bookedRooms = new HashMap<Character, Integer>();
        totalInvoice = 0;
    }

    public void addRoom(char type, int numOfRooms, int roomPrice) {
        bookedRooms.put(type, numOfRooms);
        totalInvoice += roomPrice * numOfRooms;
    }

    public void removeRooms(char type, int roomPrice, int numOfRooms) {
        totalInvoice -= (bookedRooms.get(type) - numOfRooms) * roomPrice;

        if (bookedRooms.get(type) - numOfRooms == 0) {
            bookedRooms.remove(type);
        }
        else {
            bookedRooms.put(type, bookedRooms.get(type) - numOfRooms);
        }

        totalInvoice -= bookedRooms.get(type) * roomPrice;
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
