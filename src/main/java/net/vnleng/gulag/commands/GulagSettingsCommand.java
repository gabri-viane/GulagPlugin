package net.vnleng.gulag.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.vnleng.gulag.GulagPlugin;
import net.vnleng.gulag.utils.Settings;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che gestisce le impostazioni del plugin tramite il comando in chat "gulagsettings"
 */
public class GulagSettingsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isOp()) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "get":
                        return getAction(sender, args);
                    case "set":
                        return setAction(sender, args);
                    case "save"://Salva la configurazione
                        Settings.getInstance().saveConfig();
                        return true;
                    case "reload"://Ricarica la configurazione
                        Settings.getInstance().reloadConfig();
                        return true;
                    default:
                        sender.sendMessage(Component.text("Parametri errati"));
                        return false;
                }
            } else {
                sender.sendMessage(Component.text("Parametri mancanti"));
            }
        } else {
            sender.sendMessage(Component.text("Non hai i privilegi necessari per accedere alle impostazioni"));
        }
        return false;
    }

    /**
     * Gestisce il comando di set
     *
     * @param sender
     * @param args
     * @return
     */
    private boolean setAction(CommandSender sender, String[] args) {
        if (args.length > 1) {//Controllo i parametri
            switch (args[1]) {
                case "location":
                    if (args.length > 6) {
                        int x = Integer.parseInt(args[3]);
                        int y = Integer.parseInt(args[4]);
                        int z = Integer.parseInt(args[5]);
                        String[] y_n_p = args[6].split(":");
                        float yaw = Float.parseFloat(y_n_p[0]);
                        float pitch = Float.parseFloat(y_n_p[1]);
                        Location l = new Location(GulagPlugin.current_instance.getServer().getWorld("world"), x, y, z, yaw, pitch);
                        switch (args[2]) {
                            case "exit": //Imposto la posizione di uscita dal gulag
                                Settings.getInstance().setExitGulagLocation(l);
                                break;
                            case "spawn"://Imposto la posizione di spawn del gulag
                                Settings.getInstance().setGulagLocation(l);
                                break;
                            case "p1"://Imposto la posizione di battaglia del player 1
                                Settings.getInstance().setFightLocationP1(l);
                                break;
                            case "p2"://Imposto la posizione di battaglia del player 2
                                Settings.getInstance().setFightLocationP2(l);
                                break;
                            default:
                                return false;
                        }
                    }
                    return true;
                case "countdown":
                    if (args.length > 3) {
                        int time = Integer.parseInt(args[3]);
                        switch (args[2]) {
                            case "match"://Imposto il tempo di countdown del match
                                Settings.getInstance().setMatchCountdown(time);
                                break;
                            case "wait"://Imposto il tempo di countdown dell'uscita dal gulag in assenza di giocatori
                                Settings.getInstance().setLonelyCountdown(time);
                                break;
                            default:
                                return false;
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Gestisce il comando di get
     *
     * @param sender
     * @param args
     * @return
     */
    private boolean getAction(CommandSender sender, String[] args) {
        if (args.length > 1) {
            switch (args[1]) {
                case "location":
                    switch (args[2]) {
                        case "p1":
                            sender.sendMessage(Component.text("Location Player 1: ")
                                    .append(Component.text(Settings.getInstance().getGulagFightLocations()[0].toString())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set location p1"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff"))));
                            return true;
                        case "p2":
                            sender.sendMessage(Component.text("Location Player 2: ")
                                    .append(Component.text(Settings.getInstance().getGulagFightLocations()[1].toString())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set location p2"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff"))));
                            return true;
                        case "spawn":
                            sender.sendMessage(Component.text("Gulag Spawn Location: ")
                                    .append(Component.text(Settings.getInstance().getGulagLocation().toString())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set location spawn"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff"))));
                            break;
                        case "exit":
                            sender.sendMessage(Component.text("Gulag Exit Location: ")
                                    .append(Component.text(Settings.getInstance().getGulagLocation().toString())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set location exit"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff"))));
                            break;
                        default:
                            return false;
                    }
                    return true;
                case "countdown":
                    switch (args[2]) {
                        case "match":
                            sender.sendMessage(Component.text("Match Timeout: ")
                                    .append(Component.text(Settings.getInstance().getMatchCountdown())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set countdown match"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff")))
                                    .append(Component.text(" seconds")));
                            return true;
                        case "wait":
                            sender.sendMessage(Component.text("Wait Timeout: ")
                                    .append(Component.text(Settings.getInstance().getLonelyCountdown())
                                            .clickEvent(ClickEvent.suggestCommand("/gulagsettings set countdown wait"))
                                            .style(Style.style(TextDecoration.UNDERLINED))
                                            .color(TextColor.fromHexString("#0000ff")))
                                    .append(Component.text(" seconds")));
                            return true;
                        default:
                            return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("gulagsettings")) {  //your command name
            List<String> l = new ArrayList<>(); //makes a ArrayList
            //define the possible possibility's for argument 1
            switch (args.length) {
                case 1:
                    l.add("get");
                    l.add("set");
                    l.add("save");
                    l.add("reload");
                    break;
                case 2:
                    if (args[0].equals("get") || args[0].equals("set")) {
                        l.add("location");
                        l.add("countdown");
                    }
                    break;
                case 3:
                    switch (args[1]) {
                        case "location":
                            l.add("exit");
                            l.add("spawn");
                            l.add("p1");
                            l.add("p2");
                            break;
                        case "countdown":
                            l.add("match");
                            l.add("wait");
                            break;
                    }
                    break;
                case 4:
                    Player px = GulagPlugin.current_instance.getServer().getPlayer(sender.getName());
                    if (px != null) {
                        l.add("" + px.getLocation().getBlockX());
                    }
                    break;
                case 5:
                    Player py = GulagPlugin.current_instance.getServer().getPlayer(sender.getName());
                    if (py != null) {
                        l.add("" + py.getLocation().getBlockY());
                    }
                    break;
                case 6:
                    Player pz = GulagPlugin.current_instance.getServer().getPlayer(sender.getName());
                    if (pz != null) {
                        l.add("" + pz.getLocation().getBlockZ());
                    }
                    break;
                case 7:
                    Player pt = GulagPlugin.current_instance.getServer().getPlayer(sender.getName());
                    if (pt != null) {
                        l.add(pt.getYaw() + ":" + pt.getPitch());
                    }
            }
            return l; //returns the possibility's to the client
        }
        return null;
    }
}
