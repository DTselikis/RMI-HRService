import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostname = "";
        char type = 'A';
        int numOfRooms = 0;
        String name = "";
        HRClientImpl callback = null;
        ArrayList<Character> roomsNotify = new ArrayList<>();

        StringBuilder msg = new StringBuilder();
        if (args.length < 2) {
            msg.append("Usage:").append("java HRClient list <hostname>").append(System.lineSeparator());
            msg.append("java HRClient book <hostname> <type> <number> <name>").append(System.lineSeparator());
            msg.append("java HRClient guests <hostname>").append(System.lineSeparator());
            msg.append("java HRClient cancel <hostname> <type> <number> <name>").append(System.lineSeparator());
            System.out.println(msg.toString());

            System.exit(1);
        }
        else {
            switch (args[2]) {
                case "book":
                case "cancel": {
                    try {
                        type = args[3].charAt(0);
                        numOfRooms = Integer.parseInt(args[4]);
                        name = args[5];
                    } catch (NullPointerException ex) {
                        if (args[2].equals("book")) {
                            System.out.println("Usage: java HRClient book <hostname> <type> <number> <name>\n");
                        }
                        else {
                            System.out.println("Usage: java HRClient cancel <hostname> <type> <number> <name>\n");
                        }

                        System.exit(1);
                    }
                }
                case "guests":
                case "list": {
                    try {
                        hostname = args[2];
                    } catch (NullPointerException ex) {
                        if (args[2].equals("list")) {
                            System.out.println("Usage: java HRClient list <hostname>\n");
                        }
                        else {
                            System.out.println("Usage: java HRClient guests <hostname>\n");
                        }

                        System.exit(1);
                    }
                }
            }
        }

        msg.append("rmi://").append(hostname).append(":").append("/HRService");
        HRIServer remoteServer = null;
        try {
            remoteServer  = (HRIServer) Naming.lookup(msg.toString());

            switch (args[2]) {
                case "list": {
                    HashMap<Character, ArrayList<Integer>> roomsList;
                    roomsList = remoteServer.list();

                    StringBuilder listMsg = new StringBuilder();
                    for (Map.Entry<Character, ArrayList<Integer>> room: roomsList.entrySet()) {
                        listMsg.append(room.getValue().get(0)).append(" available rooms of type ").append(room.getKey());
                        listMsg.append(" for ").append(room.getValue().get(1)).append("€ per night").append(System.lineSeparator());
                    }

                    System.out.println(listMsg.toString());

                    break;
                }
                case "book": {
                    ArrayList<Integer> response = null;
                    String choice = "y";
                    Scanner input = new Scanner(System.in);

                    while (choice.equals("y")) {
                        response = remoteServer.book(name, numOfRooms, type);

                        if (response.get(0) == numOfRooms) {
                            System.out.println("All rooms booked with total cost of :" + response.get(1) + "€\n");

                            roomsNotify.remove(Character.valueOf(type));
                        }
                        else if (response.get(0) > 0) {
                            System.out.println("There are " + response.get(0) + "available rooms at the moment.\n");
                            System.out.println("Do you want to book them?\n");
                            System.out.println("Choice (y/n): ");
                            choice = input.nextLine();

                            if(choice.equals("y")) {
                                numOfRooms = response.get(0);
                            }
                        }
                        else {
                            choice = "y";
                        }
                    }

                    // possible BUG
                    if (response.get(0) != numOfRooms && !roomsNotify.contains(Character.valueOf(type))) {
                        System.out.println("Do you want to be notified when more rooms are available?\n");
                        System.out.println("Choice (y/n): ");
                        choice = input.nextLine();

                        if(choice.equals("y")) {
                            if (callback == null) {
                                callback = new HRClientImpl();
                            }

                            roomsNotify.add(Character.valueOf(type));
                            remoteServer.registerForNotification(callback, type);
                            System.out.println("You will be notified.\n");
                        }
                    }
                    break;
                }
                case "guests": {
                    ArrayList<Guest> guests;
                    guests = remoteServer.guests();

                    StringBuilder guestsMsg = new StringBuilder();
                    for (Guest guest: guests) {
                        guestsMsg.append("Guest: \"").append(guest.getName()).append("\"").append(System.lineSeparator());
                        guestsMsg.append("Rooms:");
                        for (Map.Entry<Character, Integer> room: guest.getBookedRooms().entrySet()) {
                            guestsMsg.append("\t").append(room.getValue()).append(" rooms of type ").append(room.getValue());
                            guestsMsg.append(System.lineSeparator());
                        }
                        guestsMsg.append("Total invoice: ").append(guest.getTotalInvoice()).append("€").append(System.lineSeparator());
                    }
                    System.out.println(guestsMsg.toString());

                    break;
                }
                case "cancel": {
                    HashMap<Character, Integer> bookedRooms;
                    bookedRooms = remoteServer.cancel(name, numOfRooms, type);

                    if (bookedRooms != null) {
                        StringBuilder roomsMsg = new StringBuilder();

                        roomsMsg.append("Remaining rooms:").append(System.lineSeparator());
                        for (Map.Entry<Character, Integer> room: bookedRooms.entrySet()) {
                            roomsMsg.append(room.getValue()).append(" rooms of type ").append(room.getKey());
                            roomsMsg.append(System.lineSeparator());
                        }

                        System.out.println(roomsMsg.toString());
                    }
                    else {
                        System.out.println("No rooms of type " + type + " to cancel.\n");
                    }
                }
            }
        } catch (NotBoundException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.\n");
            System.exit(2);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.\n");
            System.exit(2);
        } catch (RemoteException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.\n");
            System.exit(2);
        }
    }
}
