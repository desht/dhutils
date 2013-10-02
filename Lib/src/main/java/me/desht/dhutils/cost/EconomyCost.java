package me.desht.dhutils.cost;

import me.desht.dhutils.DHUtilsException;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;

public class EconomyCost extends Cost {

	private static Economy economy;

	protected EconomyCost(double quantity) {
		super(quantity);
		if (getEconomy() == null || !getEconomy().isEnabled()) {
			throw new DHUtilsException("Economy costs not available (Vault and/or economy plugin not installed)");
		}
	}

	@Override
	public String getDescription() {
		return getEconomy().format(getQuantity());
	}

	@Override
	public boolean isAffordable(Player player) {
		return getEconomy().has(player.getName(), getQuantity());
	}

	@Override
	public void apply(Player player) {
		EconomyResponse resp;
		if (getQuantity() < 0.0) {
			resp = getEconomy().depositPlayer(player.getName(), -getQuantity());
		} else {
			resp = getEconomy().withdrawPlayer(player.getName(), getQuantity());
		}
		if (!resp.transactionSuccess()) {
			throw new DHUtilsException("Economy problem: " + resp.errorMessage);
		}
	}

	public static void setEconomy(Economy economy) {
		EconomyCost.economy = economy;
	}

	public static Economy getEconomy() {
		return economy;
	}
}
