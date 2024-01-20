package net.vnleng.gulag.zone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.vnleng.gulag.events.GulagCountdownListener;
import net.vnleng.gulag.events.GulagPlayerListListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Gestisce la scoreboard dentro il gulag.
 */
public class GulagInfo {

    private final String __in_attesa = Component.text("In attesa:").color(TextColor.fromHexString("#ff9933")).content();
    private final String __prox_round = Component.text("Prossimo round:").color(TextColor.fromHexString("#ff9933")).content();

    private final Scoreboard gulag_scoreboard;
    private final Scoreboard empty_scoreboard;//scoreboard vuota per resettare il giocatore
    private final GulagHandler gh;

    public GulagInfo(GulagHandler gh) {
        this.gh = gh;
        this.gulag_scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.empty_scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        init();
    }

    /**
     * Inizializza la scoreboard
     */
    private void init() {
        Objective obj = this.gulag_scoreboard.registerNewObjective("GulagInfo", Criteria.DUMMY, Component.text("Info Gulag"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score in_gulag = obj.getScore(__in_attesa);

        //Aggiungo listener per il contatore del prossimo round
        gh.addCountdownListener(new GulagCountdownListener() {
            @Override
            public void onCountdownStarted(int total_senconds) {
                Score next_gl = obj.getScore(__prox_round);
                next_gl.setScore(total_senconds);
            }

            @Override
            public void onCountdownFinished() {
                obj.getScore(__prox_round).setScore(0);
            }

            @Override
            public void onCountdownStep(int left_seconds) {
                obj.getScore(__prox_round).setScore(left_seconds);
            }
        });

        gh.addListener(new GulagPlayerListListener() {
            @Override
            public void onPlayerLeft(Player p, boolean defeated) {
                //players_gulag.prefix(Component.text(gh.getPlayersInGulag()).color(TextColor.fromHexString("#00cc00")));
                p.setScoreboard(empty_scoreboard);
                in_gulag.setScore(gh.getPlayersInGulag());
            }

            @Override
            public void onPlayerEntered(Player p) {
                //players_gulag.prefix(Component.text(gh.getPlayersInGulag()).color(TextColor.fromHexString("#00cc00")));
                p.setScoreboard(gulag_scoreboard);
                in_gulag.setScore(gh.getPlayersInGulag());
            }
        });
    }
}
