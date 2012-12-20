package me.desht.dhutils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class SpecialFX {
	public enum EffectType { EXPLOSION, LIGHTNING, EFFECT, SOUND };

	private final ConfigurationSection conf;
	private final Map<String, SpecialEffect> effects;

	private float masterVolume;

	public SpecialFX(ConfigurationSection conf) {
		this.conf = conf;
		effects = new HashMap<String, SpecialFX.SpecialEffect>();
		masterVolume = (float) conf.getDouble("volume", 1.0);
	}

	/**
	 * Play the named effect at the given location
	 * 
	 * @param loc
	 * @param effectName
	 * @throws IllegalArgumentException if the effect name is unknown or its definition is invalid
	 */
	public void playEffect(Location loc, String effectName) {
		SpecialEffect e = getEffect(effectName);
		if (e != null) {
			e.play(loc);
		}
	}

	/**
	 * Get the named effect.
	 * @param effectName	name of the effect
	 * @return the effect
	 * @throws IllegalArgumentException if the effect name is unknown or its definition is invalid
	 */
	public SpecialEffect getEffect(String effectName) {
		if (!effects.containsKey(effectName)) {
			effects.put(effectName, new SpecialEffect(conf.getString(effectName), masterVolume));
		}
		return effects.get(effectName);
	}

	public class SpecialEffect {
		private final EffectType type;
		private final Configuration params = new MemoryConfiguration();
		private final float volumeMult;

		public SpecialEffect(String spec, float volume) {
			this.volumeMult = volume;

			if (spec == null) {
				throw new IllegalArgumentException("null spec not permitted");
			}
			String[] fields = spec.toLowerCase().split(",");
			if (fields[0].startsWith("li")) {
				type = EffectType.LIGHTNING;
			} else if (fields[0].startsWith("ex")) {
				type = EffectType.EXPLOSION;
			} else if (fields[0].startsWith("ef")) {
				type = EffectType.EFFECT;
			} else if (fields[0].startsWith("so")) {
				type = EffectType.SOUND;
			} else {
				throw new IllegalArgumentException("unknown specialfx type: " + fields[0]);
			}

			for (int i = 1; i < fields.length; i++) {
				String[] val = fields[i].split("=");
				if (val.length == 2) {
					params.set(val[0], val[1]);
				} else {
					LogUtils.warning("invalid effect parameter '" + fields[i] + "' - ignored");
				}
			}
		}

		public void play(Location loc) {
			switch (type) {
			case LIGHTNING:
				int lPower = params.getInt("power", 0);
				if (lPower > 0) {
					loc.getWorld().strikeLightning(loc);
				} else {
					loc.getWorld().strikeLightningEffect(loc);
				}
				break;
			case EXPLOSION:
				float ePower = (float) params.getDouble("power", 0.0);
				boolean fire = params.getBoolean("fire", false);
				loc.getWorld().createExplosion(loc, ePower, fire);
				break;
			case EFFECT:
				Effect effect = Effect.valueOf(params.getString("name", "").toUpperCase());
				if (effect != null) {
					int data = params.getInt("data", 0);
					int radius = params.getInt("radius", 64);
					loc.getWorld().playEffect(loc, effect, data, radius);
				} else {
					LogUtils.warning("unknown effect: " + params.getString("name"));
				}
				break;
			case SOUND:
				float volume = (float) params.getDouble("volume", 1.0);
				float pitch = (float) params.getDouble("pitch", 1.0);

				Sound s = Sound.valueOf(params.getString("name", "").toUpperCase());
				if (s != null) {
					loc.getWorld().playSound(loc, s, volume * volumeMult, pitch);
				} else {
					LogUtils.warning("unknown sound effect: " + params.getString("name"));
				}
				break;
			}
		}

	}
}
