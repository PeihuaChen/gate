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
((GeneralPath)shape).moveTo(152.15785, 251.58516);
((GeneralPath)shape).lineTo(160.9443, 251.58516);
((GeneralPath)shape).lineTo(160.9443, 253.28308);
((GeneralPath)shape).lineTo(156.39743, 263.25183);
((GeneralPath)shape).lineTo(153.47035, 263.25183);
((GeneralPath)shape).lineTo(157.77243, 253.7987);
((GeneralPath)shape).lineTo(152.15785, 253.7987);
((GeneralPath)shape).lineTo(152.15785, 251.58516);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(162.35056, 260.2258);
((GeneralPath)shape).lineTo(165.16306, 260.2258);
((GeneralPath)shape).lineTo(165.16306, 263.25183);
((GeneralPath)shape).lineTo(162.35056, 263.25183);
((GeneralPath)shape).lineTo(162.35056, 260.2258);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(169.90263, 261.0383);
((GeneralPath)shape).lineTo(175.03806, 261.0383);
((GeneralPath)shape).lineTo(175.03806, 263.25183);
((GeneralPath)shape).lineTo(166.55888, 263.25183);
((GeneralPath)shape).lineTo(166.55888, 261.0383);
((GeneralPath)shape).lineTo(170.8193, 257.28308);
((GeneralPath)shape).quadTo(171.38701, 256.76746, 171.66045, 256.27527);
((GeneralPath)shape).quadTo(171.93388, 255.78308, 171.93388, 255.25183);
((GeneralPath)shape).quadTo(171.93388, 254.42891, 171.38441, 253.92891);
((GeneralPath)shape).quadTo(170.83493, 253.42891, 169.91826, 253.42891);
((GeneralPath)shape).quadTo(169.21513, 253.42891, 168.3792, 253.731);
((GeneralPath)shape).quadTo(167.54326, 254.03308, 166.59013, 254.62683);
((GeneralPath)shape).lineTo(166.59013, 252.06433);
((GeneralPath)shape).quadTo(167.60576, 251.72578, 168.59795, 251.55132);
((GeneralPath)shape).quadTo(169.59013, 251.37683, 170.54326, 251.37683);
((GeneralPath)shape).quadTo(172.63701, 251.37683, 173.79848, 252.2987);
((GeneralPath)shape).quadTo(174.95993, 253.22058, 174.95993, 254.86641);
((GeneralPath)shape).quadTo(174.95993, 255.81953, 174.46774, 256.64505);
((GeneralPath)shape).quadTo(173.97556, 257.47058, 172.39743, 258.8508);
((GeneralPath)shape).lineTo(169.90263, 261.0383);
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
((GeneralPath)shape).moveTo(121.11566, 259.43054);
((GeneralPath)shape).lineTo(121.11566, 260.35632);
((GeneralPath)shape).quadTo(120.75433, 260.1942, 120.41156, 260.11218);
((GeneralPath)shape).quadTo(120.06879, 260.03015, 119.7641, 260.03015);
((GeneralPath)shape).quadTo(119.3598, 260.03015, 119.16644, 260.14148);
((GeneralPath)shape).quadTo(118.97308, 260.2528, 118.97308, 260.48718);
((GeneralPath)shape).quadTo(118.97308, 260.66296, 119.10394, 260.7616);
((GeneralPath)shape).quadTo(119.2348, 260.86023, 119.5766, 260.93054);
((GeneralPath)shape).lineTo(120.05707, 261.02625);
((GeneralPath)shape).quadTo(120.78754, 261.17273, 121.09515, 261.47156);
((GeneralPath)shape).quadTo(121.40277, 261.7704, 121.40277, 262.32117);
((GeneralPath)shape).quadTo(121.40277, 263.04578, 120.97308, 263.3993);
((GeneralPath)shape).quadTo(120.543396, 263.7528, 119.66254, 263.7528);
((GeneralPath)shape).quadTo(119.24652, 263.7528, 118.827576, 263.67273);
((GeneralPath)shape).quadTo(118.40863, 263.59265, 117.98871, 263.43835);
((GeneralPath)shape).lineTo(117.98871, 262.48523);
((GeneralPath)shape).quadTo(118.40863, 262.7079, 118.799255, 262.82117);
((GeneralPath)shape).quadTo(119.18988, 262.93445, 119.55316, 262.93445);
((GeneralPath)shape).quadTo(119.9223, 262.93445, 120.11859, 262.8114);
((GeneralPath)shape).quadTo(120.31488, 262.68835, 120.31488, 262.45984);
((GeneralPath)shape).quadTo(120.31488, 262.25476, 120.18207, 262.14343);
((GeneralPath)shape).quadTo(120.049255, 262.0321, 119.65082, 261.9442);
((GeneralPath)shape).lineTo(119.21332, 261.84656);
((GeneralPath)shape).quadTo(118.55707, 261.70593, 118.25433, 261.39832);
((GeneralPath)shape).quadTo(117.9516, 261.0907, 117.9516, 260.5692);
((GeneralPath)shape).quadTo(117.9516, 259.91687, 118.373474, 259.5653);
((GeneralPath)shape).quadTo(118.79535, 259.21375, 119.586365, 259.21375);
((GeneralPath)shape).quadTo(119.94574, 259.21375, 120.3266, 259.26746);
((GeneralPath)shape).quadTo(120.70746, 259.32117, 121.11566, 259.43054);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(121.89105, 259.29187);
((GeneralPath)shape).lineTo(123.15082, 259.29187);
((GeneralPath)shape).lineTo(124.742615, 262.29187);
((GeneralPath)shape).lineTo(124.742615, 259.29187);
((GeneralPath)shape).lineTo(125.810974, 259.29187);
((GeneralPath)shape).lineTo(125.810974, 263.66687);
((GeneralPath)shape).lineTo(124.55121, 263.66687);
((GeneralPath)shape).lineTo(122.961365, 260.66687);
((GeneralPath)shape).lineTo(122.961365, 263.66687);
((GeneralPath)shape).lineTo(121.89105, 263.66687);
((GeneralPath)shape).lineTo(121.89105, 259.29187);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(129.06683, 262.87);
((GeneralPath)shape).lineTo(127.30316, 262.87);
((GeneralPath)shape).lineTo(127.02582, 263.66687);
((GeneralPath)shape).lineTo(125.89105, 263.66687);
((GeneralPath)shape).lineTo(127.512146, 259.29187);
((GeneralPath)shape).lineTo(128.8559, 259.29187);
((GeneralPath)shape).lineTo(130.47699, 263.66687);
((GeneralPath)shape).lineTo(129.34222, 263.66687);
((GeneralPath)shape).lineTo(129.06683, 262.87);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(127.58441, 262.0575);
((GeneralPath)shape).lineTo(128.78363, 262.0575);
((GeneralPath)shape).lineTo(128.18597, 260.31726);
((GeneralPath)shape).lineTo(127.58441, 262.0575);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(130.55707, 259.29187);
((GeneralPath)shape).lineTo(132.43011, 259.29187);
((GeneralPath)shape).quadTo(133.2641, 259.29187, 133.71136, 259.66296);
((GeneralPath)shape).quadTo(134.15863, 260.03406, 134.15863, 260.7196);
((GeneralPath)shape).quadTo(134.15863, 261.4071, 133.71136, 261.7782);
((GeneralPath)shape).quadTo(133.2641, 262.1493, 132.43011, 262.1493);
((GeneralPath)shape).lineTo(131.68597, 262.1493);
((GeneralPath)shape).lineTo(131.68597, 263.66687);
((GeneralPath)shape).lineTo(130.55707, 263.66687);
((GeneralPath)shape).lineTo(130.55707, 259.29187);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(131.68597, 260.11023);
((GeneralPath)shape).lineTo(131.68597, 261.33093);
((GeneralPath)shape).lineTo(132.30902, 261.33093);
((GeneralPath)shape).quadTo(132.63715, 261.33093, 132.81586, 261.17175);
((GeneralPath)shape).quadTo(132.99457, 261.01257, 132.99457, 260.7196);
((GeneralPath)shape).quadTo(132.99457, 260.42664, 132.81586, 260.26843);
((GeneralPath)shape).quadTo(132.63715, 260.11023, 132.30902, 260.11023);
((GeneralPath)shape).lineTo(131.68597, 260.11023);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(137.49945, 259.43054);
((GeneralPath)shape).lineTo(137.49945, 260.35632);
((GeneralPath)shape).quadTo(137.13812, 260.1942, 136.79535, 260.11218);
((GeneralPath)shape).quadTo(136.45258, 260.03015, 136.14789, 260.03015);
((GeneralPath)shape).quadTo(135.74359, 260.03015, 135.55023, 260.14148);
((GeneralPath)shape).quadTo(135.35687, 260.2528, 135.35687, 260.48718);
((GeneralPath)shape).quadTo(135.35687, 260.66296, 135.48773, 260.7616);
((GeneralPath)shape).quadTo(135.61859, 260.86023, 135.96039, 260.93054);
((GeneralPath)shape).lineTo(136.44086, 261.02625);
((GeneralPath)shape).quadTo(137.17133, 261.17273, 137.47894, 261.47156);
((GeneralPath)shape).quadTo(137.78656, 261.7704, 137.78656, 262.32117);
((GeneralPath)shape).quadTo(137.78656, 263.04578, 137.35687, 263.3993);
((GeneralPath)shape).quadTo(136.92719, 263.7528, 136.04633, 263.7528);
((GeneralPath)shape).quadTo(135.63031, 263.7528, 135.21136, 263.67273);
((GeneralPath)shape).quadTo(134.79242, 263.59265, 134.3725, 263.43835);
((GeneralPath)shape).lineTo(134.3725, 262.48523);
((GeneralPath)shape).quadTo(134.79242, 262.7079, 135.18304, 262.82117);
((GeneralPath)shape).quadTo(135.57367, 262.93445, 135.93695, 262.93445);
((GeneralPath)shape).quadTo(136.30609, 262.93445, 136.50238, 262.8114);
((GeneralPath)shape).quadTo(136.69867, 262.68835, 136.69867, 262.45984);
((GeneralPath)shape).quadTo(136.69867, 262.25476, 136.56586, 262.14343);
((GeneralPath)shape).quadTo(136.43304, 262.0321, 136.0346, 261.9442);
((GeneralPath)shape).lineTo(135.5971, 261.84656);
((GeneralPath)shape).quadTo(134.94086, 261.70593, 134.63812, 261.39832);
((GeneralPath)shape).quadTo(134.33539, 261.0907, 134.33539, 260.5692);
((GeneralPath)shape).quadTo(134.33539, 259.91687, 134.75726, 259.5653);
((GeneralPath)shape).quadTo(135.17914, 259.21375, 135.97015, 259.21375);
((GeneralPath)shape).quadTo(136.32953, 259.21375, 136.71039, 259.26746);
((GeneralPath)shape).quadTo(137.09125, 259.32117, 137.49945, 259.43054);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(138.27582, 259.29187);
((GeneralPath)shape).lineTo(139.40472, 259.29187);
((GeneralPath)shape).lineTo(139.40472, 260.95984);
((GeneralPath)shape).lineTo(141.06879, 260.95984);
((GeneralPath)shape).lineTo(141.06879, 259.29187);
((GeneralPath)shape).lineTo(142.19574, 259.29187);
((GeneralPath)shape).lineTo(142.19574, 263.66687);
((GeneralPath)shape).lineTo(141.06879, 263.66687);
((GeneralPath)shape).lineTo(141.06879, 261.8114);
((GeneralPath)shape).lineTo(139.40472, 261.8114);
((GeneralPath)shape).lineTo(139.40472, 263.66687);
((GeneralPath)shape).lineTo(138.27582, 263.66687);
((GeneralPath)shape).lineTo(138.27582, 259.29187);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(144.79535, 260.03015);
((GeneralPath)shape).quadTo(144.27972, 260.03015, 143.99554, 260.411);
((GeneralPath)shape).quadTo(143.71136, 260.79187, 143.71136, 261.48328);
((GeneralPath)shape).quadTo(143.71136, 262.17273, 143.99554, 262.5536);
((GeneralPath)shape).quadTo(144.27972, 262.93445, 144.79535, 262.93445);
((GeneralPath)shape).quadTo(145.31488, 262.93445, 145.59906, 262.5536);
((GeneralPath)shape).quadTo(145.88324, 262.17273, 145.88324, 261.48328);
((GeneralPath)shape).quadTo(145.88324, 260.79187, 145.59906, 260.411);
((GeneralPath)shape).quadTo(145.31488, 260.03015, 144.79535, 260.03015);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(144.79535, 259.21375);
((GeneralPath)shape).quadTo(145.85004, 259.21375, 146.4477, 259.81726);
((GeneralPath)shape).quadTo(147.04535, 260.42078, 147.04535, 261.48328);
((GeneralPath)shape).quadTo(147.04535, 262.54382, 146.4477, 263.14832);
((GeneralPath)shape).quadTo(145.85004, 263.7528, 144.79535, 263.7528);
((GeneralPath)shape).quadTo(143.74457, 263.7528, 143.14496, 263.14832);
((GeneralPath)shape).quadTo(142.54535, 262.54382, 142.54535, 261.48328);
((GeneralPath)shape).quadTo(142.54535, 260.42078, 143.14496, 259.81726);
((GeneralPath)shape).quadTo(143.74457, 259.21375, 144.79535, 259.21375);
((GeneralPath)shape).closePath();
((GeneralPath)shape).moveTo(146.87543, 259.29187);
((GeneralPath)shape).lineTo(150.90668, 259.29187);
((GeneralPath)shape).lineTo(150.90668, 260.1454);
((GeneralPath)shape).lineTo(149.45746, 260.1454);
((GeneralPath)shape).lineTo(149.45746, 263.66687);
((GeneralPath)shape).lineTo(148.32855, 263.66687);
((GeneralPath)shape).lineTo(148.32855, 260.1454);
((GeneralPath)shape).lineTo(146.87543, 260.1454);
((GeneralPath)shape).lineTo(146.87543, 259.29187);
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

