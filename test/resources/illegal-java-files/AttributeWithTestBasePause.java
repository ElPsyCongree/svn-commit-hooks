package rippledown.attribute;

public abstract class Attribute extends AttributeItem implements Translation.Translatable {

    public AttributeSample createAttributeSample(Sample value, boolean includeValues, boolean useFormatting) {
        TestBase.pause();
        return new AttributeSample(this, value, includeValues, useFormatting);
    }
}
