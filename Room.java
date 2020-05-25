import java.util.HashMap;

public class Room {
    private char type;
    private int availability;
    private int price;
    private String description;
    private HashMap<String, Integer> bookedRooms;

    public Room(char type, int availability, int price, String description) {
        this.type = type;
        this.availability = availability;
        this.price = price;
        this.description = description;
        bookedRooms = new HashMap<String, Integer>();
    }

    public char getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getAvailability() {
        return availability;
    }

    public int checkBooked(String name) {
        if (bookedRooms.get(name) != null) {
            return bookedRooms.get(name);
        }
        return 0;
    }

    public int book(String name, int numOfRooms) {
        if (availability >= numOfRooms) {
            bookedRooms.put(name, numOfRooms);
        }
        else {
            if ((numOfRooms = availability) != 0) {
                bookedRooms.put(name, numOfRooms);
            }
        }
        availability -= numOfRooms;

        return numOfRooms;

    }

    public int cancel(String name, int numOfRooms) {
        int remainingRooms = 0;
        if (bookedRooms.get(name) > numOfRooms) {
            bookedRooms.put(name, bookedRooms.get(name) - numOfRooms);

            remainingRooms = bookedRooms.get(name);
        }
        else {
            numOfRooms = bookedRooms.get(name);
            bookedRooms.remove(name);
        }

        availability += numOfRooms;

        return remainingRooms;
    }
}
