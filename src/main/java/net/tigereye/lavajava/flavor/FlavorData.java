package net.tigereye.lavajava.flavor;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlavorData {
    public Identifier statusID;
    public boolean isDrawback;
    public int duration;
    public int magnitude;
    public int weight;
    public int value;
    public int namePriority;
    public List<Identifier> exclusions;
    //TODO: implement exclusions in FlavorSerializer
    //TODO: check for exclusions when generating Lava Javas, re-roll if found
}
