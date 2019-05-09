package com.minefit.xerxestireiron.farlandsagain.v1_14_R1;

public class PaperSpigot {
    public final boolean generateCanyon;
    public final boolean generateCaves;
    public final boolean generateDungeon;
    public final boolean generateFortress;
    public final boolean generateMineshaft;
    public final boolean generateMonument;
    public final boolean generateStronghold;
    public final boolean generateTemple;
    public final boolean generateVillage;
    public final boolean generateFlatBedrock;

    public PaperSpigot(String worldName, boolean isPaper) {
        this.generateCanyon = true;
        this.generateCaves = true;
        this.generateDungeon = true;
        this.generateFortress = true;
        this.generateMineshaft = true;
        this.generateMonument = true;
        this.generateStronghold = true;
        this.generateTemple = true;
        this.generateVillage = true;
        this.generateFlatBedrock = false;
    }
}