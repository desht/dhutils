package me.desht.dhutils.nms.v1_7_R1;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Chunk;
import net.minecraft.server.v1_7_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_7_R1.EnumSkyBlock;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.desht.dhutils.nms.api.NMSAbstraction;

public class NMSHandler implements NMSAbstraction {

	@Override
	public boolean setBlockFast(World world, int x, int y, int z, int blockId, byte data) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld) world).getHandle();
		Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
		return chunk.a(x & 0x0f, y, z & 0x0f, Block.e(blockId), data);
	}

	@Override
	public void forceBlockLightLevel(World world, int x, int y, int z, int level) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld) world).getHandle();
		w.b(EnumSkyBlock.BLOCK, x, y, z, level);
	}

	@Override
	public int getBlockLightEmission(int blockId) {
		return Block.e(blockId).m();
	}

	@Override
	public int getBlockLightBlocking(int blockId) {
		return Block.e(blockId).k();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void queueChunkForUpdate(Player player, int cx, int cz) {
		((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(new ChunkCoordIntPair(cx, cz));
	}

	@Override
	public void recalculateBlockLighting(World world, int x, int y, int z) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld) world).getHandle();
		w.t(x, y, z);
	}
}
