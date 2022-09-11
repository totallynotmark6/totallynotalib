package dev.totallynotmark6.totallynotalib;

import com.mojang.logging.LogUtils;
import dev.totallynotmark6.totallynotalib.commands.ChunkCommand;
import dev.totallynotmark6.totallynotalib.commands.PatronCommand;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("totallynotalib")
public class totallynotalib {

    public static final Logger LOGGER = LogUtils.getLogger();

    public totallynotalib() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        PatronCommand.register(event.getDispatcher());
        ChunkCommand.register(event.getDispatcher());
    }
}
