package net.vnleng.gulag.zone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.vnleng.gulag.GulagPlugin;
import net.vnleng.gulag.events.GulagCountdownListener;
import net.vnleng.gulag.events.GulagPlayerListListener;
import net.vnleng.gulag.utils.Generator;
import net.vnleng.gulag.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gestisce i giocatori e i turni nel gulag
 */
public class GulagHandler {

    private static GulagHandler instance;

    private final ArrayList<Player> teleport_on_respawn;//Lista di giocatore in attesa di essere teletrasportati in uscita
    private final ArrayList<Player> entering;//Lista di giocatori in attesa di essere tletrasportati in ingresso
    private final Queue<Player> player_on_hold;//Giocatori in attesa di combattere
    private final HashMap<UUID, GhostInventory> inventories_on_hold;//Inventari sequestrati dei giocatori nel gulag

    private final ArrayList<GulagPlayerListListener> player_list_listeners;
    private final ArrayList<GulagCountdownListener> countdown_listeners;

    private GulagInfo info;//Gestisce le informazioni del gulag(scoreboard)
    private Integer next_round_taskID = null;//ID della task che gestisce l'inizio di un round
    private Integer timeout_taskID = null;//ID della task che gestisce l'uscita dell'unico giocatore nel gulag

    Generator<BukkitRunnable> free_player_in_gulag = () -> { //task per la liberazione del giocatore
        return new BukkitRunnable() {
            @Override
            public void run() {
                timeout_taskID = null;
                for (Player p : player_on_hold) {//Teoricamente ho un solo giocatore
                    leaveGulag(p, false);//Libero senza sconfitta
                }
            }
        };
    };
    private FightHandler fh;//Gestore della battaglia corrente
    Generator<BukkitRunnable> next_round = () -> {//task per la prossima battaglia
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (player_on_hold.size() > 1) {
                    //rimuove i due giocatori dalla lista
                    Player p1 = player_on_hold.remove();
                    Player p2 = player_on_hold.remove();
                    //Teletrasporto i giocatori nelle posizioni di combattimento
                    Location[] gulagFightLocations = Settings.getInstance().getGulagFightLocations();
                    p1.teleport(gulagFightLocations[0]);
                    p2.teleport(gulagFightLocations[1]);
                    //inizia una nuova battaglia
                    fh = new FightHandler(p1, p2);
                    countdown_listeners.forEach(fh::addCountdownlistener);
                    fh.onEnd(() -> {
                        next_round_taskID = null;
                        if(player_on_hold.size()>1) {
                            Iterator<Player> i = player_on_hold.iterator();
                            Player p3 = i.next();
                            Player p4 = i.next();
                            Component c = Component.text("Sei il prossimo a combattere: preparati!")
                                    .color(TextColor.fromHexString("#ff9933"));
                            p3.sendMessage(c);
                            p4.sendMessage(c);
                        }
                    });
                    fh.startMatch();
                }
            }
        };
    };

    private GulagHandler() {
        player_on_hold = new LinkedList<>();
        inventories_on_hold = new HashMap<>();
        teleport_on_respawn = new ArrayList<>();
        player_list_listeners = new ArrayList<>();
        countdown_listeners = new ArrayList<>();
        entering = new ArrayList<>();
        init();
    }

    public static GulagHandler getInstance() {
        if (instance == null) {
            GulagHandler.instance = new GulagHandler();
        }
        return GulagHandler.instance;
    }

    /**
     * Inizializza il gestore del gulag
     */
    private void init() {
        //Aggiunge un listener per gestire le task di liberazione del singolo giocatore o di inizio battaglia
        addListener(new GulagPlayerListListener() {
            @Override
            public void onPlayerLeft(Player p, boolean defeated) {
                checkLeftPlayers();
            }

            @Override
            public void onPlayerEntered(Player p) {
                checkLeftPlayers();
            }
        });
        info = new GulagInfo(this);//creo la scoreboard
    }

    /**
     * Controlla quanti giocatori ci sono nella lista del gulag.
     * - Se ne rimane solo uno avvio la task di uscita gratis
     * - Se non ce ne sono annullo tutte le task
     * - Se ce ne sono più di uno controllo se non ci sono già battaglie in atto
     */
    private void checkLeftPlayers() {
        if (player_on_hold.size() == 1) {
            if (next_round_taskID != null) {//Se ho già attivato la task "prossimo round" allora la resetto
                Bukkit.getScheduler().cancelTask(next_round_taskID);
            }
            if (timeout_taskID != null) {//Se ho già attivato la task "uscita gratis" allora la resetto
                Bukkit.getScheduler().cancelTask(timeout_taskID);
            }
            Player p = player_on_hold.peek();
            if (p != null) {
                p.sendMessage(Component.text("Verrai fatto uscire se nessun giocatore entra nel gulag in "
                        + Settings.getInstance().getLonelyCountdown() + " secondi")
                        .color(TextColor.fromHexString("#cc9900")));
            }
            timeout_taskID = free_player_in_gulag.generateInstance()
                    .runTaskLater(GulagPlugin.current_instance,
                            20L * Settings.getInstance().getLonelyCountdown())
                    .getTaskId();//Imposto l'evento a X secondi dopo (X letto dalle configurazioni)
        } else if (player_on_hold.size() == 0) {
            if (next_round_taskID != null) {
                Bukkit.getScheduler().cancelTask(next_round_taskID);
            }
            if (timeout_taskID != null) {
                Bukkit.getScheduler().cancelTask(timeout_taskID);
            }
        } else {
            if (timeout_taskID != null) {
                Bukkit.getScheduler().cancelTask(timeout_taskID);
            }
            if (next_round_taskID == null) {
                next_round_taskID = next_round.generateInstance().runTaskLater(GulagPlugin.current_instance, 20L * 10L).getTaskId();//Dopo 10 secondi avvio una partita
                if(player_on_hold.size()>1) {
                    Iterator<Player> i = player_on_hold.iterator();
                    Player p1 = i.next();
                    Player p2 = i.next();
                    Component c = Component.text("Il tuo turno inizia tra 10 secondi!")
                            .color(TextColor.fromHexString("#ff9933"));
                    p1.sendMessage(c);
                    p2.sendMessage(c);
                }
            }
        }
    }

    /**
     * Aggiunge un giocatore alla lista di ingresso del Gulag pronto per entrare al suo respawn (RICORDA: i giocatori
     * entrano quando muoiono)
     *
     * @param p
     */
    public void enterGulag(Player p) {
        this.entering.add(p);
    }

    /**
     * Inserisce un player nel gulag
     *
     * @param p Giocatore da inserire nel Gulag
     */
    public void joinGulag(Player p) {
        //p.setMetadata(Settings.META_IN_GULAG, new FixedMetadataValue(GulagPlugin.current_instance, true));
        if (isPlayerEnteringGulag(p)) {
            this.entering.remove(p);
            //Salva l'inventario
            GhostInventory gi = new GhostInventory();
            gi.transferFrom(p);
            //Inserisci il giocatore nel gulag
            player_on_hold.add(p);
            inventories_on_hold.put(p.getUniqueId(), gi);
            p.sendMessage(Component.text("Benvenuto nel Gulag: combatti per tornare in libertà!")
                    .style(Style.style(TextDecoration.ITALIC))
                    .color(TextColor.fromHexString("#ffcc00")));
            //Scateno l'evento di aggiunta del giocatore
            callAddPlayerEvent(p);
        } else {
            p.sendMessage(
                    Component.text("Non sei nella coda di ingresso del gulag: non devi combattere per la tua libertà ")
            );
        }
    }

    public void leaveGulag(Player p, boolean defeated) {
        //Solo se il giocatore è in lista di entrata o nel gulag
        if (this.isPlayerEnteringGulag(p) || this.isPlayerInGulag(p) || (fh != null && fh.isPlayerInMatch(p))) {
            if (defeated) {//Se il giocatore morto è quello sconfitto
                if (p.isOnline()) {//Potrebbe essersi semplicemente disconnesso
                    p.sendMessage(Component.text("Non sei abbastanza forte!\nNon ti verranno restituiti gli oggetti ritirati!")
                            .style(Style.style(TextDecoration.BOLD))
                            .color(TextColor.fromHexString("#ff0000")));

                }
                //Cancella i dati dal gulag
                inventories_on_hold.remove(p.getUniqueId()).delete();
                player_on_hold.remove(p);//(dovrebbero essere già rimosso ad inizio battaglia)
                //Poichè il giocatore è morto aspetto a teletrasportarlo
                //Se ne occupa la classe PlayerWorldEvent
                teleport_on_respawn.add(p);
            } else {//Il giocatore ha vinto o è uscito perchè non c'era nessun altro
                GhostInventory gi = this.inventories_on_hold.remove(p.getUniqueId());
                gi.transferTo(p);//Trasferisco l'inventario nuovamente al giocatore
                gi.delete();//Libero lo spazio
                player_on_hold.remove(p);//Rimuovi dal gulag (dovrebbero essere già rimosso ad inizio battaglia)
                p.sendMessage(Component.text("Per questa volta te la sei cavata... eccoti i tuoi oggetti!")
                        .style(Style.style(TextDecoration.ITALIC))
                        .color(TextColor.fromHexString("#22ca22"))
                );
                //Teletrasporto il giocatore nella zona di uscita
                p.teleport(Settings.getInstance().getGulagExitLocation());
            }
            //Scateno l'evento di uscita dal gulag del giocatore
            callRemovePlayerEvent(p, defeated);
        } else {
            p.sendMessage(
                    Component.text("Non sei nel Gulag: per uscire ci devi ")
                            .append(Component.text("entrare...")
                                    .style(Style.style(TextDecoration.UNDERLINED))
                                    .color(TextColor.fromHexString("#ff5733"))
                                    .clickEvent(ClickEvent.callback(audience -> {
                                        Location l = p.getLocation();
                                        GulagPlugin.current_instance.getServer()
                                                .sendMessage(p.displayName().append(
                                                        Component.text(
                                                                " vuole entrare nel gulag, aiutatelo a raggiungere il suo obiettivo: "
                                                                        + l.getX() + " " + l.getY() + " " + l.getZ())));
                                    }))
                            )
            );
        }

    }

    /**
     * Controlla se un giocatore è stato inserito nel Gulag
     *
     * @param p Giocatore da controllare
     * @return
     */
    public boolean isPlayerInGulag(Player p) {
        return this.player_on_hold.contains(p);
    }

    /**
     * Controlla se un giocatore è in lista per entrare nel Gulag
     *
     * @param p
     * @return
     */
    public boolean isPlayerEnteringGulag(Player p) {
        return this.entering.contains(p);
    }

    /**
     * Controllo se un giocatore è morto nel gulag e attende di essere teletrasportato
     *
     * @param p
     * @return
     */
    public boolean isPlayerDeadInGulag(Player p) {
        return this.teleport_on_respawn.contains(p);
    }

    /**
     * Rimuove il giocatore
     *
     * @param p
     * @return
     */
    public boolean removePlayerDeadInGulag(Player p) {
        return this.teleport_on_respawn.remove(p);
    }

    /**
     * Chiama tutti i listener in ascolto quando un giocatore esce dal gulag
     *
     * @param p
     * @param defeated {@code true} se il giocatore che esce è stato sconfitto
     */
    private void callRemovePlayerEvent(Player p, boolean defeated) {
        this.player_list_listeners.forEach(l -> l.onPlayerLeft(p, defeated));
    }

    /**
     * Chiama tutti i listener in ascolto quando un giocatore entra nel gulag
     *
     * @param p
     */
    private void callAddPlayerEvent(Player p) {
        this.player_list_listeners.forEach(l -> l.onPlayerEntered(p));
    }

    /**
     * Aggiunge un listener
     *
     * @param ge
     */
    public void addListener(GulagPlayerListListener ge) {
        this.player_list_listeners.add(ge);
    }

    /**
     * Rimuove un listener
     *
     * @param ge
     */
    public void removeListener(GulagPlayerListListener ge) {
        this.player_list_listeners.remove(ge);
    }

    public int getPlayersInGulag() {
        return this.player_on_hold.size();
    }

    public void addCountdownListener(GulagCountdownListener gcl) {
        this.countdown_listeners.add(gcl);
    }

    public void removeCountdownListener(GulagCountdownListener gcl){
        this.countdown_listeners.remove(gcl);
    }

}
