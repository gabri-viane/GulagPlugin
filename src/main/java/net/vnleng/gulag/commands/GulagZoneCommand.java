package net.vnleng.gulag.commands;

import net.vnleng.gulag.zone.FightHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GulagZoneCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.isOp()){
            sender.sendMessage(sender.getName());
            FightHandler fh = new FightHandler(Bukkit.getPlayer(sender.getName()),null);
            fh.onEnd(() -> sender.sendMessage("Fine scontro"));
            fh.startMatch();
        }
        return false;
    }

    private void testFight(CommandSender sender){

    }
}
