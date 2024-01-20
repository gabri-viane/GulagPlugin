package net.vnleng.gulag.events;

public interface GulagCountdownListener {
    public void onCountdownStarted(int total_senconds);

    public void onCountdownFinished();

    public void onCountdownStep(int left_seconds);
}
