package gate.resources.img.svg;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class has been automatically generated using <a
 * href="http://englishjavadrinker.blogspot.com/search/label/SVGRoundTrip">SVGRoundTrip</a>.
 */
@SuppressWarnings("unused")
public class RussianIcon implements
		javax.swing.Icon {
	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	public static void paint(Graphics2D g) {
        Shape shape = null;
        Paint paint = null;
        Stroke stroke = null;
        Area clip = null;
         
        float origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = 
                (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
	    Shape clip_ = g.getClip();
AffineTransform defaultTransform_ = g.getTransform();
//  is CompositeGraphicsNode
float alpha__0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0 = g.getClip();
AffineTransform defaultTransform__0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -0.0f, -0.0f));
clip = new Area(g.getClip());
clip.intersect(new Area(new Rectangle2D.Double(0.0,0.0,32.0,32.0)));
g.setClip(clip);
// _0 is CompositeGraphicsNode
float alpha__0_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0 = g.getClip();
AffineTransform defaultTransform__0_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_0 is TextNode of 'Ж'
shape = new GeneralPath();
((GeneralPath)shape).moveTo(18.40166, 3.6343498);
((GeneralPath)shape).lineTo(18.40166, 14.005541);
((GeneralPath)shape).quadTo(19.631578, 13.905818, 20.263157, 13.0914135);
((GeneralPath)shape).quadTo(20.894735, 12.277009, 22.01385, 9.362882);
((GeneralPath)shape).quadTo(23.476454, 5.5623274, 25.005539, 4.509696);
((GeneralPath)shape).quadTo(26.512465, 3.4792252, 30.03601, 3.4792252);
((GeneralPath)shape).quadTo(30.202215, 3.4792252, 30.756231, 3.4681447);
((GeneralPath)shape).lineTo(30.756231, 7.102494);
((GeneralPath)shape).lineTo(30.03601, 7.0914135);
((GeneralPath)shape).quadTo(28.540165, 7.0914135, 27.842104, 7.4626045);
((GeneralPath)shape).quadTo(27.144043, 7.8337955, 26.71191, 8.598338);
((GeneralPath)shape).quadTo(26.279778, 9.362882, 25.437672, 11.745152);
((GeneralPath)shape).quadTo(24.98338, 13.00831, 24.462603, 13.855956);
((GeneralPath)shape).quadTo(23.941828, 14.703602, 22.612188, 15.445984);
((GeneralPath)shape).quadTo(24.252077, 15.933518, 25.32687, 17.33518);
((GeneralPath)shape).quadTo(26.40166, 18.736843, 27.6759, 21.31856);
((GeneralPath)shape).lineTo(31.0, 28.0);
((GeneralPath)shape).lineTo(25.216066, 28.0);
((GeneralPath)shape).lineTo(22.290857, 21.695292);
((GeneralPath)shape).quadTo(22.246536, 21.562326, 22.01385, 21.185596);
((GeneralPath)shape).quadTo(21.880886, 20.930748, 21.415512, 20.033241);
((GeneralPath)shape).quadTo(20.484764, 18.293629, 19.886427, 17.811634);
((GeneralPath)shape).quadTo(19.288088, 17.32964, 18.40166, 17.32964);
((GeneralPath)shape).lineTo(18.40166, 28.0);
((GeneralPath)shape).lineTo(13.603878, 28.0);
((GeneralPath)shape).lineTo(13.603878, 17.32964);
((GeneralPath)shape).quadTo(12.750692, 17.32964, 12.135734, 17.795013);
((GeneralPath)shape).quadTo(11.520775, 18.260387, 10.612188, 20.033241);
((GeneralPath)shape).quadTo(10.091413, 21.030472, 9.99169, 21.185596);
((GeneralPath)shape).quadTo(9.847645, 21.429363, 9.714682, 21.695292);
((GeneralPath)shape).lineTo(6.7894735, 28.0);
((GeneralPath)shape).lineTo(1.0055401, 28.0);
((GeneralPath)shape).lineTo(4.32964, 21.31856);
((GeneralPath)shape).quadTo(5.5595565, 18.836565, 6.645429, 17.385042);
((GeneralPath)shape).quadTo(7.731302, 15.933518, 9.415512, 15.445984);
((GeneralPath)shape).quadTo(8.085873, 14.703602, 7.5595565, 13.861496);
((GeneralPath)shape).quadTo(7.033241, 13.019391, 6.567867, 11.745152);
((GeneralPath)shape).quadTo(5.736842, 9.418283, 5.315789, 8.637119);
((GeneralPath)shape).quadTo(4.894737, 7.855956, 4.1855955, 7.473685);
((GeneralPath)shape).quadTo(3.4764543, 7.0914135, 1.8808864, 7.0914135);
((GeneralPath)shape).quadTo(1.6814404, 7.0914135, 1.2493075, 7.102494);
((GeneralPath)shape).lineTo(1.2493075, 3.4681447);
((GeneralPath)shape).quadTo(1.8033241, 3.4792252, 1.969529, 3.4792252);
((GeneralPath)shape).quadTo(5.537396, 3.4792252, 7.0554013, 4.5318565);
((GeneralPath)shape).quadTo(8.551247, 5.5955687, 9.99169, 9.362882);
((GeneralPath)shape).quadTo(11.121883, 12.288089, 11.747922, 13.096953);
((GeneralPath)shape).quadTo(12.3739605, 13.905818, 13.603878, 14.005541);
((GeneralPath)shape).lineTo(13.603878, 3.6343498);
((GeneralPath)shape).lineTo(18.40166, 3.6343498);
((GeneralPath)shape).closePath();
paint = new Color(0, 0, 0, 255);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_0;
g.setTransform(defaultTransform__0_0);
g.setClip(clip__0_0);
origAlpha = alpha__0;
g.setTransform(defaultTransform__0);
g.setClip(clip__0);
g.setTransform(defaultTransform_);
g.setClip(clip_);

	}
	
	public Image getImage() {
		BufferedImage image =
            new BufferedImage(getIconWidth(), getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g = image.createGraphics();
    	paintIcon(null, g, 0, 0);
    	g.dispose();
    	return image;
	}

    /**
     * Returns the X of the bounding box of the original SVG image.
     * 
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 2;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * 
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 4;
    }

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 * 
	 * @return The width of the bounding box of the original SVG image.
	 */
	public static int getOrigWidth() {
		return 32;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 * 
	 * @return The height of the bounding box of the original SVG image.
	 */
	public static int getOrigHeight() {
		return 32;
	}

	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public RussianIcon() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
	}
	
	/**
	 * Creates a new transcoded SVG image with the given dimensions.
	 *
	 * @param size the dimensions of the icon
	 */
	public RussianIcon(Dimension size) {
	this.width = size.width;
	this.height = size.width;
	}

	public RussianIcon(int width, int height) {
	this.width = width;
	this.height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
    @Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
    @Override
	public int getIconWidth() {
		return width;
	}

	public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(x, y);
						
		Area clip = new Area(new Rectangle(0, 0, this.width, this.height));		
		if (g2d.getClip() != null) clip.intersect(new Area(g2d.getClip()));		
		g2d.setClip(clip);

		double coef1 = (double) this.width / (double) getOrigWidth();
		double coef2 = (double) this.height / (double) getOrigHeight();
		double coef = Math.min(coef1, coef2);
		g2d.scale(coef, coef);
		paint(g2d);
		g2d.dispose();
	}
}

