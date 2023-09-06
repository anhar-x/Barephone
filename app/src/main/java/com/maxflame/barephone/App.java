package com.maxflame.barephone;

public class App {
    public String name;
    public String packageName;

    public App(final String name,final String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return this.name;
    }
}