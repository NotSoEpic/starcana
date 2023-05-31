package dindcrzy.starcana.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class StarlightInventory extends BlockEntity {
    public static final String STARLIGHT_KEY = "Starlight";

    public final int capacity;
    private int starlight;
    public StarlightInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        this.capacity = capacity;
        this.starlight = 0;
    }

    public int getStarlight() {
        return starlight;
    }
    public void setStarlight(int v) {
        starlight = MathHelper.clamp(v, 0, capacity);
    }

    public void changeStarlight(int d) {
        setStarlight(getStarlight() + d);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(STARLIGHT_KEY, getStarlight());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setStarlight(nbt.getInt(STARLIGHT_KEY));
    }
}
