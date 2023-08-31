package dindcrzy.starcana;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;

public interface IClientData {
    HashSet<Identifier> starcana$getFoundConstellations();
    HashMap<Identifier, Float> starcana$getConstellationVisibility();
}
