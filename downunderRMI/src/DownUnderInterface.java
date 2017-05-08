/**
 * Created by Sidney on 03-May-17.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownUnderInterface extends Remote {
    int registraJogador(String jogador) throws RemoteException;

    int encerraPartida(int idJogador) throws RemoteException;

    int temPartida(int idJogador) throws RemoteException;

    int ehMinhaVez(int idJogador) throws RemoteException;

    String obtemTabuleiro(int idJogador) throws RemoteException;

    int soltaEsfera(int idJogador, int orificioTorre) throws RemoteException;

    String obtemOponente(int idJogador) throws RemoteException;


    // Métodos de timer:

    void iniciarTimerJogador(int idJogador) throws RemoteException;

    boolean obtemTimeoutOponente(int idJogador) throws RemoteException;

    void iniciarTimerPartida(int idJogador) throws RemoteException;
}
