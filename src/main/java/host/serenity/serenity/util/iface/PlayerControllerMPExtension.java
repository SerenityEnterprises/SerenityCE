package host.serenity.serenity.util.iface;

import net.minecraft.util.BlockPos;

public interface PlayerControllerMPExtension {
    int getBlockHitDelay();
    void setBlockHitDelay(int blockHitDelay);

    float getCurBlockDamageMP();
    void setCurBlockDamageMP(float curBlockDamageMP);

    BlockPos getCurrentBlock();
}
