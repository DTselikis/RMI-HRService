import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client implements Runnable {
    private String[] args;
    private Thread me = null;

    public Client(String[] args) {
        this.args = args;
    }

    public void addThread (Thread me) {
        this.me = me;
    }

    public void run() {
        String hostname = "";
        char type = 'A';
        int numOfRooms = 0;
        String name = "";
        HRClientImpl callback = null;
        ArrayList<Character> roomsNotify = new ArrayList<>();

        StringBuilder msg = new StringBuilder();
        if (args.length < 1) {
            msg.append("Usage:").append("java Client list <hostname>").append(System.lineSeparator());
            msg.append("java Client book <hostname> <type> <number> <name>").append(System.lineSeparator());
            msg.append("java Client guests <hostname>").append(System.lineSeparator());
            msg.append("java Client cancel <hostname> <type> <number> <name>").append(System.lineSeparator());
            System.out.println(msg.toString());

            System.exit(1);
        }
        else {
            switch (args[0]) {
                // Cases "book" and "cancel" has the same parameters with "guests" and "list" plus three more
                case "book":
                case "cancel": {
                    try {
                        type = args[2].charAt(0);
                        numOfRooms = Integer.parseInt(args[3]);
                        name = args[4];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        if (args[0].equals("book")) {
                            System.out.println("Usage: java HRClient book <hostname> <type> <number> <name>");
                            System.out.println(System.lineSeparator());
                        }
                        else {
                            System.out.println("Usage: java HRClient cancel <hostname> <type> <number> <name>");
                            System.out.println(System.lineSeparator());
                        }

                        System.exit(1);
                    }
                }
                // Cases "book" and "cancel" has the same parameters
                case "guests":
                case "list": {
                    try {
                        hostname = args[1];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        if (args[0].equals("list")) {
                            System.out.println("Usage: java HRClient list <hostname>");
                            System.out.println(System.lineSeparator());
                        }
                        else {
                            System.out.println("Usage: java HRClient guests <hostname>");
                            System.out.println(System.lineSeparator());
                        }

                        System.exit(1);
                    }
                    break;
                }
                default: {
                    StringBuilder usgMsg = new StringBuilder();
                    usgMsg.append("Usage:").append("java HRClient list <hostname>").append(System.lineSeparator());
                    usgMsg.append("java HRClient book <hostname> <type> <number> <name>").append(System.lineSeparator());
                    usgMsg.append("java HRClient guests <hostname>").append(System.lineSeparator());
                    usgMsg.append("java HRClient cancel <hostname> <type> <number> <name>").append(System.lineSeparator());
                    System.out.println(usgMsg.toString());

                    System.exit(1);
                }
            }
        }

        msg.append("rmi://").append(hostname).append(":1099").append("/HRService");
        HRIServer remoteServer = null;
        try {
            remoteServer  = (HRIServer) Naming.lookup(msg.toString());

            switch (args[0]) {
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
                            System.out.println("All rooms booked with total cost of: " + response.get(1) + "€");
                            System.out.println(System.lineSeparator());

                            roomsNotify.remove(Character.valueOf(type));

                            choice = "n";
                        }
                        else if (response.get(0) > 0) {
                            // If the exact number of rooms is not available, notify the user
                            // and if he wants, try again to book with the available rooms
                            System.out.println("There are " + response.get(0) + " available rooms at the moment.");
                            System.out.println(System.lineSeparator());
                            System.out.println("Do you want to book them?");
                            System.out.println(System.lineSeparator());
                            System.out.print("Choice (y/n): ");
                            choice = input.nextLine();

                            if(choice.equals("y")) {
                                numOfRooms = response.get(0);
                            }
                        }
                        else {
                            System.out.println("No available rooms at the moment");
                            choice = "n";
                        }
                    }

                    // If there was not enough rooms and guest is not already in the waiting list
                    if (response.get(0) != numOfRooms && !roomsNotify.contains(Character.valueOf(type))) {
                        System.out.println();
                        System.out.println("Do you want to be notified when more rooms are available?");
                        System.out.println();
                        System.out.print("Choice (y/n): ");
                        choice = input.nextLine();

                        if(choice.equals("y")) {
                            System.out.print("How much time do you want to wait? (seconds): ");
                            int waitFor = Integer.parseInt(input.nextLine());

                            System.out.println();
                            if (callback == null) {
                                callback = new HRClientImpl(me);
                            }

                            roomsNotify.add(Character.valueOf(type));
                            remoteServer.registerForNotification(callback, type);
                            System.out.println("You will be notified.");
                            System.out.println();

                            try {
                                Thread.sleep(waitFor * 1000);
                            } catch (InterruptedException e) {
                                // User have been notified so remove his from
                                // waiting list of this type of rooms
                                roomsNotify.remove(Character.valueOf(type));
                            }

                            remoteServer.unregisterForNotification(callback, type);

                            // To exit after sleep
                            System.exit(0);
                        }
                    }
                    break;
                }
                case "guests": {
                    ArrayList<Guest> guests;
                    guests = remoteServer.guests();

                    StringBuilder guestsMsg = new StringBuilder();
                    if (guests.size() > 0) {
                        for (Guest guest: guests) {
                            guestsMsg.append("Guest: \"").append(guest.getName()).append("\"").append(System.lineSeparator());
                            guestsMsg.append("Rooms:").append(System.lineSeparator());
                            for (Map.Entry<Character, Integer> room: guest.getBookedRooms().entrySet()) {
                                guestsMsg.append("\t").append(room.getValue()).append(" rooms of type ").append(room.getKey());
                                guestsMsg.append(System.lineSeparator());
                            }
                            guestsMsg.append("Total invoice: ").append(guest.getTotalInvoice()).append("€").append(System.lineSeparator());
                            guestsMsg.append(System.lineSeparator());
                        }
                    }
                    else {
                        guestsMsg.append("No guests at the current time!").append(System.lineSeparator());
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
                        System.out.println("No rooms of type " + type + " to cancel.");
                        System.out.println(System.lineSeparator());
                    }
                }
            }
        } catch (NotBoundException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.");
            System.out.println();

            System.exit(2);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.");
            System.out.println();

            System.exit(2);
        } catch (RemoteException e) {
            e.printStackTrace();

            System.out.println("Failed to find server.");
            System.out.println();

            System.exit(2);
        }
    }

    public static void main(String[] args) {
        // Create a thread to be able to sleep and interrupt

        Client client = new Client(args);
        Thread threadClnt = new Thread(client);
        client.addThread(threadClnt);
        threadClnt.start();
    }
}
