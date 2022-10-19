package com.lordnoisy.hoobabot;

public class RandomNumberGen {
    //TODO: CLEARLY THIS NEEDS AN ENTIRE CLASS?
    public static int getRandomNumber(int min, int max){
        return (int)(Math.random() * (max - min + 1) + min);
    }
}
