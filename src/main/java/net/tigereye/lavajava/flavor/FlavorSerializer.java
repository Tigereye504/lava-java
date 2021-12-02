package net.tigereye.lavajava.flavor;

import com.google.gson.JsonSyntaxException;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class FlavorSerializer {
    public Pair<Identifier, FlavorData> read(Identifier id, FlavorJsonFormat flavorJson) {

        if (flavorJson.flavorID == null) {
            throw new JsonSyntaxException("Flavor entry" + id + " must provide a name");
        }
        if (flavorJson.statusID == null) {
            throw new JsonSyntaxException("Flavor entry" + id + " must provide a status effect");
        }
        //isDrawback will default to false
        //magnitude will probably default to 0, which is ok
        if (flavorJson.duration <= 0) {
            throw new JsonSyntaxException("Flavor entry" + id + " must provide a positive duration");
        }
        if (flavorJson.weight <= 0) {
            throw new JsonSyntaxException("Flavor entry" + id + " must provide a positive weight");
        }
        if (flavorJson.value <= 0) {
            throw new JsonSyntaxException("Flavor entry" + id + " must provide a positive value");
        }

        FlavorData flavorData = new FlavorData();
        Identifier flavorID = new Identifier(flavorJson.flavorID);
        flavorData.statusID = new Identifier(flavorJson.statusID);
        flavorData.isDrawback = flavorJson.isDrawback;
        flavorData.duration = flavorJson.duration;
        flavorData.magnitude = flavorJson.magnitude;
        flavorData.weight = flavorJson.weight;
        flavorData.value = flavorJson.value;
        flavorData.namePriority = flavorJson.namePriority;
        return new Pair<>(flavorID, flavorData);
    }
}
