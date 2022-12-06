package com.lordnoisy.hoobabot;

import java.util.Objects;

/**
 * Coordinate class
 */
public class Coordinate {
    private int x;
    private int y;
    private int hashCode;

    /**
     * Creates some coordinates
     * @param x the x coord
     * @param y the y coord
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.hashCode = Objects.hash(x, y);
    }

    public static Coordinate stringToCoordinate(String coordinateString) {
        int xValue = coordinateString.charAt(0) - '0';
        int yValue = coordinateString.charAt(1) - 64;
        System.out.println("THE COORDS ARE: " + xValue + " , " + yValue);

        return new Coordinate(xValue, yValue);
    }

    @Override
    public boolean equals(Object comparison) {
        if (comparison == null || getClass() != comparison.getClass())
            return false;
        Coordinate comparisonCoordinate = (Coordinate) comparison;
        return this.getX() == comparisonCoordinate.getX() && this.getY() == comparisonCoordinate.getY();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
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
