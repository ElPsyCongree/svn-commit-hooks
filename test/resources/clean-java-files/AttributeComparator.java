package rippledown.attribute;

import org.jetbrains.annotations.NotNull;
import rippledown.util.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AttributeComparator implements Comparator<Attribute> {

    public static final String EMPTY_PANEL = "EMPTY_PANEL";

    private static final String CONCATENATION = "+";

    private String panel;
    private AttributeManager attributeManager;

    // the key value pairs, <panelCode+externalAttributeName, orderIndex>
    private Map<String, Integer> config;

    public static String generateConfigKey(String panelCode, String attribute) {
        if (StringUtils.isBlank(panelCode)) {
            panelCode = EMPTY_PANEL;
        }
        return panelCode + CONCATENATION + attribute;
    }

    public AttributeComparator(@NotNull String panel, @NotNull AttributeManager attributeManager, Map<String, Integer> config) {
        this.panel = panel;
        this.attributeManager = attributeManager;
        this.config = config != null ? config : new HashMap<String, Integer>();
    }

    @Override
    public int compare(Attribute a1, Attribute a2) {
        Integer orderA1 = getOrderIndex(a1);
        Integer orderA2 = getOrderIndex(a2);

        if (orderA1 == null && orderA2 == null) {
            return this.attributeManager.attributesComparator().compare(a1, a2);
        }
        if (orderA1 == null) {
            return 1;

        }
        if (orderA2 == null) {
            return -1;
        }
        return orderA1.compareTo(orderA2);
    }

    /**
     * Returns the order index for a given attribute.
     * This returns null if a given attribute is not primary attribute.
     * This returns null if a given attribute is not associated with any order index.
     */
    private Integer getOrderIndex(Attribute attribute) {
        if (!(attribute instanceof PrimaryAttribute)) {
            return null;
        }
        String externalName = this.attributeManager.externalName((PrimaryAttribute) attribute);
        String configKey = generateConfigKey(panel, externalName);
        return this.config.get(configKey);
    }

}