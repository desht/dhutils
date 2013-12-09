package me.desht.dhutils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class MapUtil {
	private static final IndexColorModel mapColorModel;
	private static final Color[] mapColors;

	private static final byte[] chatColorToMapPalette = new byte[] {
			119,    // 0 black
			48, // 1 blue
			28, // 2 green
			21, // 3 cyan
			16, // 4 red
			64,    // 5 purple
			72,    // 6 yellow
			13,    // 7 grey
			12,    // 8 dark grey
			50,    // 9 bright blue
			78,    // 10 bright green
			126,    // 11 bright cyan
			18,    // 12 bright red
			82, // 13 pink
			74, // 14 bright yellow
			34, // 15 white
	};

	static {
		final Color[] baseMapColors = new Color[] {
				new Color(0, 0, 0, 0),
				new Color(127, 178, 56),
				new Color(247, 233, 163),
				new Color(167, 167, 167),
				new Color(255, 0, 0),
				new Color(160, 160, 255),
				new Color(167, 167, 167),
				new Color(0, 124, 0),
				new Color(255, 255, 255),
				new Color(164, 168, 184),
				new Color(183, 106, 47),
				new Color(112, 112, 112),
				new Color(64, 64, 255),
				new Color(104, 83, 50),
				//new 1.7 colors (13w42a/13w42b)
				new Color(255, 252, 245),
				new Color(216, 127, 51),
				new Color(178, 76, 216),
				new Color(102, 153, 216),
				new Color(229, 229, 51),
				new Color(127, 204, 25),
				new Color(242, 127, 165),
				new Color(76, 76, 76),
				new Color(153, 153, 153),
				new Color(76, 127, 153),
				new Color(127, 63, 178),
				new Color(51, 76, 178),
				new Color(102, 76, 51),
				new Color(102, 127, 51),
				new Color(153, 51, 51),
				new Color(25, 25, 25),
				new Color(250, 238, 77),
				new Color(92, 219, 213),
				new Color(74, 128, 255),
				new Color(0, 217, 58),
				new Color(21, 20, 31),
				new Color(112, 2, 0)
		};
		mapColors = new Color[baseMapColors.length*4];
		for (int i = 0; i < baseMapColors.length; ++i) {
			Color bc = baseMapColors[i];
			mapColors[i*4 +0] = new Color((int)(bc.getRed()*180.0/255.0+0.5), (int)(bc.getGreen()*180.0/255.0+0.5), (int)(bc.getBlue()*180.0/255.0+0.5), bc.getAlpha());
			mapColors[i*4 +1] = new Color((int)(bc.getRed()*220.0/255.0+0.5), (int)(bc.getGreen()*220.0/255.0+0.5), (int)(bc.getBlue()*220.0/255.0+0.5), bc.getAlpha());
			mapColors[i*4 +2] = bc;
			mapColors[i*4 +3] = new Color((int)(bc.getRed()*135.0/255.0+0.5), (int)(bc.getGreen()*135.0/255.0+0.5), (int)(bc.getBlue()*135.0/255.0+0.5), bc.getAlpha());
		}
		byte[] r = new byte[mapColors.length],
				g = new byte[mapColors.length],
				b = new byte[mapColors.length],
				a = new byte[mapColors.length];
		for (int i = 0; i < mapColors.length; ++i) {
			Color mc = mapColors[i];
			r[i] = (byte)mc.getRed();
			g[i] = (byte)mc.getGreen();
			b[i] = (byte)mc.getBlue();
			a[i] = (byte)mc.getAlpha();
		}
		mapColorModel = new IndexColorModel(8, mapColors.length, r, g, b, a);
	}

	public static BufferedImage createMapBuffer() {
		return new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_INDEXED, mapColorModel);
	}

	public static Color getChatColor(int chatColor) {
		return mapColors[chatColorToMapPalette[chatColor]];
	}

	public static Color getMapColor(int color) {
		return mapColors[color];
	}
}
