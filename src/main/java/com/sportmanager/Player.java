package com.sportmanager;

public abstract class Player {
    protected String name;
    protected int age;
    protected String position;
    protected int fitness;
    protected int injuredMatches;

    public Player(String name, int age, String position, int fitness) {
        this.name = name;
        this.age = age;
        this.position = position;
        this.fitness = fitness;
        this.injuredMatches = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getInjuredMatches() {
        return injuredMatches;
    }

    public void setInjuredMatches(int injuredMatches) {
        this.injuredMatches = injuredMatches;
    }

    public boolean isAvailable(){
        return injuredMatches==0;
    }

    public void train(){
        fitness += 5;
    }

    public void recover(){
        if (injuredMatches>0){
            injuredMatches--;
        }
    }
}
