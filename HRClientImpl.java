import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HRClientImpl extends UnicastRemoteObject implements HRIClient {

    private Thread client = null;

    public HRClientImpl(Thread client) throws RemoteException {
        super();
        this.client = client;
    }

    @Override
    public boolean notify(String msg) throws RemoteException {
        System.out.println(msg);

        client.interrupt();

        return true;
    }
}
