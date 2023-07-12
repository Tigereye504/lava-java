package net.tigereye.lavajava.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.tigereye.lavajava.LavaJava;

@Config(name = LavaJava.MODID)
public class LJConfig implements ConfigData {
    public int TIME_UNTIL_STALE = 6000;


    public int CAFE_REFRESH_PERIOD = 3200;
    public int MAX_BARISTAS_PER_CAFE = 2;
    public float DURATION_LOST_PER_STAGE = .2f;
}
