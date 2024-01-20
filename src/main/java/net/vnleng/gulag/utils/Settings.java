package net.vnleng.gulag.utils;

import net.vnleng.gulag.GulagPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Classe che gestisce le impostazioni del plugin
 */
public final class Settings {
    public static final String META_IN_GULAG = "__gp_player_in_gulag";
    /**
     * Posizione di TP dei giocatri morti (zona di ingresso del gulag)
     */
    public static final String STT_GULAG_LOCATION = "gulag.loc.base";
    /**
     * Posizione di TP dei giocatri a fine match(zona di uscita del gulag)
     */
    public static final String STT_EXIT_GULAG_LOCATION = "gulag.loc.exit";
    /**
     * Posizione di Fight del giocatore 1
     */
    public static final String STT_GULAG_FIGHT_LOCATION_1 = "gulag.loc.fight.p1";
    /**
     * Posizione di Fight del giocatore 2
     */
    public static final String STT_GULAG_FIGHT_LOCATION_2 = "gulag.loc.fight.p2";
    /**
     * Numero di secondi di countdown per lo scontro tra due giocatori
     */
    public static final String STT_GULAG_MATCH_COUNTDOWN = "gulag.countdown.match";
    /**
     * Quanti secondi prima che un giocatore esca dal gulag se nessun altro giocatore si trova nel gulag
     */
    public static final String STT_GULAG_LONELY_COUNTDOWN = "gulag.countdown.lonely";


    private static Settings instance;
    private FileConfiguration fc;

    private Location gulag_location; //spawn del gulag
    private Location exit_gulag_location; //uscita dal gulag
    private Location[] fight_locations; //posizioni di battaglia dei giocatori
    private int match_countdown = 5; //contatore di inizio battaglia
    private int lonely_countdown = 120; //contatore di uscita dal gulag in assenza di altri giocatori

    private Settings() {
        this.fc = GulagPlugin.current_instance.getConfig();
        fc.options().copyDefaults(true);
        initDefaults();
        reloadConfig();
    }

    /**
     * Restituisce l'unica instanza delle impostazioni
     *
     * @return
     */
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Inizializza i valori di default delle impostazioni
     */
    public void initDefaults() {
        fc.addDefault(STT_GULAG_LOCATION, new Location(GulagPlugin.current_instance.getServer().getWorld("world"), 0, 100, 0));
        fc.addDefault(STT_EXIT_GULAG_LOCATION, new Location(GulagPlugin.current_instance.getServer().getWorld("world"), 0, 100, 50));
        fc.addDefault(STT_GULAG_FIGHT_LOCATION_1, new Location(GulagPlugin.current_instance.getServer().getWorld("world"), 10, 100, 0));
        fc.addDefault(STT_GULAG_FIGHT_LOCATION_2, new Location(GulagPlugin.current_instance.getServer().getWorld("world"), -10, 100, 0));
        fc.addDefault(STT_GULAG_MATCH_COUNTDOWN, 5);
        fc.addDefault(STT_GULAG_LONELY_COUNTDOWN, 120L);
    }

    /**
     * Salva le impostazioni
     */
    public void saveConfig() {
        fc.set(STT_GULAG_FIGHT_LOCATION_1, fight_locations[0]);
        fc.set(STT_GULAG_FIGHT_LOCATION_2, fight_locations[1]);
        fc.set(STT_GULAG_LOCATION, gulag_location);
        fc.set(STT_EXIT_GULAG_LOCATION, exit_gulag_location);
        fc.set(STT_GULAG_MATCH_COUNTDOWN, match_countdown);
        fc.set(STT_GULAG_LONELY_COUNTDOWN, lonely_countdown);
        GulagPlugin.current_instance.saveConfig();
    }

    /**
     * Ricarica le impostazioni
     */
    public void reloadConfig() {
        GulagPlugin.current_instance.reloadConfig();
        this.fc = GulagPlugin.current_instance.getConfig();
        this.fight_locations = new Location[]{
                fc.getLocation(STT_GULAG_FIGHT_LOCATION_1),
                fc.getLocation(STT_GULAG_FIGHT_LOCATION_2)
        };
        this.gulag_location = fc.getLocation(STT_GULAG_LOCATION);
        this.exit_gulag_location = fc.getLocation(STT_EXIT_GULAG_LOCATION);
        this.match_countdown = fc.getInt(STT_GULAG_MATCH_COUNTDOWN);
        this.lonely_countdown = fc.getInt(STT_GULAG_LONELY_COUNTDOWN);
    }

    /**
     * Restituisce la posizione dello spawn del Gulag impostata precedentemente
     *
     * @return
     */
    public Location getGulagLocation() {
        return this.gulag_location;
    }

    /**
     * Imposta la posizione dello spawn del Gulag
     * @param gulag_location
     */
    public void setGulagLocation(Location gulag_location) {
        this.gulag_location = gulag_location;
    }

    /**
     * Restituisce la posizione di uscita dal Gulag
     *
     * @return
     */
    public Location getGulagExitLocation() {
        return this.exit_gulag_location;
    }

    /**
     * Restituisce le posizione di battaglia dei due giocatori
     *
     * @return
     */
    public Location[] getGulagFightLocations() {
        return this.fight_locations;
    }

    /**
     * Restituisce il tempo in secondi del contatore di inizio match
     *
     * @return
     */
    public int getMatchCountdown() {
        return match_countdown;
    }

    /**
     * Imposta il tempo in secondi del contatore di inizio match
     * @param match_countdown Tempo in secondi
     */
    public void setMatchCountdown(int match_countdown) {
        this.match_countdown = match_countdown;
    }

    /**
     * Restituisce il tempo in secondi del contatore di uscita del giocatore dal gulag in assenza di altri giocatori
     *
     * @return
     */
    public int getLonelyCountdown() {
        return lonely_countdown;
    }

    /**
     * Imposta il tempo in secondi del contatore di uscita del giocatore dal gulag in assenza di altri giocatori
     * @param lonely_countdown
     */
    public void setLonelyCountdown(int lonely_countdown) {
        this.lonely_countdown = lonely_countdown;
    }

    /**
     * Imposta  la posizione di uscita dal Gulag
     * @param exit_gulag_location
     */
    public void setExitGulagLocation(Location exit_gulag_location) {
        this.exit_gulag_location = exit_gulag_location;
    }

    /**
     * Imposta la posizione di battaglia del giocatore 1
     * @param fight_locations
     */
    public void setFightLocationP1(Location fight_locations) {
        this.fight_locations[0] = fight_locations;
    }

    /**
     * Imposta la posizione di battaglia del giocatore 2
     * @param fight_locations
     */
    public void setFightLocationP2(Location fight_locations) {
        this.fight_locations[1] = fight_locations;
    }
}
