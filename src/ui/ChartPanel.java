package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartPanel extends JPanel {
    public enum ChartType { BAR, PIE }

    private ChartType chartType = ChartType.BAR;
    private String title = "Chart";
    private Map<String, Number> data;
    private final Color[] sliceColors = {
        new Color(41, 128, 185),
        new Color(39, 174, 96),
        new Color(243, 156, 18),
        new Color(231, 76, 60),
        new Color(142, 68, 173),
        new Color(22, 160, 133),
        new Color(211, 84, 0),
        new Color(52, 73, 94)
    };

    public ChartPanel(String title, ChartType type, Map<String, Number> data) {
        this.title = title;
        this.chartType = type;
        this.data = data;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(550, 320));
    }

    public void setData(String title, ChartType type, Map<String, Number> data) {
        this.title = title;
        this.chartType = type;
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = (width - fmTitle.stringWidth(title)) / 2;
        g2.drawString(title, Math.max(15, titleX), 25);

        if (data == null || data.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(Color.GRAY);
            g2.drawString("No data available for display", width / 2 - 70, height / 2);
            g2.dispose();
            return;
        }

        if (chartType == ChartType.BAR) {
            paintBarChart(g2, width, height);
        } else {
            paintPieChart(g2, width, height);
        }

        g2.dispose();
    }

    private void paintBarChart(Graphics2D g2, int width, int height) {
        int padLeft = 60;
        int padRight = 30;
        int padTop = 45;
        int padBottom = 50;

        int plotW = width - padLeft - padRight;
        int plotH = height - padTop - padBottom;

        double maxVal = 0;
        for (Number val : data.values()) {
            if (val.doubleValue() > maxVal) {
                maxVal = val.doubleValue();
            }
        }
        if (maxVal == 0) maxVal = 1;

        g2.setColor(new Color(200, 205, 210));
        g2.drawLine(padLeft, padTop + plotH, padLeft + plotW, padTop + plotH);
        g2.drawLine(padLeft, padTop, padLeft, padTop + plotH);

        int count = data.size();
        int barW = Math.max(16, (plotW / count) - 15);
        int gap = (plotW - (barW * count)) / (count + 1);

        int curX = padLeft + gap;
        int idx = 0;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            double v = entry.getValue().doubleValue();
            int barH = (int) ((v / maxVal) * (plotH - 25));
            int barY = padTop + plotH - barH;

            Color barCol = sliceColors[idx % sliceColors.length];
            g2.setColor(barCol);
            g2.fillRoundRect(curX, barY, barW, barH, 4, 4);

            g2.setColor(new Color(44, 62, 80));
            String valStr = String.valueOf(entry.getValue());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, curX + (barW - fm.stringWidth(valStr)) / 2, barY - 4);

            String label = entry.getKey();
            if (label.length() > 8) label = label.substring(0, 7) + "..";
            g2.drawString(label, curX + (barW - fm.stringWidth(label)) / 2, padTop + plotH + 18);

            curX += barW + gap;
            idx++;
        }
    }

    private void paintPieChart(Graphics2D g2, int width, int height) {
        double total = 0;
        for (Number val : data.values()) {
            total += val.doubleValue();
        }
        if (total == 0) return;

        int size = Math.min(width - 200, height - 70);
        int x = 30;
        int y = 45;

        double curAngle = 0;
        int idx = 0;

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            double v = entry.getValue().doubleValue();
            double arc = (v / total) * 360.0;

            g2.setColor(sliceColors[idx % sliceColors.length]);
            g2.fillArc(x, y, size, size, (int) Math.round(curAngle), (int) Math.ceil(arc));

            curAngle += arc;
            idx++;
        }

        int legX = x + size + 25;
        int legY = 60;
        idx = 0;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            g2.setColor(sliceColors[idx % sliceColors.length]);
            g2.fillRect(legX, legY - 10, 14, 14);

            g2.setColor(new Color(44, 62, 80));
            int pct = (int) Math.round((entry.getValue().doubleValue() / total) * 100);
            String item = entry.getKey() + ": " + entry.getValue() + " (" + pct + "%)";
            g2.drawString(item, legX + 22, legY + 2);

            legY += 24;
            idx++;
        }
    }
}
