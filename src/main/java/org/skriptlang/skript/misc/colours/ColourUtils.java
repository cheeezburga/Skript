package org.skriptlang.skript.misc.colours;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;

public class ColourUtils {

	public static float[] rgbToHsl(Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float h, s, l = (max + min) / 2f;
		if (max == min) {
			h = s = 0f;
		} else {
			float delta = max - min;
			s = l > 0.5f ? delta / (2f - max - min) : delta / (max + min);
			if (max == r) {
				h = ((g - b) / delta + (g < b ? 6f : 0f)) / 6f;
			} else if (max == g) {
				h = ((b - r) / delta + 2f) / 6f;
			} else {
				h = ((r - g) / delta + 4f) / 6f;
			}
		}
		return new float[]{ h, s, l };
	}

	public static ColorRGB hslToRgb(float[] hsl) {
		float h = hsl[0], s = hsl[1], l = hsl[2];
		float r, g, b;
		if (s == 0f) {
			r = g = b = l;
		} else {
			float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
			float p = 2f * l - q;
			r = hueToRgb(p, q, h + 1f / 3f);
			g = hueToRgb(p, q, h);
			b = hueToRgb(p, q, h - 1f / 3f);
		}
		int red = Math.round(r * 255f);
		int green = Math.round(g * 255f);
		int blue = Math.round(b * 255f);
		return ColorRGB.fromRGBA(red, green, blue, 255);
	}

	private static float hueToRgb(float p, float q, float t) {
		if (t < 0f)
			t += 1f;
		if (t > 1f)
			t -= 1f;
		if (t < 1f / 6f)
			return p + (q - p) * 6f * t;
		if (t < 1f / 2f)
			return q;
		if (t < 2f / 3f)
			return p + (q - p) * (2f / 3f - t) * 6f;
		return p;
	}

	public static Color blendColors(Color c1, Color c2, double amount) {
		amount = Math.max(0, Math.min(1, amount));
		int r = (int) (c1.getRed() * (1 - amount) + c2.getRed() * amount);
		int g = (int) (c1.getGreen() * (1 - amount) + c2.getGreen() * amount);
		int b = (int) (c1.getBlue() * (1 - amount) + c2.getBlue() * amount);
		int a = (int) (c1.getAlpha() * (1 - amount) + c2.getAlpha() * amount);
		return ColorRGB.fromRGBA(r, g, b, a);
	}

	public static Color complementColor(Color color) {
		int r = 255 - color.getRed();
		int g = 255 - color.getGreen();
		int b = 255 - color.getBlue();
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	public static Color complementColorHSL(Color color) {
		float[] hsl = rgbToHsl(color);
		hsl[0] = (hsl[0] + 0.5f) % 1f;
		return hslToRgb(hsl);
	}

	public static ColorRGB shadeColor(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
		double factor = (100 - amount) / 100.0;
		int r = (int) (color.getRed() * factor);
		int g = (int) (color.getGreen() * factor);
		int b = (int) (color.getBlue() * factor);
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	public static ColorRGB shadeColorHSL(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
		float[] hsl = rgbToHsl(color);
		hsl[2] *= (100 - amount) / 100f;
		return hslToRgb(hsl);
	}

	public static ColorRGB tintColor(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
		double factor = amount / 100.0;
		int r = (int) (color.getRed() + (255 - color.getRed()) * factor);
		int g = (int) (color.getGreen() + (255 - color.getGreen()) * factor);
		int b = (int) (color.getBlue() + (255 - color.getBlue()) * factor);
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	public static ColorRGB tintColorHSL(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
		float[] hsl = rgbToHsl(color);
		hsl[2] += (1f - hsl[2]) * (amount / 100f);
		hsl[2] = Math.min(1f, hsl[2]);
		return hslToRgb(hsl);
	}

	public static Color rotateHue(Color color, int degrees) {
		float[] hsl = rgbToHsl(color);
		hsl[0] = (hsl[0] + degrees / 360f) % 1f;
		if (hsl[0] < 0f)
			hsl[0] += 1f;
		return hslToRgb(hsl);
	}

	public static ColorRGB adjustBrightness(Color color, int amount) {
		amount = Math.max(-100, Math.min(100, amount));
		float[] hsb = rgbToHsb(color);
		float factor = amount / 100f;
		hsb[2] = hsb[2] + hsb[2] * factor;
		hsb[2] = Math.max(0f, Math.min(1f, hsb[2]));
		return hsbToRgb(hsb);
	}

	private static float[] rgbToHsb(Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float h = 0f;
		float s = max == 0f ? 0f : delta / max;
		float v = max;
		if (delta != 0f) {
			if (max == r) {
				h = ((g - b) / delta) % 6f;
			} else if (max == g) {
				h = ((b - r) / delta) + 2f;
			} else {
				h = ((r - g) / delta) + 4f;
			}
			h *= 60f;
			if (h < 0f) h += 360f;
		}
		h /= 360f;
		return new float[]{ h, s, v };
	}

	private static ColorRGB hsbToRgb(float[] hsb) {
		float h = hsb[0], s = hsb[1], v = hsb[2];
		float r = 0f, g = 0f, b = 0f;
		int i = (int) (h * 6f);
		float f = h * 6f - i;
		float p = v * (1f - s);
		float q = v * (1f - f * s);
		float t = v * (1f - (1f - f) * s);
		switch (i % 6) {
			case 0:
				r = v; g = t; b = p; break;
			case 1:
				r = q; g = v; b = p; break;
			case 2:
				r = p; g = v; b = t; break;
			case 3:
				r = p; g = q; b = v; break;
			case 4:
				r = t; g = p; b = v; break;
			case 5:
				r = v; g = p; b = q; break;
		}
		int red = Math.round(r * 255f);
		int green = Math.round(g * 255f);
		int blue = Math.round(b * 255f);
		return ColorRGB.fromRGBA(red, green, blue, 255);
	}

	public static ColorRGB toGrayscale(Color color) {
		int gray = (int)(0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
		return ColorRGB.fromRGBA(gray, gray, gray, color.getAlpha());
	}

	public static ColorRGB toSepia(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
		int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
		int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
		tr = Math.min(255, tr);
		tg = Math.min(255, tg);
		tb = Math.min(255, tb);
		return ColorRGB.fromRGBA(tr, tg, tb, color.getAlpha());
	}

	public static ColorRGB adjustTemperature(Color color, int amount) {
		int r = color.getRed() + amount;
		int b = color.getBlue() - amount;
		r = Math.max(0, Math.min(255, r));
		b = Math.max(0, Math.min(255, b));
		return ColorRGB.fromRGBA(r, color.getGreen(), b, color.getAlpha());
	}

}
