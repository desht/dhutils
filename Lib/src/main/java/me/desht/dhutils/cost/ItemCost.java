package me.desht.dhutils.cost;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.desht.dhutils.MiscUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCost extends Cost {
	private final Material material;
	private final short data;
	private boolean itemsDropped;

	public ItemCost(Material mat, double quantity) {
        this(mat, (short)0, quantity);
	}

	public ItemCost(Material mat, short data, double quantity) {
		super(quantity);
		this.material = mat;
		this.data = data;
	}

	public Material getMaterial() {
		return material;
	}

	public short getData() {
		return data;
	}

	public boolean isItemsDropped() {
		return itemsDropped;
	}

	@Override
	public String getDescription() {
		String desc = (int) getQuantity() + " " + getMaterial().toString().toLowerCase().replace("_", " ");
		if (getData() != 0) {
			desc += ":" + getData();
		}
		return desc;
	}

	@Override
	public boolean isAffordable(Player player) {
		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(getMaterial());
		int remainingCheck = (int) getQuantity();
		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			if (entry.getValue().getData() == null || entry.getValue().getData().getData() == getData()) {
				remainingCheck -= entry.getValue().getAmount();
				if (remainingCheck <= 0)
					break;
			}
		}
		return remainingCheck <= 0;
	}

	@Override
	public void apply(Player player) {
        if (getQuantity() > 0) {
            chargeItems(player);
        } else {
            grantItems(player);
        }
        player.updateInventory();
	}

	private void grantItems(Player player) {
		if (player == null) {
			return;
		}

		int maxStackSize = player.getInventory().getMaxStackSize();
		int quantity = (int) -getQuantity();
		int dropped = 0;

		while (quantity > maxStackSize) {
			dropped += addItems(player, maxStackSize);
			quantity -= maxStackSize;
		}
		dropped += addItems(player, quantity);

		itemsDropped = dropped > 0;
	}

	private void chargeItems(Player player) {
		if (player == null) {
			return;
		}

		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(getMaterial());

		int remainingCheck = (int) getQuantity();
		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			if (entry.getValue().getData() == null || entry.getValue().getData().getData() == getData()) {
				remainingCheck -= entry.getValue().getAmount();
				if (remainingCheck < 0) {
					entry.getValue().setAmount(-remainingCheck);
					break;
				} else if (remainingCheck == 0) {
					player.getInventory().removeItem(entry.getValue());
					break;
				} else {
					player.getInventory().removeItem(entry.getValue());
				}
			}
		}
	}

	private ItemStack makeStack(int quantity) {
		return new ItemStack(getMaterial(), quantity, getData());
	}

	private int addItems(Player player, int quantity) {
		Map<Integer, ItemStack> toDrop = player.getInventory().addItem(makeStack(quantity));
		if (toDrop.size() == 0) {
			return 0;
		}

		int dropped = 0;
		for (ItemStack is : toDrop.values()) {
			player.getWorld().dropItemNaturally(player.getLocation(), is);
			dropped += is.getAmount();
		}
		return dropped;
	}
}
