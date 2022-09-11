package dev.totallynotmark6.totallynotalib.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.stream.IntStream;

/**
 * Some functions to help with editing large amounts of blocks
 *
 * @author totallynotmark6
 * @since 1.0.0
 */
public class BuildingHelper {
    /**
     * Builds a solid cube, similar to the fill command.
     * @param world The world
     * @param start The first position
     * @param end The second position
     * @param block The block to fill
     * @param flags The flags passed to setBlock
     */
    public static void buildCube(Level world, BlockPos start, BlockPos end, Block block, int flags) {
        IntStream.rangeClosed(start.getY(), end.getY()).forEach(y -> {
            IntStream.rangeClosed(start.getX(), end.getX()).forEach(x -> {
                IntStream.rangeClosed(start.getZ(), end.getZ()).forEach(z -> {
                    world.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), flags);
                });
            });
        });
    }

    /**
     * NOT YET IMPLEMENTED
     * @hidden
     * @param world
     * @param start
     * @param end
     * @param block
     */
    public static void buildHollowCube(Level world, BlockPos start, BlockPos end, Block block) {}
    /**
     * NOT YET IMPLEMENTED
     * @hidden
     * @param world
     * @param start
     * @param end
     * @param block
     */
    public static void buildCubeFrame(Level world, BlockPos start, BlockPos end, Block block) {}
}
