package dindcrzy.starcana;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;

public interface IPlayerData {
    HashSet<Identifier> getFoundConstellations();
}
