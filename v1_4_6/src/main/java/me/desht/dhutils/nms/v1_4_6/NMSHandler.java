package me.desht.dhutils.nms.v1_4_6;

import net.minecraft.server.v1_4_6.Block;
import net.minecraft.server.v1_4_6.Chunk;
import net.minecraft.server.v1_4_6.ChunkCoordIntPair;
import net.minecraft.server.v1_4_6.EnumSkyBlock;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.desht.dhutils.nms.api.NMSAbstraction;
import org.bukkit.util.Vector;

public class NMSHandler implements NMSAbstraction {

	@Override
	public boolean setBlockFast(World world, int x, int y, int z, int blockId, byte data) {
		net.minecraft.server.v1_4_6.World w = ((CraftWorld) world).getHandle();
		Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
		return chunk.a(x & 0x0f, y, z & 0x0f, blockId, data);
	}

	@Override
	public void forceBlockLightLevel(World world, int x, int y, int z, int level) {
		net.minecraft.server.v1_4_6.World w = ((CraftWorld) world).getHandle();
		w.b(EnumSkyBlock.BLOCK, x, y, z, level);
	}

	@Override
	public int getBlockLightEmission(int blockId) {
		return Block.lightEmission[blockId];
	}

	@Override
	public int getBlockLightBlocking(int blockId) {
		return Block.lightBlock[blockId];
	}

	@SuppressWarnings("unchecked")
	@Override
	public void queueChunkForUpdate(Player player, int cx, int cz) {
		((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(new ChunkCoordIntPair(cx, cz));
	}

	@Override
	public void recalculateBlockLighting(World world, int x, int y, int z) {
		net.minecraft.server.v1_4_6.World w = ((CraftWorld) world).getHandle();
		w.z(x, y, z);
	}

	@Override
	public Vector[] getBlockHitbox(org.bukkit.block.Block block) {
		throw new UnsupportedOperationException();
	}

}
