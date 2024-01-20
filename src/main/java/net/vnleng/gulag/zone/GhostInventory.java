package net.vnleng.gulag.zone;

import net.vnleng.gulag.GulagPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Tiene in memoria l'esperienza e l'inventario di un giocatore mentre si trova nel Gulag
 * Nel caso il giocatore vinca gli elementi presenti possono essere restituiti, altrimenti eliminati.
 */
public class GhostInventory implements InventoryHolder {

    private Inventory inventory; //Contiene l'inventario copiato dal giocatore
    private int current_exp;//Contiene l'esperienza del giocatore all'ingresso del gulag

    public GhostInventory() {
        this.inventory = GulagPlugin.current_instance.getServer().createInventory(this, InventoryType.PLAYER);
    }

    /**
     * Copia l'inventario e l'esperienza di un giocatore in questo inventario, dopodichè svuota quello del giocatore.
     *
     * @param p Giocatore da cui trasferire l'inventario
     */
    public void transferFrom(Player p) {
        //Salvo i contenuti del giocatore
        this.current_exp = p.getTotalExperience();
        this.inventory.setContents(p.getInventory().getContents());
        //resetto il giocatore
        p.getInventory().clear();
        p.setExperienceLevelAndProgress(0);
    }

    /**
     * Trasferisco i contenuti di questo inventario salvato al giocatore
     *
     * @param p Giocatore a cui trasferire i contenuti dell'inventario
     */
    public void transferTo(Player p) {
        p.setExperienceLevelAndProgress(this.current_exp);
        p.getInventory().setContents(inventory.getContents());
    }

    /**
     * Resetta gli inventari.
     */
    public void delete() {
        this.inventory.clear();
        this.inventory.close();

        this.inventory.close();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
