package com.xiaofan0408;

public interface Server {

    /**
     * Keeps the server waiting for requests.
     */
    public void listen();

    /**
     * Starts the server.
     */
    public void start();

    /**
     * Stops the server.
     */
    public void stop();

}
