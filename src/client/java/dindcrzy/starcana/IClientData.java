package dindcrzy.starcana;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface IClientData {
    HashSet<Identifier> getFoundConstellations();
    HashMap<Identifier, Float> getConstellationVisibility();
}
