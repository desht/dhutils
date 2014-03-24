package me.desht.dhutils.cost;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.desht.dhutils.MiscUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCost extends Cost {
	private final ItemStack toMatch;
	private final boolean matchData;
	private final boolean matchMeta;
	private int dropped;

	public ItemCost(Material mat, double quantity) {
		super(quantity);
		this.matchData = false;
		this.matchMeta = false;
		this.toMatch = new ItemStack(mat, (int) quantity, (short) 0);
	}

	public ItemCost(Material mat, short data, double quantity) {
		super(quantity);
		this.matchData = true;
		this.matchMeta = false;
		this.toMatch = new ItemStack(mat, (int) quantity, data);
	}

	public ItemCost(ItemStack stack) {
		super(stack.getAmount());
		this.matchData = true;
		this.matchMeta = true;
		this.toMatch = stack.clone();
	}

	public Material getMaterial() {
		return toMatch.getType();
	}

	public short getData() {
		return toMatch.getData().getData();
	}

	public boolean isItemsDropped() {
		return dropped > 0;
	}

	public int getItemDropCount() {
		return dropped;
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
			if (matches(entry.getValue())) {
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

		dropped = 0;
		while (quantity > maxStackSize) {
			dropped += addItems(player, maxStackSize);
			quantity -= maxStackSize;
		}
		dropped += addItems(player, quantity);
	}

	private void chargeItems(Player player) {
		if (player == null) {
			return;
		}

		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(getMaterial());

		int remainingCheck = (int) getQuantity();
		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			if (matches(entry.getValue())) {
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

	private boolean matches(ItemStack stack) {
		if (toMatch.getType() != stack.getType()) {
			return false;
		} else if (matchData && toMatch.getData().getData() != stack.getData().getData()) {
			return false;
		} else if (matchMeta) {
			String d1 = stack.hasItemMeta() ? stack.getItemMeta().getDisplayName() : null;
			String d2 = toMatch.hasItemMeta() ? toMatch.getItemMeta().getDisplayName() : null;
			if (d1 != null && !d1.equals(d2)) {
				return false;
			} else if (d2 != null && !d2.equals(d1)) {
				return false;
			}
			return true;
		} else {
			return true;
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
