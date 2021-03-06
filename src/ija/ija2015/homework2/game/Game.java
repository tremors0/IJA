package ija.ija2015.homework2.game;
import ija.ija2015.homework2.board.Board;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * Reprezentuje hru. Při inicializaci se vždy přiřadí hrací deska.
 *
 * @author xpavlu08, xjelin42
 */
public class Game implements Serializable{
    private final Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private boolean otherCantPlay = true;
    private boolean ended = false;
    private int counts[] = new int[2];
    
    
    /**
     * Inicializuje hru.
     *
     * @param board Hrací deska, která je součástí hry.
     */
    public Game(Board board) {
        this.board = board;
        this.whitePlayer = null;
        this.blackPlayer = null;
        this.currentPlayer = null;
    }
    
    
    /**
     * Přidá hráče a současně vyvolá jeho inicializaci. Pokud hráč dané barvy
     * již existuje, nedělá nic a vrací false.
     *
     * @param player Přidávaný hráč.
     * @return Úspěch akce.
     */
    public boolean addPlayer(Player player) {
        if (this.blackPlayer == null && player.isWhite() == false) {
            this.blackPlayer = player;
            blackPlayer.init(this.board);
            this.currentPlayer = player; /* ve hre Othello zacina cerny hrac */
            return true;
        } else if (this.whitePlayer == null && player.isWhite() == true) {
            this.whitePlayer = player;
            whitePlayer.init(this.board);
            return true;
        }
        return false;
    }
    
    
    /**
     * Vrátí aktuálního hráče, který je na tahu.
     *
     * @return Aktuální hráč.
     *
     */
    public Player currentPlayer() {
        return this.currentPlayer;
    }
    
    
    /**
     * Vrátí bílého hráče.
     *
     * @return Bílý hráč
     */
    public Player getWhitePlayer() {
        return whitePlayer;
    }
    
    
    /**
     * Vrátí černého hráče.
     *
     * @return Černý hráč
     */
    public Player getBlackPlayer() {
        return blackPlayer;
    }
    
    /**
     * Nastaví aktuálního hráče na hráče, který je předán jako parametr. Využívá
     * se pouze u AI u minimaxu.
     *
     * @param currentPlayer Hráč, jenž bude nastaven jako aktuální.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    
    
    /**
     * Změní aktuálního hráče.
     *
     * @return Aktuálně nastavený hráč.
     */
    public Player nextPlayer() {
        if (this.currentPlayer == this.blackPlayer) {
            this.currentPlayer = this.whitePlayer;
        } else {
            this.currentPlayer = this.blackPlayer;
        }
        return this.currentPlayer;
    }
    
    
    /**
     * Funkce slouží k zápisu do souboru.
     *
     * @param name Název souboru.
     * @return True, pokud se podařil zápis.
     */
    public boolean writeToFile(String name) {
        try {
            File dir = new File("saves");
            dir.mkdir();
            if (new File("./saves/" + name).isFile()) {
                int dialogResult = JOptionPane.showConfirmDialog(null, "Would You Like rewrite exist game?", "Warning", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
            FileOutputStream fout = new FileOutputStream("./saves/" + name);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
            oos.close();
            //System.out.println("Done");
            return true;

        } catch (HeadlessException | IOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * Metoda spouští hru v textové verzi. Využíváno hlavně pro debug.
     */
    public void play() {
        Scanner sc = new Scanner(System.in);
        while (!ended) {
            if (this.currentPlayer().getInteligence() == Player.Ai.human) { //clovek
                int x, y;
                x = y = 0;
                boolean ok = false;
                do {
                    ok = false;
                    if (this.currentPlayer().getLegals(this.getBoard()).isEmpty()) {
                        //System.out.println("Neni kam hrat");
                        if (otherCantPlay) {
                            ended = true;
                            break;
                        } else {
                            this.nextPlayer();
                            otherCantPlay = true;
                            break;
                        }
                    } else if ((this.currentPlayer.takenFromPool == this.getBoard().getRules().numberDisks())) {
                        //System.out.println("Dosly disky!");
                        this.nextPlayer();
                        if (otherCantPlay) {
                            ended = true;
                            break;
                        }
                        otherCantPlay = true;
                    } else {
                        otherCantPlay = false;
                        //System.out.println("tahne: " + this.currentPlayer().toString());
                        //System.out.println("Zadej X a pak Y");
                        x = sc.nextInt();
                        if(x==100){
                            this.writeToFile("save.sv");
                        }
                        y = sc.nextInt();
                        ok = true;
                    }
                } while (!this.currentPlayer().canPutDisk(this.getBoard().getField(x, y)));
                if (ok) {
                    this.currentPlayer().putDisk(this.getBoard().getField(x, y));
                    this.nextPlayer();
                }
                //konec cloveka
            } else {

                if (this.currentPlayer().getLegals(this.getBoard()).isEmpty()) {
                    //System.out.println("Neni kam hrat");
                    if (otherCantPlay) {
                        ended = true;
                        break;
                    }
                    this.nextPlayer();
                    otherCantPlay = true;
                } else if (this.currentPlayer.takenFromPool == this.getBoard().getRules().numberDisks()) {
                    //System.out.println("Dosly disky!");
                    this.nextPlayer();
                    if (otherCantPlay) {
                        ended = true;
                    }
                    otherCantPlay = true;
                } else {
                    this.currentPlayer().putDisk(this);
                    this.nextPlayer();
                    otherCantPlay = false;
                }

            }
            counts = this.getBoard().score();
            //System.out.println("bílý: " + counts[0] + " černý: " + counts[1]);
            this.getBoard().toString();
        }
        //System.out.println("HRA SKONCILA!");
        if (counts[1] > counts[0]) {
           // System.out.println(">>>>BLACK WIN<<<<");
        } else {
           // System.out.println(">>>>WHITE WIN<<<<");
        }
    }
    
    /**
     * Nastaví aktuálního hráče na hráče, jenž má barvu, kterou funkce přebírá
     * parametrem. Využíváno pouze ve třídě AiPlayer v mětodě putDisk.
     * Zajišťuje, aby po simulaci hry v minimaxu táhnul správný hráč.
     *
     * @param color Barva hráče
     */
    public void setPlayerWithColor(boolean color) {
        if (this.currentPlayer.white != color) {
            this.nextPlayer();
        }
    }
    
    /**
     * Vrátí hrací desku.
     *
     * @return Hrací deska.
     */
    public Board getBoard() {
        return this.board;
    }
    
    
    /**
     * Vrátí referenci na hráče, jenž není právě aktuálním hráčem.
     *
     * @return hráč, který není na tahu
     */
    public Player getOtherPlayer() {
        if (currentPlayer == blackPlayer)
            return whitePlayer;
        else
            return blackPlayer;
    }
    
}
