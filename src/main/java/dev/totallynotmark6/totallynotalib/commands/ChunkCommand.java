package dev.totallynotmark6.totallynotalib.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Unit;
import dev.totallynotmark6.totallynotalib.building.BuildingHelper;
import dev.totallynotmark6.totallynotalib.totallynotalib;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                Commands.literal("chunk")
                        .requires(c -> c.hasPermission(2))
                        .then(
                                Commands.argument("radius", IntegerArgumentType.integer(0, 32))
                                        .then(
                                                Commands.literal("regen")
                                                        .executes(ChunkCommand::regen)
                                        )
                                        .then(
                                                Commands.literal("analyze")
                                                        .executes(ChunkCommand::analyze)
                                        )
                                        .then(
                                                Commands.literal("clear")
                                                        .executes(ChunkCommand::clear)
                                        )
                                        .then(
                                                Commands.literal("light")
                                                        .executes(ChunkCommand::light)
                                        )
                        )
        );
    }
    private static int analyze(CommandContext<CommandSourceStack> command) {
        // Analyzes the chunk, printing out stats
        return 0;
    }

    private static int clear(CommandContext<CommandSourceStack> command) {
        // Levels a chunk to the void, except for a layer of glass.
        Integer radius = IntegerArgumentType.getInteger(command, "radius");
        Vec3 vec3 = command.getSource().getPosition();

        ChunkPos chunkpos = new ChunkPos(new BlockPos(vec3));
        int i = chunkpos.z - radius;
        int j = chunkpos.z + radius;
        int k = chunkpos.x - radius;
        int l = chunkpos.x + radius;
        ServerLevel level = command.getSource().getLevel();

        for(int i1 = i; i1 <= j; ++i1) {
            for(int j1 = k; j1 <= l; ++j1) {
                ChunkPos chunkpos1 = new ChunkPos(j1, i1);
                BuildingHelper.buildCube(level, new BlockPos(chunkpos1.getMinBlockX(), level.getMinBuildHeight(), chunkpos1.getMinBlockZ()), new BlockPos(chunkpos1.getMaxBlockX(), level.getMaxBuildHeight(), chunkpos1.getMaxBlockZ()), Blocks.AIR, 3);
            }
        }
        return 0;
    }

    private static int light(CommandContext<CommandSourceStack> command) {
        // Automatically lights up the chunk so that no mobs can spawn.
        return 0;
    }

    private static int regen(CommandContext<CommandSourceStack> command) {
        // Regenerates the chunk
        
            ServerLevel serverlevel = command.getSource().getLevel();
        Integer radius = IntegerArgumentType.getInteger(command, "radius");
        boolean pSkipOldChunks = false;
            ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
            serverchunkcache.chunkMap.debugReloadGenerator();
            Vec3 vec3 = command.getSource().getPosition();
            ChunkPos chunkpos = new ChunkPos(new BlockPos(vec3));
            int i = chunkpos.z - radius;
            int j = chunkpos.z + radius;
            int k = chunkpos.x - radius;
            int l = chunkpos.x + radius;

            for(int i1 = i; i1 <= j; ++i1) {
                for(int j1 = k; j1 <= l; ++j1) {
                    ChunkPos chunkpos1 = new ChunkPos(j1, i1);
                    LevelChunk levelchunk = serverchunkcache.getChunk(j1, i1, false);
                    if (levelchunk != null && (!pSkipOldChunks || !levelchunk.isOldNoiseGeneration())) {
                            BuildingHelper.buildCube(serverlevel, new BlockPos(chunkpos1.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos1.getMinBlockZ()), new BlockPos(chunkpos1.getMaxBlockX(), serverlevel.getMaxBuildHeight(), chunkpos1.getMaxBlockZ()), Blocks.AIR, 16);
                    }
                }
            }
            ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
            long j3 = System.currentTimeMillis();
            int k3 = (radius * 2 + 1) * (radius * 2 + 1);

            for(ChunkStatus chunkstatus : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES)) {
                long k1 = System.currentTimeMillis();
                CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(() -> {
                    return Unit.INSTANCE;
                }, processormailbox::tell);

                for(int i2 = chunkpos.z - radius; i2 <= chunkpos.z + radius; ++i2) {
                    for(int j2 = chunkpos.x - radius; j2 <= chunkpos.x + radius; ++j2) {
                        ChunkPos chunkpos2 = new ChunkPos(j2, i2);
                        LevelChunk levelchunk1 = serverchunkcache.getChunk(j2, i2, false);
                        if (levelchunk1 != null && (!pSkipOldChunks || !levelchunk1.isOldNoiseGeneration())) {
                            List<ChunkAccess> list = Lists.newArrayList();
                            int k2 = Math.max(1, chunkstatus.getRange());

                            for(int l2 = chunkpos2.z - k2; l2 <= chunkpos2.z + k2; ++l2) {
                                for(int i3 = chunkpos2.x - k2; i3 <= chunkpos2.x + k2; ++i3) {
                                    ChunkAccess chunkaccess = serverchunkcache.getChunk(i3, l2, chunkstatus.getParent(), true);
                                    ChunkAccess chunkaccess1;
                                    if (chunkaccess instanceof ImposterProtoChunk) {
                                        chunkaccess1 = new ImposterProtoChunk(((ImposterProtoChunk)chunkaccess).getWrapped(), true);
                                    } else if (chunkaccess instanceof LevelChunk) {
                                        chunkaccess1 = new ImposterProtoChunk((LevelChunk)chunkaccess, true);
                                    } else {
                                        chunkaccess1 = chunkaccess;
                                    }

                                    list.add(chunkaccess1);
                                }
                            }

                            completablefuture = completablefuture.thenComposeAsync((p_183678_) -> {
                                return chunkstatus.generate(processormailbox::tell, serverlevel, serverchunkcache.getGenerator(), serverlevel.getStructureManager(), serverchunkcache.getLightEngine(), (p_183691_) -> {
                                    throw new UnsupportedOperationException("Not creating full chunks here");
                                }, list, true).thenApply((p_183681_) -> {
                                    if (chunkstatus == ChunkStatus.NOISE) {
                                        p_183681_.left().ifPresent((p_183671_) -> {
                                            Heightmap.primeHeightmaps(p_183671_, ChunkStatus.POST_FEATURES);
                                        });
                                    }

                                    return Unit.INSTANCE;
                                });
                            }, processormailbox::tell);
                        }
                    }
                }

                command.getSource().getServer().managedBlock(completablefuture::isDone);
                totallynotalib.LOGGER.debug(chunkstatus.getName() + " took " + (System.currentTimeMillis() - k1) + " ms");
            }

            long l3 = System.currentTimeMillis();

            for(int i4 = chunkpos.z - radius; i4 <= chunkpos.z + radius; ++i4) {
                for(int l1 = chunkpos.x - radius; l1 <= chunkpos.x + radius; ++l1) {
                    ChunkPos chunkpos3 = new ChunkPos(l1, i4);
                    LevelChunk levelchunk2 = serverchunkcache.getChunk(l1, i4, false);
                    if (levelchunk2 != null && (!pSkipOldChunks || !levelchunk2.isOldNoiseGeneration())) {
                        for(BlockPos blockpos1 : BlockPos.betweenClosed(chunkpos3.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos3.getMinBlockZ(), chunkpos3.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, chunkpos3.getMaxBlockZ())) {
                            serverchunkcache.blockChanged(blockpos1);
                        }
                    }
                }
            }

        totallynotalib.LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - l3) + " ms");
            long j4 = System.currentTimeMillis() - j3;
            command.getSource().sendSuccess(new TextComponent(String.format("%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", k3, j4, k3, (float)j4 / (float)k3)), true);
            return 1;
            
    }
}
