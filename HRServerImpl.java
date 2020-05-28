import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * RMI Server implementation
 * Contains the data of the hotel and is responsible for the basic operations,
 * each implemented in each own method.
 *
 * @author Tselikis Dimitrios
 * @version 1.0
 */
public class HRServerImpl extends UnicastRemoteObject implements HRIServer {
    private HashMap<Character, Room> rooms;
    private HashMap<String, Guest> guests;
    private HashMap<Character, ArrayList<HRIClient>> notifyList;

    /**
     * Constructor
     * Creates the needed objects and add some rooms.
     *
     * @throws RemoteException
     * @since 1.0
     */
    public HRServerImpl() throws RemoteException {
        super();
        rooms = new HashMap<>();
        guests = new HashMap<>();
        notifyList = new HashMap<>();

        rooms.put('A', new Room('A', 30, 50, "Single Room"));
        rooms.put('B', new Room('B', 45, 70, "Double Room"));
        rooms.put('C', new Room('C', 25, 80, "Twin Room"));
        rooms.put('D', new Room('D', 10, 120, "Triple Room"));
        rooms.put('E', new Room('E', 5, 150, "Quad Room"));
    }

    /**
     * @return a HashMap containing the availability and price for each room
     * @throws RemoteException
     * @since 1.0
     */
    @Override
    public HashMap<Character, ArrayList<Integer>> list() throws RemoteException {
        HashMap<Character, ArrayList<Integer>> availableRooms = new HashMap<Character, ArrayList<Integer>>();

        for (Room room: rooms.values()) {
            availableRooms.put(room.getType(), new ArrayList<Integer>());
            availableRooms.get(room.getType()).add(room.getAvailability());
            availableRooms.get(room.getType()).add(room.getPrice());
        }

        return availableRooms;

    }

    /**
     * <p>
     * The method will return either the number of rooms that was booked or the number of available rooms
     * @param name The name of the user
     * @param numOfRooms The number of rooms to be booked
     * @param roomType Type of rooms to be booked
     * @return An ArrayList containing the number of booked rooms and the total price
     * @throws RemoteException
     * @since 1.0
     */
    @Override
    public ArrayList<Integer> book(String name, int numOfRooms, char roomType) throws RemoteException {
        Guest guest;
        // If this is the first booking of this guest, create a new instance
        // and associate the name with the object
        if ((guest = guests.get(name)) == null) {
            guest = new Guest(name);
            guests.put(name, guest);
        }

        ArrayList<Integer> booked = new ArrayList<>();
        int bookedRooms = 0;
        // If the availability is less than what user wants, store the number
        // of available rooms to inform the user
        if (rooms.get((roomType)).getAvailability() < numOfRooms) {
            booked.add(rooms.get(roomType).getAvailability());
        }
        else {
            // If there are enough available rooms, book them
            bookedRooms = rooms.get(roomType).book(name, numOfRooms);

            if (bookedRooms > 0) {
                // and also inform the user object about this booking
                guest.addRoom(roomType, numOfRooms, rooms.get(roomType).getPrice());
            }
        }

        booked.add(bookedRooms);
        booked.add(bookedRooms * rooms.get(roomType).getPrice());

        return booked;
    }

    /**
     * <p>
     * Creates a list of all current guests that have interacted with the system until now
     * @return An ArrayList contained all the guest objects (all customers)
     * @throws RemoteException
     * @since 1.0
     */
    @Override
    public ArrayList<Guest> guests() throws RemoteException {
        ArrayList<Guest> guests = new ArrayList<>();

        for (Guest guest: this.guests.values()) {
            guests.add(guest);
        }

        return guests;
    }

    /**
     *
     * @param name Name of the guest who performs the cancellation
     * @param numOfRooms Number of rooms to be canceled
     * @param roomType Room type to be canceled
     * @return A HashMap containing the rest of this guests reservations
     * @throws RemoteException
     * @since 1.0
     */
    @Override
    public HashMap<Character, Integer> cancel(String name, int numOfRooms, char roomType) throws RemoteException {
        int bookedRooms;

        // If the guest have booked at least one room of this type
        if ((bookedRooms = rooms.get(roomType).checkBooked(name)) > 0) {
            // If user wants to cancel more rooms than he has booked, change the
            // number of rooms to be canceled to the number of his booked rooms
            if (bookedRooms < numOfRooms) {
                numOfRooms = bookedRooms;
            }

            int remainingRooms = rooms.get(roomType).cancel(name, numOfRooms);
            guests.get(name).removeRooms(roomType, rooms.get(roomType).getPrice(), numOfRooms);

            StringBuilder msg = new StringBuilder();
            msg.append(numOfRooms).append(" rooms of type ").append(roomType).append(" is available!");

            // Notify guests who waits for availability for this type of rooms
            if (notifyList.size() > 0) {
                for (HRIClient client: notifyList.get(roomType)) {
                    client.notify(msg.toString());
                }
            }

            return guests.get(name).getBookedRooms();
        }

        return null;
    }

    /**
     *
     * @param client A callback object associated to a specific guest
     * @param type The type of room that guest wants to be notified
     * @return True if operation was successful
     * @since 1.0
     */
    @Override
    public boolean registerForNotification(HRIClient client, char type) {
        // Create a notify list for this type of room, if there is not one
        if (notifyList.get(type) == null) {
            notifyList.put(type, new ArrayList<>());
        }
        notifyList.get(type).add(client);

        return true;
    }

    /**
     *
     * @param client A callback object associated to a specific guest
     * @param type The type of room that guest wants to be notified
     * @return True if operation was successful
     * @throws RemoteException
     * @since 1.0
     */
    @Override
    public boolean unregisterForNotification(HRIClient client, char type) throws RemoteException {
        // Remove guest from notify list if he was already registered
        if (notifyList.get(type) != null) {
            notifyList.get(type).remove(client);
        }

        return true;
    }
}
