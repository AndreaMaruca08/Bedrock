package render;

import scan.BedrockNode;

import java.util.*;

public class TreemapLayout {

    private static final float MIN_PIXEL_AREA = 0.0f;

    private final List<Rect> result = new ArrayList<>();

    public List<Rect> layout(BedrockNode root, float x, float y, float w, float h) {
        result.clear();
        squarify(root, x, y, w, h);
        return result;
    }

    private void squarify(BedrockNode node, float x, float y, float w, float h) {
        result.add(new Rect(x, y, w, h, node));

        if (node.children == null || node.children.isEmpty()) return;
        if (w * h < MIN_PIXEL_AREA) return;

        List<BedrockNode> sorted = new ArrayList<>(node.children);
        sorted.sort((a, b) -> Long.compare(b.totalSize, a.totalSize));

        float totalArea = w * h;
        long totalSize = node.totalSize == 0 ? 1 : node.totalSize;

        float[] areas = new float[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            areas[i] = (sorted.get(i).totalSize / (float) totalSize) * totalArea;
        }

        squarifyRows(sorted, areas, 0, x, y, w, h);
    }

    private void squarifyRows(List<BedrockNode> children, float[] areas, int startIdx,
                               float x, float y, float w, float h) {
        int n = children.size();
        if (startIdx >= n) return;

        float L = Math.min(w, h);
        List<Integer> row = new ArrayList<>();
        row.add(startIdx);
        float rowSum = areas[startIdx];

        int i = startIdx + 1;
        while (i < n) {
            float newSum = rowSum + areas[i];
            float currentWorst = worst(row, areas, rowSum, L);
            float nextWorst = worst(concat(row, i), areas, newSum, L);

            if (nextWorst >= currentWorst) {
                break; // aggiungere peggiora la riga, fermati qui
            }
            row.add(i);
            rowSum = newSum;
            i++;
        }

        boolean horizontal = w >= h;
        float rowThickness = rowSum / L;

        float rx = x, ry = y;
        for (int idx : row) {
            BedrockNode child = children.get(idx);
            float area = areas[idx];
            float length = area / rowThickness;

            if (horizontal) {
                squarify(child, rx, ry, rowThickness, length);
                ry += length;
            } else {
                squarify(child, rx, ry, length, rowThickness);
                rx += length;
            }
        }

        float nx = horizontal ? x + rowThickness : x;
        float ny = horizontal ? y : y + rowThickness;
        float nw = horizontal ? w - rowThickness : w;
        float nh = horizontal ? h : h - rowThickness;

        squarifyRows(children, areas, i, nx, ny, nw, nh);
    }

    private float worst(List<Integer> row, float[] areas, float sum, float L) {
        float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        for (int idx : row) {
            max = Math.max(max, areas[idx]);
            min = Math.min(min, areas[idx]);
        }
        float L2 = L * L;
        float s2 = sum * sum;
        return Math.max((L2 * max) / s2, s2 / (L2 * min));
    }

    private List<Integer> concat(List<Integer> row, int idx) {
        List<Integer> copy = new ArrayList<>(row);
        copy.add(idx);
        return copy;
    }
}