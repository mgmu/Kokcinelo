package fr.formiko.kokcinelo.tools;

import fr.formiko.kokcinelo.App;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * {@summary Tools to get Shapes that ShapeRenderer is not able to create.}
 * 
 * @author Hydrolien
 * @version 0.2
 * @since 0.2
 */
public class Shapes {
    private static ShapeRenderer shapeRenderer;

    public static void drawSky(int width, int heigth, float dark) {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
            shapeRenderer.setAutoShapeType(true);
        }
        // draw blue sky gradient
        shapeRenderer.begin();
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        Color topColor = new Color(0, 0.4f * dark, 1f * dark, 1);
        Color bottomColor = new Color(0, 0.8f * dark, 1f * dark, 1);
        shapeRenderer.rect(0, 0, width, heigth, bottomColor, bottomColor, topColor, topColor);
        shapeRenderer.end();
    }
    /**
     * {@summary Return a circle with a thik border.}
     * 
     * @param radius     radius of the circle
     * @param edgeLength length of the edge of the circle
     * @param color      color of the circle
     * @return a Pixmap with the circle in it
     */
    public static Texture getCircle(int radius, int edgeLength, int color) {
        return new Texture(getCirclePixmap(radius, edgeLength, color));
    }
    public static Texture getCircle(int radius, Color color) { return getCircle(radius, radius + 1, Color.rgba8888(color)); }

    /**
     * {@summary Return a circle with a thik border.}
     * 
     * @param radius     radius of the circle
     * @param edgeLength length of the edge of the circle
     * @param color      color of the circle
     * @return a Pixmap with the circle in it
     */
    public static Texture getCircle(int radius, int edgeLength, Color color) {
        return getCircle(radius, edgeLength, Color.rgba8888(color));
    }

    /**
     * {@summary Return a texture that fit into a circle.}
     * 
     * @param radius  radius of the circle
     * @param color   color of the circle
     * @param texture texture to put into the circle
     * @param zoom    zoom of the texture
     * @return a texture that fit into a circle
     */
    public static Texture getCircledTexture(int radius, Color color, Texture texture, float zoom) {
        App.log(0, "Start to create circled texture.");
        Pixmap pixmap = getCirclePixmap(radius, radius + 1, Color.rgba8888(color));
        // get image as pixmap
        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }
        Pixmap texturePixmap = texture.getTextureData().consumePixmap();
        int size = java.lang.Math.max(texturePixmap.getWidth(), texturePixmap.getHeight());
        int xOffset = (size - texturePixmap.getWidth()) / 2;
        int yOffset = (size - texturePixmap.getHeight()) / 2;
        // make square pixmap
        Pixmap squarePixmap;
        if (xOffset != 0 || yOffset != 0) {
            squarePixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            squarePixmap.drawPixmap(texturePixmap, 0, 0, texturePixmap.getWidth(), texturePixmap.getHeight(), xOffset, yOffset,
                    texturePixmap.getWidth(), texturePixmap.getHeight());
        } else {
            squarePixmap = texturePixmap;
        }

        // resize image
        Pixmap texturePixmapSized = new Pixmap((int) (pixmap.getWidth() * zoom), (int) (pixmap.getHeight() * zoom), Pixmap.Format.RGBA8888);
        texturePixmapSized.drawPixmap(squarePixmap, 0, 0, squarePixmap.getWidth(), squarePixmap.getHeight(), 0, 0,
                (int) (pixmap.getWidth() * zoom), (int) (pixmap.getHeight() * zoom));

        // draw center circle of the image
        int xCenter = (int) (pixmap.getWidth() / 2);
        int yCenter = (int) (pixmap.getHeight() / 2);
        xOffset = (int) (pixmap.getWidth() / 2 - texturePixmapSized.getWidth() / 2);
        yOffset = (int) (pixmap.getHeight() / 2 - texturePixmapSized.getHeight() / 2);

        for (int x = 0; x < texturePixmapSized.getWidth(); x++) {
            for (int y = 0; y < texturePixmapSized.getHeight(); y++) {
                int distToCenter = (int) Math.getDistanceBetweenPoints(xOffset + x, yOffset + y, xCenter, yCenter);
                // TODO in [distToCenter-0.5, distToCenter+0.5] color should be with alpha to make a smooth border
                if (distToCenter <= radius) {
                    pixmap.drawPixel(xOffset + x, yOffset + y, texturePixmapSized.getPixel(x, y));
                }
            }
        }
        App.log(0, "End to create circled texture.");
        return new Texture(pixmap);
    }

    /**
     * {@summary Return a Pixmap that include a circle.}
     * 
     * @param radius     radius of the circle
     * @param edgeLength size of the border line of the circle
     * @param color      color of the circle
     * @return a Pixmap that include a circle
     */
    private static Pixmap getCirclePixmap(int radius, int edgeLength, int color) {
        App.log(0, "Start generate circle");
        Pixmap pixmap = new Pixmap(radius * 2, radius * 2, Pixmap.Format.RGBA8888);
        int xCenter = (int) (pixmap.getWidth() / 2);
        int yCenter = (int) (pixmap.getHeight() / 2);

        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                int distToCenter = (int) Math.getDistanceBetweenPoints(x, y, xCenter, yCenter);
                // TODO in [distToCenter-0.5, distToCenter+0.5] color should be with alpha to make a smooth border
                if (distToCenter > radius - edgeLength && distToCenter <= radius) {
                    pixmap.drawPixel(x, y, color);
                }
            }
        }
        App.log(0, "End generate circle");
        return pixmap;
    }

}
