/**
 * Created by Sidney on 03-May-17.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownUnderInterface extends Remote {
    public int registraJogador(String jogador) throws RemoteException;
    public int encerraPartida(int idJogador) throws RemoteException;
    public int temPartida(int idJogador) throws RemoteException;
    public int ehMinhaVez(int idJogador) throws RemoteException;
    public String obtemTabuleiro(int idJogador) throws RemoteException;
    public int soltaEsfera(int idJogador, int orificioTorre) throws RemoteException;
    public String obtemOponente(int idJogador) throws RemoteException;
}
