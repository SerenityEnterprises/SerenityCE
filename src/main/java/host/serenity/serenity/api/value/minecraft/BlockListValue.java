package host.serenity.serenity.api.value.minecraft;

import host.serenity.serenity.api.value.Value;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockListValue extends Value<List<String>> {
    public BlockListValue(String name, List<String> list) {
        super(name, new ArrayList<>(list));
    }

    public void addBlock(Block block) {
        getValue().add(getStringFromBlock(block));
    }

    public void removeBlock(Block block) {
        getValue().remove(getStringFromBlock(block));
    }

    public boolean containsBlock(Block block) {
        return getValue().contains(getStringFromBlock(block));
    }

    private String getStringFromBlock(Block block) {
        return Block.blockRegistry.getNameForObject(block).toString();
    }

    private Block getBlockFromString(String string) {
        return (Block) Block.blockRegistry.getObject(string);
    }

    @Override
    public void setValueFromString(String string) {}
}
