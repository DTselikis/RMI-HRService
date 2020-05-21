import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HRClientImpl extends UnicastRemoteObject implements HRIClient {


    protected HRClientImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean notify(String msg) throws RemoteException {
        System.out.println(msg);

        return true;
    }
}
