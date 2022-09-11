package dev.totallynotmark6.totallynotalib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.totallynotmark6.totallynotalib.goodies.ContributorRewards;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class PatronCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                Commands.literal("patron")
                        .then(
                            Commands.argument("player", EntityArgument.player())
                                    .executes(PatronCommand::executeElse)
                        )
                        .then(
                                Commands.literal("pretend")
                                        .then(
                                                Commands.argument("level", IntegerArgumentType.integer(0, 99))
                                                        .executes(PatronCommand::pretend)
                                        )
                        )
                        .executes(PatronCommand::executeSelf)
        );
    }

    private static int pretend(CommandContext<CommandSourceStack> command) {
        Integer level = IntegerArgumentType.getInteger(command, "level");
        ContributorRewards.localPatronTier = level;
        command.getSource().sendSuccess(new TextComponent("...please donate."), true);
        return 0;
    }

    private static int executeElse(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(command, "player");
        if(command.getSource().getEntity() instanceof Player){
            command.getSource().getEntity().sendMessage(new TextComponent(String.valueOf(ContributorRewards.getTier(player))), Util.NIL_UUID);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSelf(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof Player){
            Player player = (Player) command.getSource().getEntity();
            player.sendMessage(new TextComponent(String.valueOf(ContributorRewards.localPatronTier)), Util.NIL_UUID);
        }
        return Command.SINGLE_SUCCESS;
    }
}
