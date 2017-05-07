import java.rmi.Naming;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sidney on 03-May-17.
 */
public class DownUnderClient {
    public static void main (String[] args) {

        if(args.length !=2) {
            System.out.println("Uso: java DownUnderClient <maquina> <nome do jogador>");
            System.exit(1);
        }
        try {
            DownUnderInterface downunder = (DownUnderInterface) Naming.lookup("//"+args[0]+"/DownUnder");
            Scanner reader = new Scanner(System.in);
            int idJogador = downunder.registraJogador(args[1]);
            System.out.println("Buscando oponente...");
            int temPartida = downunder.temPartida(idJogador);
            while(temPartida == 0) {
                TimeUnit.SECONDS.sleep(1);
                temPartida = downunder.temPartida(idJogador);
            }
            //TODO: BUG: na segunda partida, começa mesmo sendo o jogador ímpar
            System.out.println("Partida criada, seu oponente é: "+downunder.obtemOponente(idJogador));

            char pecaJogador = 'C';
            char pecaOponente = 'E';
            if(temPartida == 2) {
                pecaJogador = 'E';
                pecaOponente = 'C';
            }

            System.out.println("Suas peças: "+pecaJogador+"\nPeças do seu oponente: "+pecaOponente);
            if(temPartida == 1) {
                System.out.println("Você joga primeiro.");
            }
            else {
                System.out.println("Seu oponente joga primeiro.");
            }

            int turno = 0;
            while (turno != 20) {
                int vezDoJogador = downunder.ehMinhaVez(idJogador);
                if (vezDoJogador != 1) {
                    System.out.println("Esperando jogada do seu oponente...");
                }
                while (vezDoJogador != 1) {
                    TimeUnit.SECONDS.sleep(1);
                    vezDoJogador = downunder.ehMinhaVez(idJogador);
                }

                String topoTorre = downunder.obtemTabuleiro(idJogador);
                System.out.println("Sua vez. Estado do jogo:\n" + topoTorre + "\n\nEscolha a torre para inserir sua peça:");
                int userInput = reader.nextInt();
                int userMovement = downunder.soltaEsfera(idJogador, userInput);

                while (userMovement != 1) {
                    if (userMovement == -1) {
                        System.out.println("Posição da torre inválida. Insira novamente:");
                    } else if (userMovement == 0) {
                        System.out.println("Este orifício já foi preenchido. Insira novamente:");
                    }
                    userInput = reader.nextInt();
                    userMovement = downunder.soltaEsfera(idJogador, userInput);
                }
                System.out.println("Movimento realizado. \nEstado do jogo:\n" + topoTorre);
                turno++;
            }

            if(temPartida == 1) {
                int vezDoJogador = downunder.ehMinhaVez(idJogador);
                while (vezDoJogador != 1) {
                    TimeUnit.SECONDS.sleep(1);
                    vezDoJogador = downunder.ehMinhaVez(idJogador);
                }
            }

            System.out.println("Jogo encerrado. Tabuleiro final:\n\n"+downunder.obtemTabuleiro(idJogador)+"\n");
            System.out.println("Encerrando partida...");

            //TODO: teste se encerrou corretamente (verificar outros métodos usados se não houve erro também)
            downunder.encerraPartida(idJogador);

        } catch (Exception e) {
            System.out.println("DownUnderClient failed.");
            e.printStackTrace();
        }
    }
}
