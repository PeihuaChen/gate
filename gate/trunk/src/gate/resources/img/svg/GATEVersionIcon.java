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
// _0_1_3 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new Rectangle2D.Double(151.00753784179688, 250.3676300048828, 25.646968841552734, 14.104340553283691);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_3;
g.setTransform(defaultTransform__0_1_3);
g.setClip(clip__0_1_3);
float alpha__0_1_4 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_4 = g.getClip();
AffineTransform defaultTransform__0_1_4 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_4 is TextNode of '7.2'
shape = new GeneralPath();
((GeneralPath)shape).moveTo(153.48337, 263.25183);
((GeneralPath)shape).quadTo(153.64743, 261.94714, 154.1279, 260.96277);
((GeneralPath)shape).quadTo(154.60837, 259.9784, 156.28806, 257.52527);
((GeneralPath)shape).lineTo(159.0068, 253.56433);
((GeneralPath)shape).lineTo(152.99118, 253.56433);
((GeneralPath)shape).lineTo(152.99118, 251.68933);
((GeneralPath)shape).lineTo(160.89743, 251.68933);
((GeneralPath)shape).lineTo(160.89743, 253.56433);
((GeneralPath)shape).quadTo(158.624, 256.43152, 157.33493, 259.00574);
((GeneralPath)shape).quadTo(156.04587, 261.57996, 156.04587, 263.25183);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(160.86618, 263.25183);
((GeneralPath)shape).lineTo(160.86618, 260.74402);
((GeneralPath)shape).lineTo(163.374, 260.74402);
((GeneralPath)shape).lineTo(163.374, 263.25183);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(163.74118, 263.25183);
((GeneralPath)shape).lineTo(163.74118, 261.4237);
((GeneralPath)shape).lineTo(163.86618, 261.25183);
((GeneralPath)shape).quadTo(164.79587, 260.00964, 165.77243, 259.15808);
((GeneralPath)shape).lineTo(166.71774, 258.32214);
((GeneralPath)shape).quadTo(169.05368, 256.25964, 169.05368, 254.70496);
((GeneralPath)shape).quadTo(169.05368, 252.89246, 166.99118, 252.89246);
((GeneralPath)shape).quadTo(165.71774, 252.89246, 164.03806, 253.75183);
((GeneralPath)shape).lineTo(164.03806, 252.05652);
((GeneralPath)shape).quadTo(165.80368, 251.40027, 167.45212, 251.40027);
((GeneralPath)shape).quadTo(169.29587, 251.40027, 170.40524, 252.27136);
((GeneralPath)shape).quadTo(171.51462, 253.14246, 171.51462, 254.6034);
((GeneralPath)shape).quadTo(171.51462, 255.6112, 170.97556, 256.43933);
((GeneralPath)shape).quadTo(170.4365, 257.26746, 169.07712, 258.3612);
((GeneralPath)shape).lineTo(168.29587, 258.9862);
((GeneralPath)shape).quadTo(166.66306, 260.2987, 166.35837, 261.4237);
((GeneralPath)shape).lineTo(171.46774, 261.4237);
((GeneralPath)shape).lineTo(171.46774, 263.25183);
((GeneralPath)shape).closePath();
paint = new Color(0, 128, 0, 255);
g.setPaint(paint);
g.fill(shape);
paint = new Color(0, 128, 0, 255);
stroke = new BasicStroke(1.0f,0,0,4.0f,null,0.0f);
g.setStroke(stroke);
g.setPaint(paint);
g.draw(shape);
origAlpha = alpha__0_1_4;
g.setTransform(defaultTransform__0_1_4);
g.setClip(clip__0_1_4);
float alpha__0_1_5 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_5 = g.getClip();
AffineTransform defaultTransform__0_1_5 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_5 is CompositeGraphicsNode
float alpha__0_1_5_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_5_0 = g.getClip();
AffineTransform defaultTransform__0_1_5_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_5_0 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new Rectangle2D.Double(116.65451049804688, 255.174072265625, 35.33734893798828, 9.297898292541504);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_5_0;
g.setTransform(defaultTransform__0_1_5_0);
g.setClip(clip__0_1_5_0);
float alpha__0_1_5_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1_5_1 = g.getClip();
AffineTransform defaultTransform__0_1_5_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1_5_1 is TextNode of 'SNAPSHOT'
shape = new GeneralPath();
((GeneralPath)shape).moveTo(117.81879, 263.62878);
((GeneralPath)shape).lineTo(117.81879, 262.88464);
((GeneralPath)shape).quadTo(118.51898, 263.1776, 119.01703, 263.1776);
((GeneralPath)shape).quadTo(119.36566, 263.1776, 119.566345, 263.02234);
((GeneralPath)shape).quadTo(119.76703, 262.86707, 119.76703, 262.60046);
((GeneralPath)shape).quadTo(119.76703, 262.38953, 119.64105, 262.2445);
((GeneralPath)shape).quadTo(119.515076, 262.0995, 119.16351, 261.90027);
((GeneralPath)shape).lineTo(118.873474, 261.73914);
((GeneralPath)shape).quadTo(118.29047, 261.411, 118.05023, 261.11072);
((GeneralPath)shape).quadTo(117.81, 260.81042, 117.81, 260.40906);
((GeneralPath)shape).quadTo(117.81, 259.87292, 118.20404, 259.54773);
((GeneralPath)shape).quadTo(118.59808, 259.22253, 119.245544, 259.22253);
((GeneralPath)shape).quadTo(119.7641, 259.22253, 120.41449, 259.37195);
((GeneralPath)shape).lineTo(120.41449, 260.06628);
((GeneralPath)shape).quadTo(119.74945, 259.81726, 119.36273, 259.81726);
((GeneralPath)shape).quadTo(119.0639, 259.81726, 118.88373, 259.95056);
((GeneralPath)shape).quadTo(118.70355, 260.08386, 118.70355, 260.30066);
((GeneralPath)shape).quadTo(118.70355, 260.47937, 118.82953, 260.61414);
((GeneralPath)shape).quadTo(118.955505, 260.7489, 119.30414, 260.9452);
((GeneralPath)shape).lineTo(119.614685, 261.11804);
((GeneralPath)shape).quadTo(120.24164, 261.4696, 120.47894, 261.7655);
((GeneralPath)shape).quadTo(120.71625, 262.0614, 120.71625, 262.4862);
((GeneralPath)shape).quadTo(120.71625, 263.08972, 120.268005, 263.4325);
((GeneralPath)shape).quadTo(119.81976, 263.77527, 119.03168, 263.77527);
((GeneralPath)shape).quadTo(118.51019, 263.77527, 117.81879, 263.62878);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(120.989685, 263.66687);
((GeneralPath)shape).lineTo(120.989685, 259.33093);
((GeneralPath)shape).lineTo(121.78363, 259.33093);
((GeneralPath)shape).lineTo(123.793396, 262.29285);
((GeneralPath)shape).lineTo(123.793396, 259.33093);
((GeneralPath)shape).lineTo(124.51703, 259.33093);
((GeneralPath)shape).lineTo(124.51703, 263.66687);
((GeneralPath)shape).lineTo(123.708435, 263.66687);
((GeneralPath)shape).lineTo(121.71332, 260.70496);
((GeneralPath)shape).lineTo(121.71332, 263.66687);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(124.6264, 263.66687);
((GeneralPath)shape).lineTo(126.3139, 259.33093);
((GeneralPath)shape).lineTo(127.21625, 259.33093);
((GeneralPath)shape).lineTo(128.90375, 263.66687);
((GeneralPath)shape).lineTo(127.97797, 263.66687);
((GeneralPath)shape).lineTo(127.526794, 262.50964);
((GeneralPath)shape).lineTo(125.733826, 262.50964);
((GeneralPath)shape).lineTo(125.28265, 263.66687);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(125.95648, 261.9325);
((GeneralPath)shape).lineTo(127.30414, 261.9325);
((GeneralPath)shape).lineTo(126.63031, 260.20398);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(129.01898, 263.66687);
((GeneralPath)shape).lineTo(129.01898, 259.33093);
((GeneralPath)shape).lineTo(130.5307, 259.33093);
((GeneralPath)shape).quadTo(131.32172, 259.33093, 131.6601, 259.59607);
((GeneralPath)shape).quadTo(131.99847, 259.8612, 131.99847, 260.4823);
((GeneralPath)shape).quadTo(131.99847, 261.18542, 131.54584, 261.57654);
((GeneralPath)shape).quadTo(131.0932, 261.96765, 130.27875, 261.96765);
((GeneralPath)shape).lineTo(129.90375, 261.96765);
((GeneralPath)shape).lineTo(129.90375, 263.66687);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(129.90375, 261.37292);
((GeneralPath)shape).lineTo(130.07953, 261.37292);
((GeneralPath)shape).quadTo(130.54242, 261.37292, 130.81049, 261.1576);
((GeneralPath)shape).quadTo(131.07855, 260.94226, 131.07855, 260.57312);
((GeneralPath)shape).quadTo(131.07855, 259.92566, 130.25531, 259.92566);
((GeneralPath)shape).lineTo(129.90375, 259.92566);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(131.9555, 263.62878);
((GeneralPath)shape).lineTo(131.9555, 262.88464);
((GeneralPath)shape).quadTo(132.6557, 263.1776, 133.15375, 263.1776);
((GeneralPath)shape).quadTo(133.50238, 263.1776, 133.70306, 263.02234);
((GeneralPath)shape).quadTo(133.90375, 262.86707, 133.90375, 262.60046);
((GeneralPath)shape).quadTo(133.90375, 262.38953, 133.77777, 262.2445);
((GeneralPath)shape).quadTo(133.6518, 262.0995, 133.30023, 261.90027);
((GeneralPath)shape).lineTo(133.0102, 261.73914);
((GeneralPath)shape).quadTo(132.42719, 261.411, 132.18695, 261.11072);
((GeneralPath)shape).quadTo(131.94672, 260.81042, 131.94672, 260.40906);
((GeneralPath)shape).quadTo(131.94672, 259.87292, 132.34076, 259.54773);
((GeneralPath)shape).quadTo(132.7348, 259.22253, 133.38226, 259.22253);
((GeneralPath)shape).quadTo(133.90082, 259.22253, 134.55121, 259.37195);
((GeneralPath)shape).lineTo(134.55121, 260.06628);
((GeneralPath)shape).quadTo(133.88617, 259.81726, 133.49945, 259.81726);
((GeneralPath)shape).quadTo(133.20062, 259.81726, 133.02045, 259.95056);
((GeneralPath)shape).quadTo(132.84027, 260.08386, 132.84027, 260.30066);
((GeneralPath)shape).quadTo(132.84027, 260.47937, 132.96625, 260.61414);
((GeneralPath)shape).quadTo(133.09222, 260.7489, 133.44086, 260.9452);
((GeneralPath)shape).lineTo(133.7514, 261.11804);
((GeneralPath)shape).quadTo(134.37836, 261.4696, 134.61566, 261.7655);
((GeneralPath)shape).quadTo(134.85297, 262.0614, 134.85297, 262.4862);
((GeneralPath)shape).quadTo(134.85297, 263.08972, 134.40472, 263.4325);
((GeneralPath)shape).quadTo(133.95648, 263.77527, 133.1684, 263.77527);
((GeneralPath)shape).quadTo(132.64691, 263.77527, 131.9555, 263.62878);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(135.1264, 263.66687);
((GeneralPath)shape).lineTo(135.1264, 259.33093);
((GeneralPath)shape).lineTo(136.02875, 259.33093);
((GeneralPath)shape).lineTo(136.02875, 261.09167);
((GeneralPath)shape).lineTo(137.82172, 261.09167);
((GeneralPath)shape).lineTo(137.82172, 259.33093);
((GeneralPath)shape).lineTo(138.72406, 259.33093);
((GeneralPath)shape).lineTo(138.72406, 263.66687);
((GeneralPath)shape).lineTo(137.82172, 263.66687);
((GeneralPath)shape).lineTo(137.82172, 261.68933);
((GeneralPath)shape).lineTo(136.02875, 261.68933);
((GeneralPath)shape).lineTo(136.02875, 263.66687);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(141.20941, 263.77527);
((GeneralPath)shape).quadTo(140.2221, 263.77527, 139.63031, 263.15417);
((GeneralPath)shape).quadTo(139.03851, 262.53308, 139.03851, 261.4989);
((GeneralPath)shape).quadTo(139.03851, 260.453, 139.63324, 259.83777);
((GeneralPath)shape).quadTo(140.22797, 259.22253, 141.23871, 259.22253);
((GeneralPath)shape).quadTo(142.24359, 259.22253, 142.83978, 259.83777);
((GeneralPath)shape).quadTo(143.43597, 260.453, 143.43597, 261.4901);
((GeneralPath)shape).quadTo(143.43597, 262.55066, 142.83978, 263.16296);
((GeneralPath)shape).quadTo(142.24359, 263.77527, 141.20941, 263.77527);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(141.22113, 263.1776);
((GeneralPath)shape).quadTo(141.80121, 263.1776, 142.13959, 262.72205);
((GeneralPath)shape).quadTo(142.47797, 262.26648, 142.47797, 261.48718);
((GeneralPath)shape).quadTo(142.47797, 260.73132, 142.13812, 260.2743);
((GeneralPath)shape).quadTo(141.79828, 259.81726, 141.23871, 259.81726);
((GeneralPath)shape).quadTo(140.67328, 259.81726, 140.3349, 260.2743);
((GeneralPath)shape).quadTo(139.99652, 260.73132, 139.99652, 261.49597);
((GeneralPath)shape).quadTo(139.99652, 262.25476, 140.33344, 262.7162);
((GeneralPath)shape).quadTo(140.67035, 263.1776, 141.22113, 263.1776);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(144.8227, 263.66687);
((GeneralPath)shape).lineTo(144.8227, 259.92566);
((GeneralPath)shape).lineTo(143.26996, 259.92566);
((GeneralPath)shape).lineTo(143.26996, 259.33093);
((GeneralPath)shape).lineTo(147.2807, 259.33093);
((GeneralPath)shape).lineTo(147.2807, 259.92566);
((GeneralPath)shape).lineTo(145.72797, 259.92566);
((GeneralPath)shape).lineTo(145.72797, 263.66687);
((GeneralPath)shape).closePath();
paint = new Color(0, 128, 0, 255);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_1_5_1;
g.setTransform(defaultTransform__0_1_5_1);
g.setClip(clip__0_1_5_1);
origAlpha = alpha__0_1_5;
g.setTransform(defaultTransform__0_1_5);
g.setClip(clip__0_1_5);
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

