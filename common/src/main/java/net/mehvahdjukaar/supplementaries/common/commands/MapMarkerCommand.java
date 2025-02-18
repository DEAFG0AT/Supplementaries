package net.mehvahdjukaar.supplementaries.common.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Objects;
import java.util.Optional;


public class MapMarkerCommand {
    private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.structure.invalid", new Object[]{object}));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands.literal("add_marker")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("marker", ResourceArgument.resource(context, MapDecorationRegistry.KEY))
                        .executes(MapMarkerCommand::addMapMarker)
                );
    }


    public static int addMapMarker(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        var decoration = ResourceArgument.getResource(context, "marker", MapDecorationRegistry.KEY)
                .value();
        var p = source.getPlayer();
        if (p != null) {
            ItemStack stack = p.getMainHandItem();
            var data = MapHelper.getMapData(stack, level, p);
            if (data != null) {
                MapHelper.addDecorationToMap(stack, p.getOnPos(), decoration, 0);
            }
        }
        return 0;
    }
}