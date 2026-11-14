package GameRun;

public final class Viewport {
    public static final int WIDTH = 1900;
    public static final int HEIGHT = 1000;

    private Viewport() {
    }

    public static double scaleFor(int panelWidth, int panelHeight) {
        if (panelWidth <= 0 || panelHeight <= 0) return 1.0;
        return Math.min((double) panelWidth / WIDTH, (double) panelHeight / HEIGHT);
    }

    public static int offsetXFor(int panelWidth, int panelHeight) {
        double scale = scaleFor(panelWidth, panelHeight);
        return (int) Math.round((panelWidth - WIDTH * scale) / 2.0);
    }

    public static int offsetYFor(int panelWidth, int panelHeight) {
        double scale = scaleFor(panelWidth, panelHeight);
        return (int) Math.round((panelHeight - HEIGHT * scale) / 2.0);
    }

    public static int toLogicalX(int panelX, int panelWidth, int panelHeight) {
        return (int) Math.floor((panelX - offsetXFor(panelWidth, panelHeight))
                / scaleFor(panelWidth, panelHeight));
    }

    public static int toLogicalY(int panelY, int panelWidth, int panelHeight) {
        return (int) Math.floor((panelY - offsetYFor(panelWidth, panelHeight))
                / scaleFor(panelWidth, panelHeight));
    }
}
