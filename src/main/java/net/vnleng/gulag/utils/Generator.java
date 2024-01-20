package net.vnleng.gulag.utils;

/**
 * Utilizzata per creare nuove instanze di Task/Runnable
 * @param <T> Classe da instanziare
 */
public interface Generator<T> {

    /**
     * Genera una nuova instanza.
     * @return Instanza creata
     */
    public T generateInstance();
}
