package net.vnleng.gulag.zone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.vnleng.gulag.GulagPlugin;
import net.vnleng.gulag.events.GulagCountdownListener;
import net.vnleng.gulag.events.MatchListener;
import net.vnleng.gulag.utils.CountdownTimer;
import net.vnleng.gulag.utils.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Gestisce lo scontro tra due giocatori ereditati dalla lista del gulag.
 * Se un giocatore prova a muoversi prima dell'inizio del match l'evento viene annullato, inoltre gestisce anche
 * l'evento di morte dei giocatori che sono in questo match.
 */
public class FightHandler implements Listener {

    private final Player p1;
    private final Player p2;

    private boolean started = false;
    private MatchListener me;//Listener in ascolto per la fine del match
    private final ArrayList<GulagCountdownListener> cnt_listeners;

    /**
     * Crea un nuovo match
     *
     * @param p1 Giocatore 1
     * @param p2 Giocatore 2
     */
    public FightHandler(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        cnt_listeners = new ArrayList<>();
        //Mi registro come listener per ascoltare gli eventi di movimento/morte
        GulagPlugin.current_instance.getServer().getPluginManager().registerEvents(this, GulagPlugin.current_instance);
    }

    /**
     * Fa partire il match, inizializza il timer e:
     * 1) Mostra il titolo di inizio sfida
     * 2) Ogni secondo mostro quanti secondi mancano all'inizio match
     * 3) Mostro il titolo di via al combattimento
     */
    public void startMatch() {
        int seconds = Settings.getInstance().getMatchCountdown();
        CountdownTimer ct = new CountdownTimer(seconds,
                () -> {
                    cnt_listeners.forEach(l -> l.onCountdownStarted(seconds));
                    p1.showTitle(Title.title(
                            Component.text("La sfida sta per iniziare!"),
                            Component.text("Preparati per la sfida contro: " + p2.getName()),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500)))
                    );
                    if(p2!=null) {
                        p2.showTitle(Title.title(
                                Component.text("La sfida sta per iniziare!"),
                                Component.text("Preparati per la sfida contro: " + p1.getName()),
                                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500)))
                        );
                    }
                },
                () -> {
                    started = true;
                    Title t = Title.title(
                            Component.text("Combatti!"),
                            Component.empty(),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(1), Duration.ofMillis(200)));
                    p1.showTitle(t);
                    if(p2!=null) {
                        p2.showTitle(t);
                    }
                },
                (countdownTimer) -> {
                    Title t = Title.title(
                            Component.text(countdownTimer.getSecondsLeft()),
                            Component.empty(),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(800), Duration.ofMillis(200)));
                    p1.showTitle(t);
                    if(p2!=null) {
                        p2.showTitle(t);
                    }
                    int s_left = countdownTimer.getSecondsLeft();
                    cnt_listeners.forEach(l -> l.onCountdownStep(s_left));
                });
        ct.scheduleTimer();//Faccio partire il timer
    }

    /**
     * Cancello tutti gli eventi di movimento dei giocatori in questo match finché non inizia lo scontro
     *
     * @param pme Evento di movimento del giocatore
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMoved(PlayerMoveEvent pme) {
        Player p = pme.getPlayer();
        if ((p == p1 || p == p2) && !started) {
            pme.setCancelled(true);
        }
    }

    /**
     * Aspetto la morte di uno dei due giocatori per far finire la sfida
     *
     * @param pde Evento di morte del giocatore
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent pde) {
        Player p = pde.getPlayer();
        if ((p == p1 || p == p2) && started) {
            //Controllo ch giocatore a vinto e li lascio abbandonare il gulag
            me.onMatchEnd();
            cnt_listeners.forEach(GulagCountdownListener::onCountdownFinished);
            //Rimuovo i giocatori del gulag
            GulagHandler.getInstance().leaveGulag(p1, p == p1);
            GulagHandler.getInstance().leaveGulag(p2, p == p2);

            //Mi tolgo dalla lista in ascolto quando il match finisce
            PlayerMoveEvent.getHandlerList().unregister(this);
            PlayerDeathEvent.getHandlerList().unregister(this);
        }
    }

    /**
     * Imposta il listener che verrà chiamato quando il match finisce.
     *
     * @param me Listener del match
     */
    public void onEnd(MatchListener me) {
        this.me = me;
    }

    public void addCountdownlistener(GulagCountdownListener cnt_listener) {
        this.cnt_listeners.add(cnt_listener);
    }

    /**
     * Controlla se un giocatore è in questo match.
     *
     * @param p Giocatore da trovare
     * @return {@code true} se il giocatore si trova nel match
     */
    public boolean isPlayerInMatch(Player p) {
        return p == p1 || p == p2;
    }

}
