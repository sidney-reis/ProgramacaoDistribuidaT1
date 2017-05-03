/**
 * Created by Sidney on 03-May-17.
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class Jogador {
    public int id;
    public String nome;
    public Partida partidaAtual;

    public Jogador(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}

class Partida {
    public Jogador jogador1;
    public Jogador jogador2;
    public char[][] tabuleiro;
    public int[] quantidadeOrificio;
    public int[] ultimasJogadas; // [ Posição da última jogada | Posição da penúltima jogada ]
    public int estado;

    public Partida(Jogador jogador1, Jogador jogador2) {
        this.jogador1 = jogador1;
        this.jogador2 = jogador2;
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 8; j++) {
                this.tabuleiro[i][j] = '-';
            }
        }
        quantidadeOrificio = new int[]{0, 0, 0, 0, 0};
        ultimasJogadas = new int[]{-1, -1};
        this.estado = 0; // 0: Não há 2 jogadores ainda. / 1: Vez do jogador 1. / 2: vez do jogador 2.
    }
}

public class DownUnder extends UnicastRemoteObject implements DownUnderInterface {
    private final int maxPartidas = 50;
    private ArrayList<Partida> partidas = new ArrayList<>();

    private int jogadoresCount = 0;
    Map<Integer, Jogador> jogadores = new HashMap<Integer, Jogador>();

    private static AtomicInteger idCounter = new AtomicInteger();

    protected DownUnder() throws RemoteException {
    }

    public static int novoIdJogador() {
        return idCounter.getAndIncrement();
    }

    @Override
    public int registraJogador(String jogador) throws RemoteException {
        if(jogadoresCount == (maxPartidas*2)) {
            return -2;
        }

        for(Integer key: jogadores.keySet()) {
            if(jogadores.get(key).nome == jogador) {
                return -1;
            }
        }

        Jogador novoJogador = new Jogador(novoIdJogador(), jogador);
        jogadores.put(novoJogador.id, novoJogador);

        return novoJogador.id;
    }

    @Override
    public int encerraPartida(int idJogador) throws RemoteException {
        try {
            Partida partida = jogadores.get(idJogador).partidaAtual;
            jogadores.remove(partida.jogador1);
            jogadores.remove(partida.jogador2);
            jogadoresCount -= 2;

            partidas.remove(partida);
            return 0;
        }
        catch(Exception e) {
            return -1;
        }
    }

    @Override
    public int temPartida(int idJogador) throws RemoteException {
        //TODO: FAZER TIMEOUT

        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            if(partida.estado == 0) {
                return 0; // Partida não possui 2 jogadores ainda
            }
            else if(partida.jogador1.id == jogador.id) {
                return 1; // Partida existe e jogador é o 1 (inicia e usa as esferas "C")
            }
            else if(partida.jogador2.id == jogador.id) {
                return 2; // Partida existe e jogador é o 1 (segundo a jogar e usa as esferas "E")
            }

            return -1;
        }
        catch(Exception e) {
            return -1;
        }
    }

    @Override
    public int ehMinhaVez(int idJogador) throws RemoteException {
        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            if (partida.estado == 0) {
                return -2; // Partida não possui 2 jogadores ainda
            }
            else if(((partida.jogador1.id == jogador.id) && (partida.estado == 2)) ||
                    ((partida.jogador2.id == jogador.id) && (partida.estado == 1))) {
                return 0; // Não é a vez do jogador
            }
            else if(((partida.jogador1.id == jogador.id) && (partida.estado == 1)) ||
            ((partida.jogador2.id == jogador.id) && (partida.estado == 2))) {
                return 1; // Vez do jogador
            }

            return -1;
        }
        catch(Exception e) {
            return -1;
        }
    }

    @Override
    public String obtemTabuleiro(int idJogador) throws RemoteException {
        Partida partida = jogadores.get(idJogador).partidaAtual;
        StringBuilder topoTabuleiro = new StringBuilder();

        for(int i = 0; i < 5; i++) {
            //TODO: adicionar topo de orifícios na string topoTabuleiro
        }
        for(int i = 0; i < 5; i++) {
            if(i == partida.ultimasJogadas[0] || i == partida.ultimasJogadas[1])
            {
                topoTabuleiro.append("^");
            }
            else {
                topoTabuleiro.append(".");
            }
        }

        return String.valueOf(topoTabuleiro);
    }

    @Override
    public int soltaEsfera(int idJogador, int orificioTorre) throws RemoteException {
        return 0;
    }

    @Override
    public String obtemOponente(int idJogador) throws RemoteException {
        return null;
    }


}
