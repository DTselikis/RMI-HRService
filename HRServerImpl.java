import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class HRServerImpl extends UnicastRemoteObject implements HRIServer {
    private HashMap<Character, Room> rooms;
    private HashMap<String, Guest> guests;
    private HashMap<Character, ArrayList<HRIClient>> notifyList;

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

    @Override
    public ArrayList<Integer> book(String name, int numOfRooms, char roomType) throws RemoteException {
        Guest guest;
        if ((guest = guests.get(name)) == null) {
            guest = new Guest(name);
            guests.put(name, guest);
        }

        ArrayList<Integer> booked = new ArrayList<>();
        int bookedRooms = 0;
        if (rooms.get((roomType)).getAvailability() < numOfRooms) {
            booked.add(rooms.get(roomType).getAvailability());
        }
        else {
            bookedRooms = rooms.get(roomType).book(name, numOfRooms);

            if (bookedRooms > 0) {
                guest.addRoom(roomType, numOfRooms, rooms.get(roomType).getPrice());
            }
        }

        booked.add(bookedRooms);
        booked.add(bookedRooms * rooms.get(roomType).getPrice());

        return booked;
    }

    @Override
    public ArrayList<Guest> guests() throws RemoteException {
        ArrayList<Guest> guests = new ArrayList<>();

        for (Guest guest: this.guests.values()) {
            guests.add(guest);
        }

        return guests;
    }

    @Override
    public HashMap<Character, Integer> cancel(String name, int numOfRooms, char roomType) throws RemoteException {
        int bookedRooms;

        if ((bookedRooms = rooms.get(roomType).checkBooked(name)) > 0) {
            if (bookedRooms < numOfRooms) {
                numOfRooms = bookedRooms;
            }

            int remainingRooms = rooms.get(roomType).cancel(name, numOfRooms);
            guests.get(name).removeRooms(roomType, rooms.get(roomType).getPrice(), numOfRooms);

            StringBuilder msg = new StringBuilder();
            msg.append(numOfRooms).append(" rooms of type ").append(roomType).append(" is available!");

            if (notifyList.size() > 0) {
                for (HRIClient client: notifyList.get(roomType)) {
                    client.notify(msg.toString());
                }
            }

            return guests.get(name).getBookedRooms();
        }

        return null;
    }

    @Override
    public boolean registerForNotification(HRIClient client, char type) {
        if (notifyList.get(type) == null) {
            notifyList.put(type, new ArrayList<>());
        }
        notifyList.get(type).add(client);

        return true;
    }

    @Override
    public boolean unregisterForNotification(HRIClient client, char type) throws RemoteException {
        if (notifyList.get(type) != null) {
            notifyList.get(type).remove(client);
        }

        return true;
    }
}
