package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComboBoxRenderer extends DefaultListCellRenderer {

    private Map<String, ImageIcon> iconMap = new HashMap<>();
    private Color background = new Color(0, 100, 255, 15);
    private Color defaultBackground = (Color) UIManager.get("List.background");
    private List<String> languages = new ArrayList<>();

    public ComboBoxRenderer() {
        languages.add("RO");
        languages.add("EN");
        languages.add("IT");
        Image it = new ImageIcon(getClass().getResource("/images/it.png")).getImage().getScaledInstance(20,14, Image.SCALE_DEFAULT);
        Image en = new ImageIcon(getClass().getResource("/images/en.png")).getImage().getScaledInstance(20,14, Image.SCALE_DEFAULT);
        Image ro = new ImageIcon(getClass().getResource("/images/ro.png")).getImage().getScaledInstance(20,14, Image.SCALE_DEFAULT);

        iconMap.put("RO", new ImageIcon(ro));
        iconMap.put("EN", new ImageIcon(en));
        iconMap.put("IT", new ImageIcon(it));
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        this.setText((String)value);
        this.setIcon(iconMap.get((String)value));
        if (!isSelected) {
            this.setBackground(index % 2 == 0 ? background : defaultBackground);
        }
        return this;
    }
}