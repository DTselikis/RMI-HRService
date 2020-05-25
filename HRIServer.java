import java.util.ArrayList;
import java.util.HashMap;

public interface HRIServer extends java.rmi.Remote {
    public  HashMap<Character, ArrayList<Integer>> list() throws java.rmi.RemoteException;
    public ArrayList<Integer> book(String name, int numOfRooms, char roomType) throws java.rmi.RemoteException;
    public ArrayList<Guest> guests() throws java.rmi.RemoteException;
    public HashMap<Character, Integer> cancel(String name, int numOfRooms, char roomType) throws java.rmi.RemoteException;
    public boolean registerForNotification(HRIClient client, char type) throws java.rmi.RemoteException;


}
