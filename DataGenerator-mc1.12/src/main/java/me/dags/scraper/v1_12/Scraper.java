package me.dags.scraper.v1_12;

import me.dags.converter.data.GameDataWriter;
import me.dags.converter.data.Schema;
import me.dags.converter.data.SectionWriter;
import me.dags.converter.data.biome.BiomeData;
import me.dags.converter.data.block.BlockData;
import me.dags.converter.data.block.StateData;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Mod(modid = "data_generator")
public class Scraper {

    public Scraper() {
        MinecraftForge.EVENT_BUS.register(Scraper.class);
    }

    @SubscribeEvent
    public static void load(WorldEvent.Load event) {
        File dir = event.getWorld().getSaveHandler().getWorldDirectory();
        File out = new File(dir, "game_data.json");
        Schema schema = Schema.forVersion("1.12");
        try (GameDataWriter writer = new GameDataWriter(schema, out)) {
            try (SectionWriter<BlockData> section = writer.startBlocks()) {
                for (Block block : ForgeRegistries.BLOCKS) {
                    section.write(getBlockData(block));
                }
            }
            try (SectionWriter<BiomeData> section = writer.startBiomes()) {
                for (Biome biome : ForgeRegistries.BIOMES) {
                    section.write(geBiomeData(biome));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }

        RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
        if (result == null) {
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = result.getBlockPos();
            IBlockState state = Minecraft.getMinecraft().world.getBlockState(pos);
            int id = Block.getIdFromBlock(state.getBlock());
            int meta = state.getBlock().getMetaFromState(state);
            Minecraft.getMinecraft().ingameGUI.setOverlayMessage(String.format("%s [%s:%s]", state, id, meta), false);
        }
    }

    private static BiomeData geBiomeData(Biome biome) {
        return new BiomeData(biome.getRegistryName(), Biome.getIdForBiome(biome));
    }

    private static BlockData getBlockData(Block block) {
        Object name = block.getRegistryName();
        int blockId = Block.getIdFromBlock(block);
        boolean upgrade = hasTransientProperties(block);
        StateData defaults = getStateData(block.getDefaultState());
        List<StateData> states = new LinkedList<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            states.add(getStateData(state));
        }
        return new BlockData(name, blockId, upgrade, defaults, states);
    }

    private static StateData getStateData(IBlockState state) {
        return new StateData(propertyString(state), state.getBlock().getMetaFromState(state));
    }

    private static String propertyString(IBlockState state) {
        StringBuilder sb = new StringBuilder();
        state.getPropertyKeys().stream().sorted(Comparator.comparing(IProperty::getName)).forEach(p -> {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p.getName()).append('=').append(state.getValue(p).toString().toLowerCase());
        });
        return sb.toString();
    }

    private static boolean hasTransientProperties(Block block) {
        if (block.getBlockState().getProperty("snowy") != null) {
            return false;
        }
        int[] metas = new int[16];
        for (IBlockState state : block.getBlockState().getValidStates()) {
            int meta = block.getMetaFromState(state);
            metas[meta]++;
        }
        for (int i : metas) {
            if (i > 1) {
                return true;
            }
        }
        return false;
    }
}