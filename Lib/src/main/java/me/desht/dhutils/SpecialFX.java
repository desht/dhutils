package me.desht.dhutils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.desht.dhutils.midi.MidiUtil;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * SpecialFX: map logical effect names to specifications for various effects that can be played in the world.
 * Plugins can use this to play effects for given plugin-defined events, e.g. a game was started/won/lost etc.
 * 
 * @author desht
 */
public class SpecialFX {
	public enum EffectType { EXPLOSION, LIGHTNING, EFFECT, SOUND, FIREWORK, MIDI };

	private final ConfigurationSection conf;
	private final Map<String, SpecialEffect> effects;

	private float masterVolume;

	private static Map<String, Set<String>> validArgs = new HashMap<String, Set<String>>();
	private static void args(EffectType ef, String... valid) {
		validArgs.get(ef.toString()).addAll(Arrays.asList(valid));
	}
	private static boolean isValidArg(EffectType type, String arg) {
		return validArgs.get(type.toString()).contains(arg);
	}
	static {
		for (EffectType ef : EffectType.values()) {
			validArgs.put(ef.toString(), new HashSet<String>());
		}
		args(EffectType.EXPLOSION, "power", "fire" );
		args(EffectType.LIGHTNING, "power");
		args(EffectType.EFFECT, "name", "data", "radius");
		args(EffectType.SOUND, "name", "volume", "pitch");
		args(EffectType.FIREWORK, "type", "color", "fade", "flicker", "trail");
		args(EffectType.MIDI, "file", "tempo");
	}

	/**
	 * Create a SpecialFX object from the given configuration section.  This could be read from the plugin's
	 * config.yml (a common case) or constructed internally by the plugin.
	 *
	 * @param conf configuration object which maps logical (plugin-defined) effect names to the effect specification
	 */
	public SpecialFX(ConfigurationSection conf) {
		this.conf = conf;
		effects = new HashMap<String, SpecialFX.SpecialEffect>();
		masterVolume = (float) conf.getDouble("volume", 1.0);
	}

	/**
	 * Play the named effect at the given location.
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
	 * Get the named effect, creating and caching a SpecialEffect object for it if necessary.
	 * 
	 * @param effectName	name of the effect
	 * @return the effect
	 * @throws IllegalArgumentException if the effect name is unknown or its definition is invalid
	 */
	public SpecialEffect getEffect(String effectName) {
		if (!effects.containsKey(effectName)) {
			try {
				effects.put(effectName, new SpecialEffect(conf.getString(effectName), masterVolume));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("for effect name '" + effectName + "': " + e.getMessage());
			}
		}
		return effects.get(effectName);
	}

	public class SpecialEffect {
		private final EffectType type;
		private final Configuration params = new MemoryConfiguration();
		private final float volumeMult;

		public SpecialEffect(String spec) {
			this(spec, 1.0f);
		}

		public SpecialEffect(String spec, float volume) {
			this.volumeMult = volume;

			if (spec == null) {
				throw new IllegalArgumentException("null spec not permitted (unknown effect name?)");
			}
			String[] fields = spec.split(",");
			type = EffectType.valueOf(fields[0].toUpperCase());

			for (int i = 1; i < fields.length; i++) {
				String[] val = fields[i].split("=", 2);
				if (!isValidArg(type, val[0])) {
					throw new IllegalArgumentException("invalid parameter: " + val[0]);
				}
				if (val.length == 2) {
					params.set(val[0].toLowerCase(), val[1]);
				} else {
					LogUtils.warning("missing value for parameter '" + fields[i] + "' - ignored");
				}
			}
		}

		/**
		 * Validate this effect.
		 *
		 * @throws IllegalArgumentException if any effect parameter is not valid
		 * @throws NumberFormatException if any numeric parameter is misformed
		 */
		public void validate() {
			play(null);
		}

		/**
		 * Play this effect at the given location.  A null location may be passed, in which case no
		 * effect will be played, but validation of the effect specification will still be done.
		 * 
		 * @param loc Location at which to play the effect
		 * @throws IllegalArgumentException if any effect parameter is not valid
		 * @throws NumberFormatException if any numeric parameter is misformed
		 */
		public void play(Location loc) {
			switch (type) {
			case LIGHTNING:
				int lPower = Integer.parseInt(params.getString("power", "0"));
				if (lPower > 0) {
					if (loc != null) loc.getWorld().strikeLightning(loc);
				} else {
					if (loc != null) loc.getWorld().strikeLightningEffect(loc);
				}
				break;
			case EXPLOSION:
				float ePower = Float.parseFloat(params.getString("power", "0.0"));
				boolean fire = Boolean.parseBoolean(params.getString("fire", "false"));
				if (loc != null) loc.getWorld().createExplosion(loc, ePower, fire);
				break;
			case EFFECT:
				String effectName = params.getString("name");
				if (effectName != null && !effectName.isEmpty()) {
					Effect effect = Effect.valueOf(effectName.toUpperCase());
					int data = Integer.parseInt(params.getString("data", "0"));
					int radius = Integer.parseInt(params.getString("radius", "64"));
					if (loc != null) loc.getWorld().playEffect(loc, effect, data, radius);
				}
				break;
			case SOUND:
				String soundName = params.getString("name");
				if (soundName != null && !soundName.isEmpty()) {
					Sound s = Sound.valueOf(soundName.toUpperCase());
					float volume = Float.parseFloat(params.getString("volume", "1.0"));
					float pitch = Float.parseFloat(params.getString("pitch", "1.0"));
					if (loc != null) loc.getWorld().playSound(loc, s, volume * volumeMult, pitch);
				}
				break;
			case FIREWORK:
				if (!params.contains("type")) {
					throw new IllegalArgumentException("firework effect type must have 'type' parameter");
				}

				FireworkEffect.Builder b = FireworkEffect.builder();
				b = b.with(FireworkEffect.Type.valueOf(params.getString("type").toUpperCase()));
				if (params.contains("color")) {
					b = b.withColor(getColors(params.getString("color")));
				}
				if (params.contains("fade")) {
					b = b.withColor(getColors(params.getString("fade")));
				}
				boolean flicker = Boolean.parseBoolean(params.getString("flicker", "false"));
				boolean trail = Boolean.parseBoolean(params.getString("trail", "false"));
				b = b.flicker(flicker).trail(trail);
				if (loc != null) {
					try {
						FireworkEffectPlayer fwp = new FireworkEffectPlayer();
						fwp.playFirework(loc.getWorld(), loc, b.build());
					} catch (Exception e) {
						LogUtils.warning("can't play firework effect: "	 + e.getMessage());
					}
				}
				break;
			case MIDI:
				String file = effectName = params.getString("file");
				float tempo = Float.parseFloat(params.getString("tempo", "1.0"));
				if (file != null && !file.isEmpty()) {
					File f = new File(file);
					if (f.exists()) {
						if (loc != null) {
							MidiUtil.playMidiQuietly(f, tempo, loc);
						}
					} else {
						LogUtils.warning("non-existent midi file " + f.getAbsolutePath());
					}
				}
				break;
			}
		}

		private Color[] getColors(String string) {
			String[] s = string.split(" ");
			Color[] colors = new Color[s.length];
			for (int i = 0; i < s.length; i++) {
				colors[i] = Color.fromRGB(Integer.parseInt(s[i], 16));
			}
			return colors;
		}

	}
}
