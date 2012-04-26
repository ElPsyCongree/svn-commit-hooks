/*
 * Copyright (c) 2012 by Pacific Knowledge Systems Pty. Ltd.
 * Suite 309, 50 Holt St, Surry Hills, NSW, 2010 Australia
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of Pacific Knowledge Systems ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with PKS.
 */
package rippledown.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rippledown.condition.ConditionUserStrings;
import rippledown.condition.OnePlaceConditionMessages;
import rippledown.condition.SimpleConditionMessages;
import rippledown.condition.Values;
import rippledown.condition.semantictree.AttributeTree;
import rippledown.condition.semantictree.BasicAttributeTree;
import rippledown.format.SampleFormat;
import rippledown.rcase.*;
import rippledown.ruletree.interpret.StringWithVariables;
import rippledown.tools.persistent.ObjectId;
import rippledown.tools.persistent.basicstore.Manager;
import rippledown.tools.persistent.basicstore.Storable;
import rippledown.translation.Translation;
import rippledown.ui.UserStrings;
import rippledown.ui.editor.Editor;
import rippledown.util.StringUtils;
import rippledown.util.XML;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * An Attribute (eg "cholesterol") is used as the unique identifier for a set of values
 * in a case, and corresponding identifier in the rule conditions (eg "cholesterol > 6.0")
 * refering to those values.
 */
public abstract class Attribute extends AttributeItem implements Translation.Translatable {

    private static final String ILLEGAL_CHARACTERS_PATTERN = "[\\^,()-]";
    private static final UserStrings us = ConditionUserStrings.userStrings();

    static final String TAG_NAME = "Attribute";
    static final String DISPLAY_TYPE = "DisplayType";
    static final String COLOR = "Color";
    static final String DISPLAY_NAME = "DisplayName";
    static final String FORMAT = "Format";

    public static class DisplayType implements Serializable {
        public static final DisplayType IN_HEADER = new DisplayType(0);

        public static final DisplayType HIGHLIGHTED = new DisplayType(1);

        public static final DisplayType NORMAL = new DisplayType(2);

        public static final DisplayType HIDDEN = new DisplayType(3);

        public static final DisplayType ALWAYS_VISIBLE = new DisplayType(4);

        private int type;

        static DisplayType fromCode(String typeStr) {
            int i = Integer.parseInt(typeStr);
            switch (i) {
                case 0:
                    return IN_HEADER;
                case 1:
                    return HIGHLIGHTED;
                case 2:
                    return NORMAL;
                case 3:
                    return HIDDEN;
                case 4:
                    return ALWAYS_VISIBLE;
                default:
                    throw new IllegalArgumentException("Unknown DisplayType: " + i);
            }
        }

        private DisplayType(int type) {
            this.type = type;
        }

        public boolean equals(Object o) {
            return o instanceof DisplayType && ((DisplayType) o).type == type;
        }

        public int hashCode() {
            return type;
        }

        public String toString() {
            return "" + type;
        }
    }

    public AttributeSample createAttributeSample(Sample value, boolean includeValues, boolean useFormatting) {
        return new AttributeSample(this, value, includeValues, useFormatting);
    }

    private String displayName;
    private DisplayType displayType;
    public SampleFormat sampleFormat;
    private Color color;

    public static String canonicalFormForName(String name) {
        return name.trim().replaceAll(ILLEGAL_CHARACTERS_PATTERN, "_");
    }

    public BasicAttributeTree attributeTree() {
        return new AttributeTree(this);
    }

    /**
     * For the Manager
     */
    public Attribute() {
    }

    /**
     * @pre id != null
     * @pre name != null
     */
    public Attribute(ObjectId id, String name) {
        super(name);
        setId(id);
        displayType = DisplayType.NORMAL;
        sampleFormat = null;
    }

    public String displayName() {
        return displayName == null ? name() : displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public StringWithVariables untranslated() {
        return new StringWithVariables(displayName());
    }

    public StringWithVariables defaultValueForEditing() {
        return new StringWithVariables(Translation.NOT_TRANSLATED_TEXT + displayName());
    }

    public String toString() {
        return name();
    }

    public GroupAttribute owningGroup() {
        return null;
    }

    public boolean equivalent(Object obj) {
        return obj instanceof Attribute && ((Attribute) obj).name().equalsIgnoreCase(name());
    }

    public boolean isUserAttribute() {
        return false;
    }

    public boolean dependsOn(Storable other) {
        return false;
    }

    public boolean canBeReferredTo() {
        return true;
    }

    public boolean canReferTo(Attribute other) {
        return false;
    }

    public DisplayType displayType() {
        return displayType;
    }

    public boolean isHidden() {
        return DisplayType.HIDDEN.equals(displayType());
    }

    public
    @Nullable
    Color color() {
        return color;
    }

    public void setColor(@Nullable Color color) {
        this.color = color;
    }

    public boolean isEffectivelyEmpty(SampleSequence q) {
        try {
            return q.containsAllNullSamples();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Attribute.isEffectivelyEmpty failed for " + name());
            return false;
        }
    }

    public void setFormat(SampleFormat formatter) {
        sampleFormat = formatter;
    }

    public String formatForDisplay(Sample s, Translation translation, boolean isInCaseView) {
        if (sampleFormat == null) {
            return isInCaseView ? s.displayedString() : s.value();
        }
        return sampleFormat.format(s, translation, isInCaseView);
    }

    public SampleFormat formatter() {
        return sampleFormat;
    }

    /**
     * @pre type != null
     */
    public void setDisplayType(DisplayType type) {
        displayType = type;
    }

    /**
     * Provides ui for editing.
     *
     * @pre handler.owner() != null
     */
    public void displayForEditing(HandlerForEditing handler) {
    }

    @Override
    public void toGUI(EditorCallback callback, Window parent) {
    }

    public Values valuesCase(RDRCase kase) {
        return valuesForCaseForSample(kase, kase.cursor());
    }

    private Values valuesForCaseForSample(RDRCase kase, int sampleIndex) {
        Values values = new Values();
        if (kase.containsAttribute(this)) {
            values.add(this, kase.getSampleSequence(this).getSample(sampleIndex));
        } else {
            values.add(this, Sample.notInCaseSample());
        }
        return values;
    }

    public SampleSequence valuesSequence(RDRCase kase) {
        SampleSequenceBuilder ssb = new SampleSequenceBuilder();
        for (int i = 1; i <= kase.numberOfDates(); i++) {
            Values values = valuesForCaseForSample(kase, i);
            ssb.add(new ValuesSample(values));
        }
        return ssb.sampleSequence();
    }

    /**
     * True if this can have its values updated by the specified attribute
     *
     * @pre attribute != null
     */
    protected boolean isCompatibleUpdate(Attribute attribute) {
        return attribute.getClass().equals(getClass());
    }

    protected String stringSerialize() {
        Document doc = XML.createDocument();
        Element root = doc.createElement(TAG_NAME);
        doc.appendChild(root);
        setRootNodeAttributes(root, doc);
        return XML.writeDocument(doc);
    }

    protected void restore(String str, Manager manager) {
        if (XML.isXml(str)) {
            Document doc = XML.parse(str);
            Element root = doc.getDocumentElement();
            setNameDisplayTypeAndFormat(root, manager);
        } else {
            restoreForPre580(str, manager);
        }
    }

    @Deprecated
    private void restoreForPre580(String str, Manager manager) {
        //The format is either:
        // int name (pre 5.66)
        //or
        //formatCode|int name
        int firstSpace = str.indexOf(SPACE);
        String upToName = str.substring(0, firstSpace);
        String displayCode;
        String formatCode = null;
        int pipePos = upToName.indexOf('|');
        if (pipePos >= 0) {
            formatCode = upToName.substring(0, pipePos);
            displayCode = upToName.substring(pipePos + 1, firstSpace);
        } else {
            displayCode = upToName;
        }
        setDisplayType(DisplayType.fromCode(displayCode));
        sampleFormat = ((AttributeStore) manager).formatsManager().formatMatching(formatCode);
        setName(str.substring(firstSpace + 1));
    }

    void setRootNodeAttributes(Element root, Document document) {
        super.writeInto(root);//Name, order, parentId
        root.setAttribute(DISPLAY_TYPE, "" + displayType());
        if (color != null) root.setAttribute(COLOR, "" + color.getRGB());
        if (formatter() != null) {
            root.setAttribute(FORMAT, formatter().storageString());
        }
        //Only persist the display name if different to the name
        if (!name().equals(displayName())) XML.createAndAppendCDataElement(DISPLAY_NAME, root, document, displayName());
    }

    void setNameDisplayTypeAndFormat(Element root, Manager manager) {
        super.restoreFrom(root);
        setDisplayType(DisplayType.fromCode(root.getAttribute(DISPLAY_TYPE)));
        String colorRGB = root.getAttribute(COLOR);
        if (!StringUtils.isBlank(colorRGB)) setColor(new Color(Integer.parseInt(colorRGB)));
        String formatCode = root.getAttribute(FORMAT);
        setFormat(((AttributeStore) manager).formatsManager().formatMatching(formatCode));
        NodeList nodeList = root.getElementsByTagName(DISPLAY_NAME);
        if (nodeList.getLength() > 0) {
            Node displayNameNodeList = nodeList.item(0);
            if (displayNameNodeList.getChildNodes().getLength() > 0) {
                String displayNameInXML = displayNameNodeList.getFirstChild().getNodeValue();
                setDisplayName(displayNameInXML);
            }
        }
    }

    protected void makeEquivalentTo(Storable other) {
        super.makeEquivalentTo(other);
        Attribute attr = ((Attribute) other);
        displayType = attr.displayType;
        sampleFormat = attr.sampleFormat;
        displayName = attr.displayName;
        color = attr.color;
    }

    @NotNull
    public String toHTML(@Nullable RDRCase kase) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h3>");
        builder.append(name());
        builder.append("</h3>");
        if (kase != null) {
            if (kase.containsAttribute(this)) {
                SampleSequence sampleSequence = kase.getSampleSequence(this);
                String value = StringUtils.convertAngleBracketsToHTML(sampleSequence.toString());
                builder.append(us.formattedMessage(OnePlaceConditionMessages.EXPRESSION_EVALUATES_TO, value));
            } else {
                builder.append(us.message(SimpleConditionMessages.NOT_IN_CASE_MSG));
            }
        }
        return builder.toString();
    }


    @Override
    public boolean canBeAParent(@Nullable Editor.Item candidateChild) {
        return false;
    }

    @Override
    public Icon icon() {
        return AttributeEditor.iconForAttribute(this);
    }
}