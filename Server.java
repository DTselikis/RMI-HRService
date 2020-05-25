import javax.sound.midi.Soundbank;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Server <host>");
            System.exit(1);
        }

        try {
            startRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();

            System.out.println("RMI server bind failed.\n");
            System.exit(2);
        }

        HRServerImpl remoteServer = null;
        try {
            remoteServer = new HRServerImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        StringBuilder rmiRegS = new StringBuilder();
        rmiRegS.append("rmi://").append(args[0]).append(":1099/").append("HRService");
        try {
            Naming.rebind(rmiRegS.toString(), remoteServer);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println("Remote server up and running!");
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list( );
            // This call will throw an exception
            // if the registry does not already exist
        }
        catch (RemoteException e) {
            // No valid registry at that port.
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
        }
    } // end s
}
