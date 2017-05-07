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

    public Partida() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                this.tabuleiro[i][j] = '-';
            }
        }
        quantidadeOrificio = new int[]{0, 0, 0, 0, 0};
        ultimasJogadas = new int[]{-1, -1};
        this.estado = 0;
    }
}

public class DownUnder extends UnicastRemoteObject implements DownUnderInterface {
    private ArrayList<Partida> partidas = new ArrayList<>();
    private Partida ultimaPartidaCriada;
    private int jogadoresCount = 0;
    private Map<Integer, Jogador> jogadores = new HashMap<Integer, Jogador>();

    private static AtomicInteger idCounter = new AtomicInteger();

    public DownUnder() throws RemoteException {
    }

    //TODO: rever métodos para incluir timeout e synchronized

    private static int novoIdJogador() {
        return idCounter.getAndIncrement();
    }

    @Override
    public int registraJogador(String jogador) throws RemoteException {
        int maxPartidas = 50;
        if (jogadoresCount == (maxPartidas * 2)) {
            return -2; // Máximo de jogadores atingido
        }

        for (Integer key : jogadores.keySet()) {
            if (jogadores.get(key).nome.equals(jogador)) {
                return -1; // Nome já existe
            }
        }

        Jogador novoJogador = new Jogador(novoIdJogador(), jogador);
        jogadores.put(novoJogador.id, novoJogador);

        if(jogadoresCount % 2 == 0) {
            ultimaPartidaCriada = new Partida();
            ultimaPartidaCriada.jogador1 = novoJogador;
            partidas.add(ultimaPartidaCriada);
        } else {
            ultimaPartidaCriada.jogador2 = novoJogador;
            ultimaPartidaCriada.estado = 1;
        }
        novoJogador.partidaAtual = ultimaPartidaCriada;

        jogadoresCount++;
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
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int temPartida(int idJogador) throws RemoteException {
        //TODO: FAZER TIMEOUT

        try {
            Jogador jogador = jogadores.get(idJogador);
            Partida partida = jogador.partidaAtual;

            if (partida.estado == 0) {
                return 0; // Partida não possui 2 jogadores ainda
            } else if (partida.jogador1.id == jogador.id) {
                return 1; // Partida existe e jogador é o 1 (inicia e usa as esferas "C")
            } else if (partida.jogador2.id == jogador.id) {
                return 2; // Partida existe e jogador é o 2 (segundo a jogar e usa as esferas "E")
            }

            return -1;
        } catch (Exception e) {
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

                    while (contadorPecas < 4) {
                        if (((i+contadorPecas+1) < 5) && (pecaAtual == partida.tabuleiro[i + contadorPecas + 1][j])) {
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 4) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        }
                        else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 4) {
                        if (((j+contadorPecas+1) < 8) && (pecaAtual == partida.tabuleiro[i][j + contadorPecas + 1])) {
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 4) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        }
                        else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 4) {
                        if (((i+contadorPecas+1) < 5) && ((j+contadorPecas+1) < 8) && (pecaAtual == partida.tabuleiro[i + contadorPecas + 1][j + contadorPecas + 1])) {
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 4) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        }
                        else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;

                    while (contadorPecas < 4) {
                        if (((i-contadorPecas-1) > -1) && ((j+contadorPecas+1) < 8) && (pecaAtual == partida.tabuleiro[i - contadorPecas - 1][j + contadorPecas + 1])) {
                            contadorPecas++;
                        } else {
                            break;
                        }
                    }
                    if (contadorPecas == 4) {
                        if (pecaAtual == 'C') {
                            pontosP1++;
                        }
                        else {
                            pontosP2++;
                        }
                    }
                    contadorPecas = 0;
                }
            }

            topoTabuleiro.append("Pontos do jogador "+partida.jogador1+" : "+pontosP1+"\n");
            topoTabuleiro.append("Pontos do jogador "+partida.jogador2+" : "+pontosP2+"\n");

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
        //TODO: TIMEOUT: retorna 2 (partida encerrada, o que ocorrerá caso o jogador demore muito para enviar a sua jogada e ocorra o time­out de 60 segundos para envio de jogadas)
        Jogador jogador = jogadores.get(idJogador);
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

        char peca = 'C'; // Claras
        if (partida.jogador2.id == jogador.id) {
            peca = 'E'; // Escuras
        }

        partida.tabuleiro[orificioTorre][7 - partida.quantidadeOrificio[orificioTorre]] = peca;
        partida.quantidadeOrificio[orificioTorre]++;
        partida.ultimasJogadas[1] = partida.ultimasJogadas[0];
        partida.ultimasJogadas[0] = orificioTorre;

        if(partida.estado == 2) {
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
            return "";
        }
    }
}
