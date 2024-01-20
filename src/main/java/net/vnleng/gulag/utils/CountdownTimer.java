package net.vnleng.gulag.utils;

import net.vnleng.gulag.GulagPlugin;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

/**
 * Rappresenta un timer: è una task che viene chiamata ripetutamente dallo scheduler ogni secondo per un tempo specificato.
 * Permette di avere un'azione eseguita ad inizio del timer, a fine timer e una chiamata ad ogni secondo
 */
public class CountdownTimer implements Runnable {

    private final int seconds;
    private final Consumer<CountdownTimer> step;
    private final Runnable onStart;
    private final Runnable onEnd;
    private int secondsLeft;
    private Integer taskID;

    /**
     * Inizializza un nuovo CountdownTimer
     *
     * @param seconds  Secondi da contare
     * @param _onStart Runnable da eseguire a inizio Timer
     * @param _onEnd   Runnable da eseguire a fine Timer
     * @param _step    Consumer da azionare ad ogni secondo
     */
    public CountdownTimer(int seconds, Runnable _onStart, Runnable _onEnd, Consumer<CountdownTimer> _step) {
        this.seconds = seconds;
        this.secondsLeft = seconds;
        this.onStart = _onStart;
        this.onEnd = _onEnd;
        this.step = _step;
    }

    /**
     * Ad ogni secondo viene chiamata e decrementa i secondi rimasti.
     * Quando i secondi sono terminati elimina la task dallo scheduler.
     */
    @Override
    public void run() {
        if (secondsLeft < 1) {
            onEnd.run();
            if (taskID != null) {
                Bukkit.getScheduler().cancelTask(taskID);
            }
            return;
        } else if (secondsLeft == seconds) {
            onStart.run();
        }
        step.accept(this);
        secondsLeft--;
    }

    /**
     * Restituisce i secondi rimanenti
     *
     * @return i secondi rimanenti.
     */
    public int getSecondsLeft() {
        return secondsLeft;
    }

    /**
     * Imposta il timer come Task sincrona ripetuta ogni secondo.
     */
    public void scheduleTimer() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                GulagPlugin.current_instance,
                this,
                0L,
                20L);
    }


}
