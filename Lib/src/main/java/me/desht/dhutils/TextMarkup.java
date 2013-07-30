package me.desht.dhutils;

import org.apache.commons.lang.StringUtils;

public class TextMarkup {
	private enum MarkupType {
		COLOUR,
		BOLD,
		STRIKE,
		UNDERLINE,
		ITALIC,
		RESET,
		NONE,
	}

	private static class MarkedUpTextSegment {
		private final String text;
		private final MarkupType type;
		private final char colourCode;

		private MarkedUpTextSegment(String text, MarkupType type) {
			this(text, type, '\000');
		}
		private MarkedUpTextSegment(String text, MarkupType type, char colourCode) {
			this.text = text;
			this.type = type;
			this.colourCode = colourCode;
		}

		@Override
		public String toString() {
			String style;
			switch(type) {
			case COLOUR: style = "color:#" + colourToHex(colourCode); break;
			case BOLD: style = "font-weight:bold"; break;
			case STRIKE: style = "text-decoration:line-through"; break;
			case UNDERLINE: style = "text-decoration:underline"; break;
			case ITALIC: style = "font-style:italic"; break;
			case RESET: return "<span>" + text;
			case NONE: default: return text;
			}
			return "<span style='" + style + ";'>" + text;
		}
	}

	private static MarkedUpTextSegment processMarkup(String text) {
		char code = Character.toLowerCase(text.charAt(0));
		text = text.substring(1);
		if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
			return new MarkedUpTextSegment(text, MarkupType.COLOUR, code);
		} else if (code == 'l') {
			return new MarkedUpTextSegment(text, MarkupType.BOLD);
		} else if (code == 'm') {
			return new MarkedUpTextSegment(text, MarkupType.STRIKE);
		} else if (code == 'n') {
			return new MarkedUpTextSegment(text, MarkupType.UNDERLINE);
		} else if (code == 'o') {
			return new MarkedUpTextSegment(text, MarkupType.ITALIC);
		} else if (code == 'r') {
			return new MarkedUpTextSegment(text, MarkupType.RESET);
		} else {
			return new MarkedUpTextSegment("\u00a7" + code + text, MarkupType.NONE);
		}
	}

	private static String colourToHex(char code) {
		switch (code) {
		case '0': return "222";
		case '1': return "00A";
		case '2': return "0A0";
		case '3': return "0AA";
		case '4': return "A00";
		case '5': return "A0A";
		case '6': return "FA0";
		case '7': return "AAA";
		case '8': return "555";
		case '9': return "55F";
		case 'a': return "5F5";
		case 'b': return "5FF";
		case 'c': return "F55";
		case 'd': return "F5F";
		case 'e': return "FF5";
		case 'f': return "FFF";
		default: return "FFF";
		}
	}

	public static String textToHTML(String text) {
		String[] sections = text.split("\u00a7");
		StringBuilder sb = new StringBuilder();

		int spanDepth = 0; // count how many nested spans we have

		for (int i = 0; i < sections.length; i++) {
			if (i == 0) {
				// first section has no markup; just append it
				sb.append(sections[i]);
			} else if (sections[i].length() >= 2) {
				MarkedUpTextSegment seg = processMarkup(sections[i]);

				if (seg.type == MarkupType.COLOUR || seg.type == MarkupType.RESET) {
					// close any open spans
					sb.append(StringUtils.repeat("</span>", spanDepth));
					sb.append(seg.toString()).append("</span>");
					spanDepth = 0;
				} else if (seg.type == MarkupType.NONE) {
					sb.append(seg.toString()); // no span tag here
				} else {
					sb.append(seg.toString()); // leave the span tag open
					spanDepth++;
				}
			}
		}
		sb.append(StringUtils.repeat("</span>", spanDepth));
		return sb.toString();
	}
}
