package fr.formiko.tools;

public class Math {
    public static double getDistanceBetweenPoints(int x1, int y1, int x2, int y2) {
        return getDistanceBetweenPoints((double) x1, (double) y1, (double) x2, (double) y2);
    }

    public static double getDistanceBetweenPoints(float x1, float y1, float x2, float y2) {
        return getDistanceBetweenPoints((double) x1, (double) y1, (double) x2, (double) y2);
    }

    public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return java.lang.Math.sqrt(
                java.lang.Math.pow((y2 - y1), 2)
                        + java.lang.Math.pow((x2 - x1), 2));
    }
}
