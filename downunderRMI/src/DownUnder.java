/**
 * Created by Sidney on 03-May-17.
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class Jogador {
    int id;
    String nome;
    Partida partidaAtual;

    Jogador(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}

class Partida {
    Jogador jogador1;
    Jogador jogador2;
    char[][] tabuleiro = new char[5][8];
    int[] quantidadeOrificio; // Quantas peças foram colocadas em cada orifício
    int[] ultimasJogadas; // [ Posição da última jogada | Posição da penúltima jogada ]
    int estado; // 0: Não há 2 jogadores ainda. / 1: Vez do jogador 1. / 2: vez do jogador 2.

    int timerJogador1;
    int timerJogador2;
    boolean timeoutJogador1;
    boolean timeoutJogador2;
    boolean jogador1Jogou;
    boolean jogador2Jogou;
    int timer;

    Partida() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                this.tabuleiro[i][j] = '-';
            }
        }
        quantidadeOrificio = new int[]{0, 0, 0, 0, 0};
        ultimasJogadas = new int[]{-1, -1};
        this.estado = 0;
        this.timerJogador1 = 0;
        this.timerJogador2 = 0;
        this.timeoutJogador1 = false;
        this.timeoutJogador2 = false;
        this.jogador1Jogou = false;
        this.jogador2Jogou = false;
        this.timer = 0;
    }
}

public class DownUnder extends UnicastRemoteObject implements DownUnderInterface {
    private Partida ultimaPartidaCriada;
    private int jogadoresCount = 0;
    private Map<Integer, Jogador> jogadores = new HashMap<Integer, Jogador>();
    private static AtomicInteger idCounter = new AtomicInteger();
    private final Object jogadoresLock = new Object();

    public DownUnder() throws RemoteException {
    }

    private static int novoIdJogador() {
        return idCounter.getAndIncrement();
    }

    @Override
    public void iniciarTimerJogador(int idJogador) throws RemoteException {
        Jogador jogador = jogadores.get(idJogador);
        Partida partida = jogador.partidaAtual;

        final int numeroJogador;
        if (idJogador == partida.jogador1.id) {
            numeroJogador = 1;
        } else if (idJogador == partida.jogador2.id) {
            numeroJogador = 2;
        } else {
            return;
        }

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (numeroJogador == 1) {
                    partida.timerJogador1++;
                    if (partida.jogador1Jogou) {
                        partida.timerJogador1 = 0;
                        partida.jogador1Jogou = false;
                        t.cancel();
                    } else if (partida.timerJogador1 >= 60) {
                        partida.timeoutJogador1 = true;
                        try {
                            encerraPartida(jogador.id);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        t.cancel();
                    }
                } else {
                    partida.timerJogador2++;
                    if (partida.jogador2Jogou) {
                        partida.timerJogador2 = 0;
                        partida.jogador2Jogou = false;
                        t.cancel();
                    } else if (partida.timerJogador2 >= 60) {
                        partida.timeoutJogador2 = true;
                        try {
                            encerraPartida(jogador.id);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        t.cancel();
                    }
                }
            }
        }, 0, 1000);
    }

    @Override
    public boolean obtemTimeoutOponente(int idJogador) throws RemoteException {
        Jogador jogador = jogadores.get(idJogador);
        Partida partida = jogador.partidaAtual;

        if (idJogador == partida.jogador1.id) {
            return partida.timeoutJogador2;
        }

        return partida.timeoutJogador1;
    }

    @Override
    public void iniciarTimerPartida(int idJogador) throws RemoteException {
        Jogador jogador = jogadores.get(idJogador);
        Partida partida = jogador.partidaAtual;

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                partida.timer++;
                if (partida.timer >= 120) {
                    t.cancel();
                }
            }
        }, 0, 1000);
    }

    @Override
    public synchronized int registraJogador(String jogador) throws RemoteException {
        Jogador novoJogador;

        synchronized (jogadoresLock) {
            int maxPartidas = 50;
            if (jogadoresCount == (maxPartidas * 2)) {
                return -2; // Máximo de jogadores atingido
            }

            for (Integer key : jogadores.keySet()) {
                if (jogadores.get(key).nome.equals(jogador)) {
                    return -1; // Nome já existe
                }
            }

            novoJogador = new Jogador(novoIdJogador(), jogador);
            jogadores.put(novoJogador.id, novoJogador);

            if (jogadoresCount % 2 == 0) {
                ultimaPartidaCriada = new Partida();
                ultimaPartidaCriada.jogador1 = novoJogador;
            } else {
                ultimaPartidaCriada.jogador2 = novoJogador;
                ultimaPartidaCriada.estado = 1;
            }
            novoJogador.partidaAtual = ultimaPartidaCriada;

            jogadoresCount++;
            System.out.println("Jogador registrado: ID -> " + novoJogador.id + " | NOME -> " + novoJogador.nome);
        }
        return novoJogador.id;
    }

    @Override
    public int encerraPartida(int idJogador) throws RemoteException {
        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Encerranto partida do jogador " + jogador.nome + ".");

                    synchronized (jogadoresLock) {
                        jogadores.remove(partida.jogador1.id);
                        if (partida.jogador2 == null) {
                            jogadoresCount -= 1;
                        } else {
                            jogadores.remove(partida.jogador2.id);
                            jogadoresCount -= 2;
                        }
                    }
                    t.cancel();
                }
            }, 60000, 1000);

            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int temPartida(int idJogador) throws RemoteException {
        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            if (partida.timer >= 120) {
                return -2;
            }

            if (partida.estado == 0) {
                return 0; // Partida não possui 2 jogadores ainda
            } else if (partida.jogador1.id == jogador.id) {
                return 1; // Partida existe e jogador é o 1 (inicia e usa as esferas "C")
            } else if (partida.jogador2.id == jogador.id) {
                return 2; // Partida existe e jogador é o 2 (segundo a jogar e usa as esferas "E")
            }

            return -1;
        } catch (Exception e) {
            System.out.println("Erro ao tentar realizar o método 'temPartida'.");
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
            } else if (((partida.jogador1.id == jogador.id) && (partida.estado == 2)) ||
                    ((partida.jogador2.id == jogador.id) && (partida.estado == 1))) {
                return 0; // Não é a vez do jogador
            } else if (((partida.jogador1.id == jogador.id) && (partida.estado == 1)) ||
                    ((partida.jogador2.id == jogador.id) && (partida.estado == 2))) {
                return 1; // Vez do jogador
            }

            return -1;
        } catch (Exception e) {
            System.out.println("Erro ao tentar realizar o método 'ehMinhaVez'.");
            return -1;
        }
    }

    @Override
    public String obtemTabuleiro(int idJogador) throws RemoteException {
        Partida partida = jogadores.get(idJogador).partidaAtual;
        StringBuilder topoTabuleiro = new StringBuilder();

        if ((partida.quantidadeOrificio[0] == 8) &&
                (partida.quantidadeOrificio[1] == 8) &&
                (partida.quantidadeOrificio[2] == 8) &&
                (partida.quantidadeOrificio[3] == 8) &&
                (partida.quantidadeOrificio[4] == 8)) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 8; j++) {
                    topoTabuleiro.append(partida.tabuleiro[i][j]);
                }
                topoTabuleiro.append("\n");
            }

            int pontosP1 = 0;
            int pontosP2 = 0;
            int contadorPecas = 0;
            char pecaAtual;

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 8; j++) {
                    pecaAtual = partida.tabuleiro[i][j];

                    while (contadorPecas < 3) {
                        if (((i + contadorPecas + 1) < 5) && (pecaAtual == partida.tabuleiro[i + contadorPecas + 1][j])) { // Checa se há 3 peças iguais abaixo
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 3) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        } else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 3) {
                        if (((j + contadorPecas + 1) < 8) && (pecaAtual == partida.tabuleiro[i][j + contadorPecas + 1])) { // Checa se há 3 peças iguais na direita
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 3) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        } else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 3) {
                        if (((i + contadorPecas + 1) < 5) && ((j + contadorPecas + 1) < 8) && (pecaAtual == partida.tabuleiro[i + contadorPecas + 1][j + contadorPecas + 1])) { // Checa se há 3 peças iguais na diagonal \
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 3) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        } else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 3) {
                        if (((i - contadorPecas - 1) > -1) && ((j + contadorPecas + 1) < 8) && (pecaAtual == partida.tabuleiro[i - contadorPecas - 1][j + contadorPecas + 1])) { // Checa se há 3 peças iguais na diagonal /
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 3) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        } else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;
                }
            }

            topoTabuleiro.append("Pontos do jogador ").append(partida.jogador1.nome).append(" : ").append(pontosP1).append("\n").append("Pontos do jogador ").append(partida.jogador2.nome).append(" : ").append(pontosP2).append("\n");

            return String.valueOf(topoTabuleiro);
        }

        for (int i = 0; i < 5; i++) {
            if (partida.quantidadeOrificio[i] == 8) {
                topoTabuleiro.append(partida.tabuleiro[i][0]);
            } else {
                topoTabuleiro.append("-");
            }
        }
        for (int i = 0; i < 5; i++) {
            if (i == partida.ultimasJogadas[0] || i == partida.ultimasJogadas[1]) {
                topoTabuleiro.append("^");
            } else {
                topoTabuleiro.append(".");
            }
        }

        return String.valueOf(topoTabuleiro);
    }

    @Override
    public int soltaEsfera(int idJogador, int orificioTorre) throws RemoteException {
        Jogador jogador = jogadores.get(idJogador);
        if (jogador == null) {
            return 2; // Partida foi encerrada por timeout, por tanto o jogador não existe mais
        }
        Partida partida = jogador.partidaAtual;

        if (((partida.jogador1.id == jogador.id) && (partida.estado == 2)) ||
                ((partida.jogador2.id == jogador.id) && (partida.estado == 1))) {
            return -3; // Não é a vez do jogador
        } else if (partida.estado == 0) {
            return -2; // Não há dois jogadores na partida ainda
        } else if ((orificioTorre != 0) &&
                (orificioTorre != 1) &&
                (orificioTorre != 2) &&
                (orificioTorre != 3) &&
                (orificioTorre != 4)) {
            return -1; // Número inválido de orifício
        } else if (partida.quantidadeOrificio[orificioTorre] == 8) {
            return 0; // Movimento inválido: orifício já foi preenchido
        }

        if (partida.jogador1.id == jogador.id) {
            partida.jogador1Jogou = true;
        } else if (partida.jogador2.id == jogador.id) {
            partida.jogador2Jogou = true;
        }

        char peca = 'C'; // Claras
        if (partida.jogador2.id == jogador.id) {
            peca = 'E'; // Escuras
        }

        partida.tabuleiro[orificioTorre][7 - partida.quantidadeOrificio[orificioTorre]] = peca;
        partida.quantidadeOrificio[orificioTorre]++;
        partida.ultimasJogadas[1] = partida.ultimasJogadas[0];
        partida.ultimasJogadas[0] = orificioTorre;

        if (partida.estado == 2) {
            partida.estado = 1;
        } else if (partida.estado == 1) {
            partida.estado = 2;
        }
        return 1; // Movimento realizado
    }

    @Override
    public String obtemOponente(int idJogador) throws RemoteException {
        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            if (partida.estado == 0) {
                return ""; // Erro: partida não possuí 2 jogadores ainda
            }

            if (partida.jogador1.id == jogador.id) {
                return partida.jogador2.nome;
            }
            return partida.jogador1.nome;
        } catch (Exception e) {
            System.out.println("Erro ao tentar realizar o método 'obtemOponente'.");
            return "";
        }
    }
}
