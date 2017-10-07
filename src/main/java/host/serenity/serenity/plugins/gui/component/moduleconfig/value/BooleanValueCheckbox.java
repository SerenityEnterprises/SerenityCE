package host.serenity.serenity.plugins.gui.component.moduleconfig.value;

import host.serenity.serenity.api.gui.component.Checkbox;
import host.serenity.serenity.api.value.BooleanValue;

public class BooleanValueCheckbox extends Checkbox {
    private final BooleanValue booleanValue;

    public BooleanValueCheckbox(BooleanValue booleanValue) {
        super(booleanValue.getName());
        this.booleanValue = booleanValue;

        setChecked(booleanValue.getValue());
    }

    @Override
    protected void onChecked(boolean checked) {
        booleanValue.setValue(checked);
    }
}
