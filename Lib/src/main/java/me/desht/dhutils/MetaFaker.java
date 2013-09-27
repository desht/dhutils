package me.desht.dhutils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;

public class MetaFaker {
	private final Plugin plugin;
	private final ProtocolManager protocolManager;
	private final MetadataFilter filter;
	private final PacketListener listener;

	public MetaFaker(Plugin plugin, MetadataFilter filter) {
		this.plugin = plugin;
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		this.filter = filter;
		this.listener = registerListener();
	}

	public void shutdown() {
		protocolManager.removePacketListener(listener);
	}

	private PacketListener registerListener() {
		PacketListener l = new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, 0x67, 0x68) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				Player player = event.getPlayer();
				switch (packet.getID()) {
				case 0x67:
					StructureModifier<ItemStack> sm = packet.getItemModifier();
					for (int i = 0; i < sm.size(); i++) {
						filterMetaData(sm.read(i), player);
					}
					break;
				case 0x68:
					StructureModifier<ItemStack[]> smArray = packet.getItemArrayModifier();
                    for (int i = 0; i < smArray.size(); i++) {
                    	ItemStack[] stacks = smArray.read(i);
                    	for (int n = 0; n < stacks.length; n++) {
                    		filterMetaData(stacks[n], player);
                        }
                    }
                    break;
				default:
					break;
				}
			}
		};
		protocolManager.addPacketListener(l);
		return l;
	}

	private void filterMetaData(ItemStack stack, Player player) {
		if (stack != null) {
			ItemMeta newMeta = filter.filter(stack.getItemMeta(), player);
			if (newMeta != null) {
				stack.setItemMeta(newMeta);
			}
		}
	}

	public interface MetadataFilter {
		public ItemMeta filter(ItemMeta itemMeta, Player player);
	}
}
