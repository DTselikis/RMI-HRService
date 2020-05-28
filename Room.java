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
        // Check if the guest with this name has booked any rooms of this type
        if (bookedRooms.get(name) != null) {
            return bookedRooms.get(name);
        }
        return 0;
    }

    public int book(String name, int numOfRooms) {
        int totalRooms = numOfRooms;

        // If the guest with this name has booked any rooms of this type
        // then add the booked rooms with those to be booked
        if (bookedRooms.get(name) != null) {
            totalRooms = bookedRooms.get(name) + numOfRooms;
        }

        // If there are enough rooms, book all of user wants
        if (availability >= numOfRooms) {
            bookedRooms.put(name, totalRooms);
        }
        else {
            // else book only the available rooms
            if ((numOfRooms = availability) != 0) {
                bookedRooms.put(name, totalRooms);
            }
        }
        availability -= numOfRooms;

        return numOfRooms;

    }

    public int cancel(String name, int numOfRooms) {
        int remainingRooms = 0;

        // If guest has more rooms than he wants to cancel, subtract them
        // from the total number
        if (bookedRooms.get(name) > numOfRooms) {
            bookedRooms.put(name, bookedRooms.get(name) - numOfRooms);

            remainingRooms = bookedRooms.get(name);
        }
        else {
            // else remove this guest entirely from the list
            numOfRooms = bookedRooms.get(name);
            bookedRooms.remove(name);
        }

        availability += numOfRooms;

        return remainingRooms;
    }
}
