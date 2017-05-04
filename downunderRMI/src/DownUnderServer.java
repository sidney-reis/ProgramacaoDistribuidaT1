/**
 * Created by Sidney on 03-May-17.
 */
import java.rmi.Naming;
import java.rmi.RemoteException;

public class DownUnderServer {
    public static void main (String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry ready.");
        } catch (RemoteException e) {
            System.out.println("RMI registry already running.");
        }
        try {
            Naming.rebind("DownUnder", new DownUnder());
            System.out.println("DownUnderServer is ready.");
        } catch (Exception e) {
            System.out.println("DownUnderServer failed:");
            e.printStackTrace();
        }
    }
}
