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
public class GATEVersionIcon implements
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
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
clip = new Area(g.getClip());
clip.intersect(new Area(new Rectangle2D.Double(0.0,0.0,60.0,60.0)));
g.setClip(clip);
// _0 is CompositeGraphicsNode
float alpha__0_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0 = g.getClip();
AffineTransform defaultTransform__0_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -116.65451049804688f, -204.4719696044922f));
// _0_0 is CompositeGraphicsNode
origAlpha = alpha__0_0;
g.setTransform(defaultTransform__0_0);
g.setClip(clip__0_0);
float alpha__0_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1 = g.getClip();
AffineTransform defaultTransform__0_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -116.65451049804688f, -204.4719696044922f));
// _0_1 is CompositeGraphicsNode
float alpha__0_1_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_0 = g.getClip();
AffineTransform defaultTransform__0_1_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_0 is ShapeNode
origAlpha = alpha__0_1_0;
g.setTransform(defaultTransform__0_1_0);
g.setClip(clip__0_1_0);
float alpha__0_1_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_1 = g.getClip();
AffineTransform defaultTransform__0_1_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_1 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(170.63737, 234.47195);
((GeneralPath)shape).curveTo(170.80554, 263.7357, 122.67123, 258.0005, 122.67123, 258.0005);
((GeneralPath)shape).lineTo(122.67123, 210.94342);
((GeneralPath)shape).curveTo(122.67123, 210.94342, 170.46924, 205.20825, 170.63737, 234.47195);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
paint = new Color(0, 128, 0, 255);
stroke = new BasicStroke(2.0333996f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(170.63737, 234.47195);
((GeneralPath)shape).curveTo(170.80554, 263.7357, 122.67123, 258.0005, 122.67123, 258.0005);
((GeneralPath)shape).lineTo(122.67123, 210.94342);
((GeneralPath)shape).curveTo(122.67123, 210.94342, 170.46924, 205.20825, 170.63737, 234.47195);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1_1;
g.setTransform(defaultTransform__0_1_1);
g.setClip(clip__0_1_1);
float alpha__0_1_2 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_2 = g.getClip();
AffineTransform defaultTransform__0_1_2 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_2 is CompositeGraphicsNode
float alpha__0_1_2_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_2_0 = g.getClip();
AffineTransform defaultTransform__0_1_2_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_2_0 is ShapeNode
paint = new Color(255, 0, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(159.5505, 249.17284);
((GeneralPath)shape).lineTo(140.4545, 249.17284);
((GeneralPath)shape).curveTo(136.08382, 249.17284, 132.46115, 247.72084, 129.5865, 244.81683);
((GeneralPath)shape).curveTo(126.74117, 241.91283, 125.318504, 238.24617, 125.318504, 233.81683);
((GeneralPath)shape).curveTo(125.318504, 229.41685, 126.7265, 225.86752, 129.54251, 223.16884);
((GeneralPath)shape).curveTo(132.38785, 220.4702, 136.02518, 219.12086, 140.45451, 219.12083);
((GeneralPath)shape).lineTo(157.4385, 219.12083);
((GeneralPath)shape).lineTo(157.4385, 224.35684);
((GeneralPath)shape).lineTo(140.45451, 224.35684);
((GeneralPath)shape).curveTo(137.57983, 224.35686, 135.20383, 225.28087, 133.3265, 227.12885);
((GeneralPath)shape).curveTo(131.4785, 228.97687, 130.5545, 231.35286, 130.5545, 234.25685);
((GeneralPath)shape).curveTo(130.5545, 237.13153, 131.4785, 239.46352, 133.3265, 241.25285);
((GeneralPath)shape).curveTo(135.20383, 243.04219, 137.57983, 243.93686, 140.45451, 243.93686);
((GeneralPath)shape).lineTo(154.31451, 243.93686);
((GeneralPath)shape).lineTo(154.31451, 237.38086);
((GeneralPath)shape).lineTo(140.05852, 237.38086);
((GeneralPath)shape).lineTo(140.05852, 232.58485);
((GeneralPath)shape).lineTo(159.55052, 232.58485);
((GeneralPath)shape).lineTo(159.55052, 249.17285);
g.setPaint(paint);
g.fill(shape);
paint = new Color(128, 0, 0, 255);
stroke = new BasicStroke(1.0f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(159.5505, 249.17284);
((GeneralPath)shape).lineTo(140.4545, 249.17284);
((GeneralPath)shape).curveTo(136.08382, 249.17284, 132.46115, 247.72084, 129.5865, 244.81683);
((GeneralPath)shape).curveTo(126.74117, 241.91283, 125.318504, 238.24617, 125.318504, 233.81683);
((GeneralPath)shape).curveTo(125.318504, 229.41685, 126.7265, 225.86752, 129.54251, 223.16884);
((GeneralPath)shape).curveTo(132.38785, 220.4702, 136.02518, 219.12086, 140.45451, 219.12083);
((GeneralPath)shape).lineTo(157.4385, 219.12083);
((GeneralPath)shape).lineTo(157.4385, 224.35684);
((GeneralPath)shape).lineTo(140.45451, 224.35684);
((GeneralPath)shape).curveTo(137.57983, 224.35686, 135.20383, 225.28087, 133.3265, 227.12885);
((GeneralPath)shape).curveTo(131.4785, 228.97687, 130.5545, 231.35286, 130.5545, 234.25685);
((GeneralPath)shape).curveTo(130.5545, 237.13153, 131.4785, 239.46352, 133.3265, 241.25285);
((GeneralPath)shape).curveTo(135.20383, 243.04219, 137.57983, 243.93686, 140.45451, 243.93686);
((GeneralPath)shape).lineTo(154.31451, 243.93686);
((GeneralPath)shape).lineTo(154.31451, 237.38086);
((GeneralPath)shape).lineTo(140.05852, 237.38086);
((GeneralPath)shape).lineTo(140.05852, 232.58485);
((GeneralPath)shape).lineTo(159.55052, 232.58485);
((GeneralPath)shape).lineTo(159.55052, 249.17285);
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1_2_0;
g.setTransform(defaultTransform__0_1_2_0);
g.setClip(clip__0_1_2_0);
origAlpha = alpha__0_1_2;
g.setTransform(defaultTransform__0_1_2);
g.setClip(clip__0_1_2);
float alpha__0_1_3 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3 = g.getClip();
AffineTransform defaultTransform__0_1_3 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3 is CompositeGraphicsNode
float alpha__0_1_3_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3_0 = g.getClip();
AffineTransform defaultTransform__0_1_3_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3_0 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new Rectangle2D.Double(151.00753784179688, 250.3676300048828, 25.646968841552734, 14.104340553283691);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_3_0;
g.setTransform(defaultTransform__0_1_3_0);
g.setClip(clip__0_1_3_0);
float alpha__0_1_3_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3_1 = g.getClip();
AffineTransform defaultTransform__0_1_3_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3_1 is CompositeGraphicsNode
float alpha__0_1_3_1_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3_1_0 = g.getClip();
AffineTransform defaultTransform__0_1_3_1_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3_1_0 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(152.04587, 251.58777);
((GeneralPath)shape).lineTo(159.95993, 251.58777);
((GeneralPath)shape).lineTo(159.95993, 253.28308);
((GeneralPath)shape).lineTo(155.86618, 263.25183);
((GeneralPath)shape).lineTo(153.22556, 263.25183);
((GeneralPath)shape).lineTo(157.10056, 253.7987);
((GeneralPath)shape).lineTo(152.04587, 253.7987);
((GeneralPath)shape).lineTo(152.04587, 251.58777);
g.setPaint(paint);
g.fill(shape);
paint = new Color(0, 128, 0, 255);
stroke = new BasicStroke(1.0f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(152.04587, 251.58777);
((GeneralPath)shape).lineTo(159.95993, 251.58777);
((GeneralPath)shape).lineTo(159.95993, 253.28308);
((GeneralPath)shape).lineTo(155.86618, 263.25183);
((GeneralPath)shape).lineTo(153.22556, 263.25183);
((GeneralPath)shape).lineTo(157.10056, 253.7987);
((GeneralPath)shape).lineTo(152.04587, 253.7987);
((GeneralPath)shape).lineTo(152.04587, 251.58777);
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1_3_1_0;
g.setTransform(defaultTransform__0_1_3_1_0);
g.setClip(clip__0_1_3_1_0);
float alpha__0_1_3_1_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3_1_1 = g.getClip();
AffineTransform defaultTransform__0_1_3_1_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3_1_1 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(162.58493, 260.2284);
((GeneralPath)shape).lineTo(165.11618, 260.2284);
((GeneralPath)shape).lineTo(165.11618, 263.25183);
((GeneralPath)shape).lineTo(162.58493, 263.25183);
((GeneralPath)shape).lineTo(162.58493, 260.2284);
g.setPaint(paint);
g.fill(shape);
paint = new Color(0, 128, 0, 255);
stroke = new BasicStroke(1.0f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(162.58493, 260.2284);
((GeneralPath)shape).lineTo(165.11618, 260.2284);
((GeneralPath)shape).lineTo(165.11618, 263.25183);
((GeneralPath)shape).lineTo(162.58493, 263.25183);
((GeneralPath)shape).lineTo(162.58493, 260.2284);
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1_3_1_1;
g.setTransform(defaultTransform__0_1_3_1_1);
g.setClip(clip__0_1_3_1_1);
float alpha__0_1_3_1_2 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_3_1_2 = g.getClip();
AffineTransform defaultTransform__0_1_3_1_2 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_3_1_2 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(168.27243, 261.1737);
((GeneralPath)shape).lineTo(170.66306, 261.1737);
((GeneralPath)shape).lineTo(170.66306, 253.63464);
((GeneralPath)shape).lineTo(168.20993, 254.19714);
((GeneralPath)shape).lineTo(168.20993, 252.15027);
((GeneralPath)shape).lineTo(170.64743, 251.58777);
((GeneralPath)shape).lineTo(173.22556, 251.58777);
((GeneralPath)shape).lineTo(173.22556, 261.1737);
((GeneralPath)shape).lineTo(175.61618, 261.1737);
((GeneralPath)shape).lineTo(175.61618, 263.25183);
((GeneralPath)shape).lineTo(168.27243, 263.25183);
((GeneralPath)shape).lineTo(168.27243, 261.1737);
g.setPaint(paint);
g.fill(shape);
paint = new Color(0, 128, 0, 255);
stroke = new BasicStroke(1.0f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(168.27243, 261.1737);
((GeneralPath)shape).lineTo(170.66306, 261.1737);
((GeneralPath)shape).lineTo(170.66306, 253.63464);
((GeneralPath)shape).lineTo(168.20993, 254.19714);
((GeneralPath)shape).lineTo(168.20993, 252.15027);
((GeneralPath)shape).lineTo(170.64743, 251.58777);
((GeneralPath)shape).lineTo(173.22556, 251.58777);
((GeneralPath)shape).lineTo(173.22556, 261.1737);
((GeneralPath)shape).lineTo(175.61618, 261.1737);
((GeneralPath)shape).lineTo(175.61618, 263.25183);
((GeneralPath)shape).lineTo(168.27243, 263.25183);
((GeneralPath)shape).lineTo(168.27243, 261.1737);
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1_3_1_2;
g.setTransform(defaultTransform__0_1_3_1_2);
g.setClip(clip__0_1_3_1_2);
origAlpha = alpha__0_1_3_1;
g.setTransform(defaultTransform__0_1_3_1);
g.setClip(clip__0_1_3_1);
origAlpha = alpha__0_1_3;
g.setTransform(defaultTransform__0_1_3);
g.setClip(clip__0_1_3);
float alpha__0_1_4 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4 = g.getClip();
AffineTransform defaultTransform__0_1_4 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4 is CompositeGraphicsNode
float alpha__0_1_4_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_0 = g.getClip();
AffineTransform defaultTransform__0_1_4_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_0 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new Rectangle2D.Double(116.65451049804688, 255.174072265625, 35.33734893798828, 9.297898292541504);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_0;
g.setTransform(defaultTransform__0_1_4_0);
g.setClip(clip__0_1_4_0);
float alpha__0_1_4_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1 = g.getClip();
AffineTransform defaultTransform__0_1_4_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1 is CompositeGraphicsNode
float alpha__0_1_4_1_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_0 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_0 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(120.75433, 259.43054);
((GeneralPath)shape).lineTo(120.75433, 260.35632);
((GeneralPath)shape).curveTo(120.53949, 260.2489, 120.32855, 260.16785, 120.12152, 260.11316);
((GeneralPath)shape).curveTo(119.91644, 260.05847, 119.7221, 260.03116, 119.53851, 260.03116);
((GeneralPath)shape).curveTo(119.296326, 260.03116, 119.116646, 260.06827, 118.99945, 260.1425);
((GeneralPath)shape).curveTo(118.88421, 260.21667, 118.8266, 260.33194, 118.8266, 260.4882);
((GeneralPath)shape).curveTo(118.8266, 260.60538, 118.8657, 260.69717, 118.94379, 260.76358);
((GeneralPath)shape).curveTo(119.02189, 260.8281, 119.16449, 260.8837, 119.37151, 260.93057);
((GeneralPath)shape).lineTo(119.802185, 261.02728);
((GeneralPath)shape).curveTo(120.24164, 261.12497, 120.553154, 261.27338, 120.736755, 261.4726);
((GeneralPath)shape).curveTo(120.92035, 261.6718, 121.01214, 261.95502, 121.012146, 262.3222);
((GeneralPath)shape).curveTo(121.01214, 262.80463, 120.88324, 263.164, 120.62543, 263.40033);
((GeneralPath)shape).curveTo(120.36957, 263.6347, 119.97699, 263.7519, 119.447685, 263.7519);
((GeneralPath)shape).curveTo(119.197685, 263.7519, 118.94671, 263.7255, 118.694756, 263.6728);
((GeneralPath)shape).curveTo(118.444756, 263.6201, 118.19379, 263.54193, 117.941826, 263.43842);
((GeneralPath)shape).lineTo(117.941826, 262.48627);
((GeneralPath)shape).curveTo(118.19379, 262.6347, 118.43694, 262.747, 118.671326, 262.82318);
((GeneralPath)shape).curveTo(118.90569, 262.89737, 119.132256, 262.9345, 119.351006, 262.9345);
((GeneralPath)shape).curveTo(119.57171, 262.9345, 119.740654, 262.89352, 119.85785, 262.81146);
((GeneralPath)shape).curveTo(119.97699, 262.72946, 120.03656, 262.61224, 120.03656, 262.4599);
((GeneralPath)shape).curveTo(120.03656, 262.32318, 119.99656, 262.2177, 119.91644, 262.1435);
((GeneralPath)shape).curveTo(119.83634, 262.0693, 119.676216, 262.00287, 119.435974, 261.94427);
((GeneralPath)shape).lineTo(119.0434, 261.84756);
((GeneralPath)shape).curveTo(118.650826, 261.75388, 118.36273, 261.6044, 118.179146, 261.39932);
((GeneralPath)shape).curveTo(117.997505, 261.19424, 117.906685, 260.91788, 117.906685, 260.57022);
((GeneralPath)shape).curveTo(117.906685, 260.13467, 118.03364, 259.7997, 118.287544, 259.56534);
((GeneralPath)shape).curveTo(118.54144, 259.33096, 118.904724, 259.21378, 119.37739, 259.21378);
((GeneralPath)shape).curveTo(119.594185, 259.21378, 119.81684, 259.23236, 120.04536, 259.26947);
((GeneralPath)shape).curveTo(120.273865, 259.30466, 120.5102, 259.35837, 120.75433, 259.4306);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_0;
g.setTransform(defaultTransform__0_1_4_1_0);
g.setClip(clip__0_1_4_1_0);
float alpha__0_1_4_1_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_1 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_1 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(121.9057, 259.29285);
((GeneralPath)shape).lineTo(123.03949, 259.29285);
((GeneralPath)shape).lineTo(124.47211, 262.29285);
((GeneralPath)shape).lineTo(124.47211, 259.29285);
((GeneralPath)shape).lineTo(125.43304, 259.29285);
((GeneralPath)shape).lineTo(125.43304, 263.66687);
((GeneralPath)shape).lineTo(124.299255, 263.66687);
((GeneralPath)shape).lineTo(122.86957, 260.66687);
((GeneralPath)shape).lineTo(122.86957, 263.66687);
((GeneralPath)shape).lineTo(121.9057, 263.66687);
((GeneralPath)shape).lineTo(121.9057, 259.29285);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_1;
g.setTransform(defaultTransform__0_1_4_1_1);
g.setClip(clip__0_1_4_1_1);
float alpha__0_1_4_1_2 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_2 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_2 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_2 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(128.81976, 262.87);
((GeneralPath)shape).lineTo(127.23187, 262.87);
((GeneralPath)shape).lineTo(126.97992, 263.66687);
((GeneralPath)shape).lineTo(125.96039, 263.66687);
((GeneralPath)shape).lineTo(127.419365, 259.29285);
((GeneralPath)shape).lineTo(128.62932, 259.29285);
((GeneralPath)shape).lineTo(130.0883, 263.66687);
((GeneralPath)shape).lineTo(129.06584, 263.66687);
((GeneralPath)shape).lineTo(128.81975, 262.87);
((GeneralPath)shape).moveTo(127.48382, 262.05847);
((GeneralPath)shape).lineTo(128.56194, 262.05847);
((GeneralPath)shape).lineTo(128.0258, 260.31824);
((GeneralPath)shape).lineTo(127.48381, 262.05847);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_2;
g.setTransform(defaultTransform__0_1_4_1_2);
g.setClip(clip__0_1_4_1_2);
float alpha__0_1_4_1_3 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_3 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_3 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_3 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(130.61273, 259.29285);
((GeneralPath)shape).lineTo(132.2973, 259.29285);
((GeneralPath)shape).curveTo(132.79926, 259.29285, 133.18402, 259.41687, 133.4516, 259.66492);
((GeneralPath)shape).curveTo(133.71916, 259.911, 133.85297, 260.26257, 133.85297, 260.7196);
((GeneralPath)shape).curveTo(133.85295, 261.1786, 133.71916, 261.5321, 133.4516, 261.78015);
((GeneralPath)shape).curveTo(133.18402, 262.02625, 132.79926, 262.1493, 132.2973, 262.1493);
((GeneralPath)shape).lineTo(131.62933, 262.1493);
((GeneralPath)shape).lineTo(131.62933, 263.66687);
((GeneralPath)shape).lineTo(130.61273, 263.66687);
((GeneralPath)shape).lineTo(130.61273, 259.29285);
((GeneralPath)shape).moveTo(131.62933, 260.11023);
((GeneralPath)shape).lineTo(131.62933, 261.3319);
((GeneralPath)shape).lineTo(132.1889, 261.3319);
((GeneralPath)shape).curveTo(132.38617, 261.3319, 132.53851, 261.2792, 132.64595, 261.1737);
((GeneralPath)shape).curveTo(132.75336, 261.06628, 132.80708, 260.91492, 132.80708, 260.7196);
((GeneralPath)shape).curveTo(132.80708, 260.5243, 132.75339, 260.3739, 132.64595, 260.26843);
((GeneralPath)shape).curveTo(132.53851, 260.16296, 132.38618, 260.11023, 132.1889, 260.11023);
((GeneralPath)shape).lineTo(131.62933, 260.11023);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_3;
g.setTransform(defaultTransform__0_1_4_1_3);
g.setClip(clip__0_1_4_1_3);
float alpha__0_1_4_1_4 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_4 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_4 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_4 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(137.31293, 259.43054);
((GeneralPath)shape).lineTo(137.31293, 260.35632);
((GeneralPath)shape).curveTo(137.09808, 260.2489, 136.88713, 260.16785, 136.68011, 260.11316);
((GeneralPath)shape).curveTo(136.47504, 260.05847, 136.2807, 260.03116, 136.09712, 260.03116);
((GeneralPath)shape).curveTo(135.85493, 260.03116, 135.67525, 260.06827, 135.55806, 260.1425);
((GeneralPath)shape).curveTo(135.44283, 260.21667, 135.38521, 260.33194, 135.38521, 260.4882);
((GeneralPath)shape).curveTo(135.38521, 260.60538, 135.4243, 260.69717, 135.5024, 260.76358);
((GeneralPath)shape).curveTo(135.58049, 260.8281, 135.7231, 260.8837, 135.93013, 260.93057);
((GeneralPath)shape).lineTo(136.3608, 261.02728);
((GeneralPath)shape).curveTo(136.80025, 261.12497, 137.11177, 261.27338, 137.29536, 261.4726);
((GeneralPath)shape).curveTo(137.47896, 261.6718, 137.57076, 261.95502, 137.57076, 262.3222);
((GeneralPath)shape).curveTo(137.57076, 262.80463, 137.44185, 263.164, 137.18404, 263.40033);
((GeneralPath)shape).curveTo(136.92818, 263.6347, 136.5356, 263.7519, 136.0063, 263.7519);
((GeneralPath)shape).curveTo(135.75629, 263.7519, 135.50533, 263.7255, 135.25337, 263.6728);
((GeneralPath)shape).curveTo(135.00337, 263.6201, 134.7524, 263.54193, 134.50044, 263.43842);
((GeneralPath)shape).lineTo(134.50044, 262.48627);
((GeneralPath)shape).curveTo(134.7524, 262.6347, 134.99556, 262.747, 135.22993, 262.82318);
((GeneralPath)shape).curveTo(135.46431, 262.89737, 135.69087, 262.9345, 135.90962, 262.9345);
((GeneralPath)shape).curveTo(136.13033, 262.9345, 136.29926, 262.89352, 136.41646, 262.81146);
((GeneralPath)shape).curveTo(136.5356, 262.72946, 136.59517, 262.61224, 136.59517, 262.4599);
((GeneralPath)shape).curveTo(136.59517, 262.32318, 136.55518, 262.2177, 136.47505, 262.1435);
((GeneralPath)shape).curveTo(136.39496, 262.0693, 136.23482, 262.00287, 135.99458, 261.94427);
((GeneralPath)shape).lineTo(135.602, 261.84756);
((GeneralPath)shape).curveTo(135.20943, 261.75388, 134.92134, 261.6044, 134.73775, 261.39932);
((GeneralPath)shape).curveTo(134.5561, 261.19424, 134.46529, 260.91788, 134.46529, 260.57022);
((GeneralPath)shape).curveTo(134.46529, 260.13467, 134.59224, 259.7997, 134.84615, 259.56534);
((GeneralPath)shape).curveTo(135.10005, 259.33096, 135.46333, 259.21378, 135.93599, 259.21378);
((GeneralPath)shape).curveTo(136.15279, 259.21378, 136.37544, 259.23236, 136.60396, 259.26947);
((GeneralPath)shape).curveTo(136.83247, 259.30466, 137.0688, 259.35837, 137.31294, 259.4306);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_4;
g.setTransform(defaultTransform__0_1_4_1_4);
g.setClip(clip__0_1_4_1_4);
float alpha__0_1_4_1_5 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_5 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_5 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_5 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(138.4643, 259.29285);
((GeneralPath)shape).lineTo(139.48091, 259.29285);
((GeneralPath)shape).lineTo(139.48091, 260.95984);
((GeneralPath)shape).lineTo(140.97798, 260.95984);
((GeneralPath)shape).lineTo(140.97798, 259.29285);
((GeneralPath)shape).lineTo(141.99165, 259.29285);
((GeneralPath)shape).lineTo(141.99165, 263.66687);
((GeneralPath)shape).lineTo(140.97798, 263.66687);
((GeneralPath)shape).lineTo(140.97798, 261.81238);
((GeneralPath)shape).lineTo(139.48091, 261.81238);
((GeneralPath)shape).lineTo(139.48091, 263.66687);
((GeneralPath)shape).lineTo(138.4643, 263.66687);
((GeneralPath)shape).lineTo(138.4643, 259.29285);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_5;
g.setTransform(defaultTransform__0_1_4_1_5);
g.setClip(clip__0_1_4_1_5);
float alpha__0_1_4_1_6 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_6 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_6 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_6 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(144.78656, 260.03113);
((GeneralPath)shape).curveTo(144.47797, 260.03113, 144.23773, 260.15808, 144.06586, 260.412);
((GeneralPath)shape).curveTo(143.89592, 260.6659, 143.81097, 261.02332, 143.81097, 261.48425);
((GeneralPath)shape).curveTo(143.81097, 261.94324, 143.89598, 262.29968, 144.06586, 262.5536);
((GeneralPath)shape).curveTo(144.23773, 262.8075, 144.47795, 262.93445, 144.78656, 262.93445);
((GeneralPath)shape).curveTo(145.0971, 262.93445, 145.33734, 262.8075, 145.50726, 262.5536);
((GeneralPath)shape).curveTo(145.67914, 262.29968, 145.76508, 261.94324, 145.76508, 261.48425);
((GeneralPath)shape).curveTo(145.76506, 261.02332, 145.67908, 260.6659, 145.50726, 260.412);
((GeneralPath)shape).curveTo(145.33734, 260.15808, 145.0971, 260.03113, 144.78656, 260.03113);
((GeneralPath)shape).moveTo(144.78656, 259.21375);
((GeneralPath)shape).curveTo(145.41937, 259.21375, 145.91449, 259.41492, 146.27191, 259.81726);
((GeneralPath)shape).curveTo(146.63129, 260.2196, 146.81097, 260.77527, 146.81097, 261.48425);
((GeneralPath)shape).curveTo(146.81097, 262.19128, 146.63129, 262.74597, 146.27191, 263.14832);
((GeneralPath)shape).curveTo(145.91449, 263.55066, 145.41937, 263.75183, 144.78656, 263.75183);
((GeneralPath)shape).curveTo(144.1557, 263.75183, 143.66058, 263.55066, 143.30121, 263.14832);
((GeneralPath)shape).curveTo(142.94183, 262.74597, 142.76215, 262.19128, 142.76215, 261.48425);
((GeneralPath)shape).curveTo(142.76215, 260.77527, 142.94183, 260.2196, 143.30121, 259.81726);
((GeneralPath)shape).curveTo(143.66058, 259.41492, 144.1557, 259.21375, 144.78656, 259.21375);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_6;
g.setTransform(defaultTransform__0_1_4_1_6);
g.setClip(clip__0_1_4_1_6);
float alpha__0_1_4_1_7 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4_1_7 = g.getClip();
AffineTransform defaultTransform__0_1_4_1_7 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4_1_7 is ShapeNode
paint = new Color(0, 128, 0, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(147.11273, 259.29285);
((GeneralPath)shape).lineTo(150.73969, 259.29285);
((GeneralPath)shape).lineTo(150.73969, 260.1454);
((GeneralPath)shape).lineTo(149.43596, 260.1454);
((GeneralPath)shape).lineTo(149.43596, 263.66687);
((GeneralPath)shape).lineTo(148.41936, 263.66687);
((GeneralPath)shape).lineTo(148.41936, 260.1454);
((GeneralPath)shape).lineTo(147.11272, 260.1454);
((GeneralPath)shape).lineTo(147.11272, 259.29285);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_4_1_7;
g.setTransform(defaultTransform__0_1_4_1_7);
g.setClip(clip__0_1_4_1_7);
origAlpha = alpha__0_1_4_1;
g.setTransform(defaultTransform__0_1_4_1);
g.setClip(clip__0_1_4_1);
origAlpha = alpha__0_1_4;
g.setTransform(defaultTransform__0_1_4);
g.setClip(clip__0_1_4);
origAlpha = alpha__0_1;
g.setTransform(defaultTransform__0_1);
g.setClip(clip__0_1);
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
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * 
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 0;
    }

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 * 
	 * @return The width of the bounding box of the original SVG image.
	 */
	public static int getOrigWidth() {
		return 60;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 * 
	 * @return The height of the bounding box of the original SVG image.
	 */
	public static int getOrigHeight() {
		return 60;
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
	public GATEVersionIcon() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
	}
	
	/**
	 * Creates a new transcoded SVG image with the given dimensions.
	 *
	 * @param size the dimensions of the icon
	 */
	public GATEVersionIcon(Dimension size) {
	this.width = size.width;
	this.height = size.width;
	}

	public GATEVersionIcon(int width, int height) {
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

