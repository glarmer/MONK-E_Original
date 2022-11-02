package com.lordnoisy.hoobabot;

/**
 * Coordinates class
 */
public class Coordinates {
    private int x;
    private int y;

    /**
     * Creates some coordinates
     * @param x the x coord
     * @param y the y coord
     */
    public Coordinates (int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get x coord
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     * Get y coord
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * Set x coord
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Set y coord
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }
}
