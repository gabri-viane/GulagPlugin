package net.vnleng.gulag.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.vnleng.gulag.GulagPlugin;
import net.vnleng.gulag.utils.Settings;
import net.vnleng.gulag.zone.GulagHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerWorldEvent implements Listener {

    private GulagHandler gh = GulagHandler.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDied(PlayerDeathEvent pde) {
        //Controllo se il giocatore è già nel gualg, o entrando nel gulag, o morto nel gulag
        Player p = pde.getPlayer();
        if (!gh.isPlayerInGulag(p) && !gh.isPlayerEnteringGulag(p) && !gh.isPlayerDeadInGulag(p)) {
            //Elimino i drop / imposto il keepinventory
            pde.setKeepInventory(true);
            pde.setKeepLevel(true);
            pde.getDrops().clear();
            pde.setDroppedExp(0);
            pde.setShouldDropExperience(false);

            //Mando messaggio di morte
            pde.deathMessage(Component.text(p.getName()).append(Component.text(" entra nel gulag: ").color(TextColor.fromHexString("#ff0000"))
                    .append(Component.text("Combatte per la libertà!"))));
            GulagHandler.getInstance().enterGulag(p);
        }
    }

    /**
     * Controllo se il player che respawna è marchiato per il gulag e in caso lo teletrasporto alla zona di attesa gulag
     *
     * @param pre
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent pre) {
        Player p = pre.getPlayer();
        if (gh.isPlayerEnteringGulag(p) || gh.isPlayerInGulag(p)) {//Se un giocatore è morto e sta aspettando di entrare nel gulag oppure se il giocatore è morto nel gulag ma non nel match
            BukkitTask br = new BukkitRunnable() {
                @Override
                public void run() {
                    gh.joinGulag(p);
                    p.teleport(Settings.getInstance().getGulagLocation());
                }
            }.runTaskLater(GulagPlugin.current_instance, 20L);
        } else if (gh.isPlayerDeadInGulag(p)) {//Se un giocatore è morto nel match
            BukkitTask br = new BukkitRunnable() {
                @Override
                public void run() {
                    gh.removePlayerDeadInGulag(p);
                    p.teleport(Settings.getInstance().getGulagExitLocation());
                }
            }.runTaskLater(GulagPlugin.current_instance, 20L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent pqe) {
        Player p = pqe.getPlayer();
        if (gh.isPlayerInGulag(p)) {//Se il player lascia mentre è nel gulag lo faccio abbandonare
            gh.leaveGulag(p, true);//Segna il giocatore come sconfitto
        }
    }

    public void unregister() {
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
    }
}
