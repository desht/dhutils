package me.desht.dhutils.nms.v1_7_R1;

import me.desht.dhutils.nms.api.NMSAbstraction;
import net.minecraft.server.v1_7_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class NMSHandler implements NMSAbstraction {

	@Override
	public boolean setBlockFast(World world, int x, int y, int z, int blockId, byte data) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld) world).getHandle();
		Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
		return a(chunk, x & 0x0f, y, z & 0x0f, Block.e(blockId), data);
	}

    private boolean a(Chunk that, int i, int j, int k, Block block, int l) {
        Block block1 = that.getType(i, j, k);
        int k1 = that.getData(i, j, k);

        if (block1 == block && k1 == l) {
            return false;
        } else {
            ChunkSection chunksection = ((ChunkSection[])ReflectionUtil.getProtectedValue(that, "sections"))[j >> 4];

            if (chunksection == null) {
                if (block == Blocks.AIR) {
                    return false;
                }

                chunksection = ((ChunkSection[])ReflectionUtil.getProtectedValue(that, "sections"))[j >> 4] = new ChunkSection(j >> 4 << 4, !that.world.worldProvider.g);
            }

            int l1 = that.locX * 16 + i;
            int i2 = that.locZ * 16 + k;

            if (!that.world.isStatic) {
                block1.f(that.world, l1, j, i2, k1);
            }

            // CraftBukkit start - Delay removing containers until after they're cleaned up
            if (!(block1 instanceof IContainer)) {
                chunksection.setTypeId(i, j & 15, k, block);
            }
            // CraftBukkit end

            if (!that.world.isStatic) {
                block1.remove(that.world, l1, j, i2, block1, k1);
            } else if (block1 instanceof IContainer && block1 != block) {
                that.world.p(l1, j, i2);
            }

            // CraftBukkit start - Remove containers now after cleanup
            if (block1 instanceof IContainer) {
                chunksection.setTypeId(i, j & 15, k, block);
            }
            // CraftBukkit end

            if (chunksection.getTypeId(i, j & 15, k) != block) {
                return false;
            } else {
                chunksection.setData(i, j & 15, k, l);

                TileEntity tileentity;

                if (block1 instanceof IContainer) {
                    tileentity = that.e(i, j, k);
                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                // CraftBukkit - Don't place while processing the BlockPlaceEvent, unless it's a BlockContainer
                if (!that.world.isStatic && (!that.world.callingPlaceEvent || (block instanceof BlockContainer))) {
                    block.onPlace(that.world, l1, j, i2);
                }

                if (block instanceof IContainer) {
                    // CraftBukkit start - Don't create tile entity if placement failed
                    if (that.getType(i, j, k) != block) {
                        return false;
                    }
                    // CraftBukkit end

                    tileentity = that.e(i, j, k);
                    if (tileentity == null) {
                        tileentity = ((IContainer) block).a(that.world, l);
                        that.world.setTileEntity(l1, j, i2, tileentity);
                    }

                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                that.n = true;
                return true;
            }
        }
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
        Chunk chunk = ((CraftChunk)world.getChunkAt(x >> 4, z >> 4)).getHandle();
        int i = x & 0x0f;
        int j = y;
        int k = z & 0x0f;

        int i1 = k << 4 | i;
        if (j >= chunk.b[i1] - 1) {
            chunk.b[i1] = -999;
        }

        int maxY = chunk.heightMap[i1];

        chunk.initLighting();

        Block block = chunk.getType(i, j, k);
        int j2 = block.k();

        if (j2 > 0) {
            if (j >= maxY) {
                ReflectionUtil.invokeProtectedMethod(chunk, "h", i, j + 1, k);
            }
        } else if (j == maxY - 1) {
            ReflectionUtil.invokeProtectedMethod(chunk, "h",i, j, k);
        }

        if (chunk.getBrightness(EnumSkyBlock.SKY, i, j, k) > 0 || chunk.getBrightness(EnumSkyBlock.BLOCK, i, j, k) > 0) {
            ReflectionUtil.invokeProtectedMethod(chunk, "e", i, k);
        }

		net.minecraft.server.v1_7_R1.World w = ((CraftWorld) world).getHandle();
		w.t(x, y, z);
	}

	@Override
	public Vector[] getBlockHitbox(org.bukkit.block.Block block) {
		net.minecraft.server.v1_7_R1.World w = ((CraftWorld)block.getWorld()).getHandle();
		net.minecraft.server.v1_7_R1.Block b = w.getType(block.getX(), block.getY(), block.getZ());
		b.updateShape(w, block.getX(), block.getY(), block.getZ());
		return new Vector[] {
				new Vector(block.getX() + b.x(), block.getY() + b.z(), block.getZ() + b.B()),
				new Vector(block.getX() + b.y(), block.getY() + b.A(), block.getZ() + b.C())
		};
	}

}
