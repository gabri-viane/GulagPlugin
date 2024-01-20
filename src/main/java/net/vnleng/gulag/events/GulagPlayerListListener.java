package net.vnleng.gulag.events;

import org.bukkit.entity.Player;

public interface GulagPlayerListListener {

    public void onPlayerLeft(Player p,boolean defeated);

    public void onPlayerEntered(Player p);

}
