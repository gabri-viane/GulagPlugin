package net.vnleng.gulag.zone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.vnleng.gulag.events.GulagCountdownListener;
import net.vnleng.gulag.events.GulagPlayerListListener;
import net.vnleng.gulag.utils.Generator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Gestisce la scoreboard dentro il gulag.
 */
public class GulagInfo {

    private final Generator<TextComponent> __in_attesa = () -> Component.text("Nel gulag: ").color(TextColor.fromHexString("#cccccc"));
    private final Generator<TextComponent> __prox_round = () -> Component.text("Prossimo round in:").color(TextColor.fromHexString("#cccccc"));//ff9933

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
        Team cntdwn = gulag_scoreboard.registerNewTeam("countdown");
        Team in_gulag = gulag_scoreboard.registerNewTeam("players_in_gulag");

        String entry_cntdwn = Component.text("").color(NamedTextColor.AQUA).append(Component.text("").color(NamedTextColor.DARK_GREEN)).content();
        String entry_in_gulag = Component.text("").color(NamedTextColor.BLACK).content();

        cntdwn.addEntry(entry_cntdwn);
        cntdwn.prefix(Component.text("Nessun round").color(TextColor.fromHexString("#e10000")));
        obj.getScore(entry_cntdwn).setScore(1);

        Score money = obj.getScore(__prox_round.generateInstance().content());
        money.setScore(2);

        in_gulag.addEntry(entry_in_gulag);
        in_gulag.prefix(__in_attesa.generateInstance().append(Component.text("0").color(TextColor.fromHexString("#ff9933"))));
        obj.getScore(entry_in_gulag).setScore(3);

        //Aggiungo listener per il contatore del prossimo round
        gh.addCountdownListener(new GulagCountdownListener() {
            int total;

            @Override
            public void onCountdownStarted(int total_senconds) {
                this.total = total_senconds;
                cntdwn.prefix(Component.text(total)
                        .color(TextColor.fromHexString("#ff9933"))
                        .append(Component.text("/" + total)
                                .color(TextColor.fromHexString("#cccccc"))));
            }

            @Override
            public void onCountdownFinished() {
                cntdwn.prefix(__prox_round.generateInstance().append(
                        Component.text("Match iniziato")
                                .color(TextColor.fromHexString("#ff9933"))));
            }

            @Override
            public void onCountdownStep(int left_seconds) {
                cntdwn.prefix(Component.text(left_seconds)
                        .color(TextColor.fromHexString("#ff9933"))
                        .append(Component.text("/" + total)
                                .color(TextColor.fromHexString("#cccccc"))));
            }
        });

        gh.addListener(new GulagPlayerListListener() {
            @Override
            public void onPlayerLeft(Player p, boolean defeated) {
                p.setScoreboard(empty_scoreboard);
                in_gulag.prefix(__in_attesa.generateInstance().append(Component.text(gh.getPlayersInGulag()).color(TextColor.fromHexString("#ff9933"))));
            }

            @Override
            public void onPlayerEntered(Player p) {
                p.setScoreboard(gulag_scoreboard);
                in_gulag.prefix(__in_attesa.generateInstance().append(Component.text(gh.getPlayersInGulag()).color(TextColor.fromHexString("#ff9933"))));
            }
        });
    }
}
