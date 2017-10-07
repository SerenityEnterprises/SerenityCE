package host.serenity.serenity.mixin.core;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.modules.render.Wallhack;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow
    abstract int getColorIndex(int p_78909_1_);

    @Shadow
    private IntBuffer rawIntBuffer;

    @Shadow
    private int color;

    @Shadow
    private boolean needsUpdate;

    @Overwrite
    public void putColorMultiplier(float red, float green, float blue, int p_178978_4_) {
        int j = this.getColorIndex(p_178978_4_);
        int k = this.rawIntBuffer.get(j);
        int l;
        int i1;
        int j1;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            l = (int) ((float) (k & 255) * red);
            i1 = (int) ((float) (k >> 8 & 255) * green);
            j1 = (int) ((float) (k >> 16 & 255) * blue);
            try {
                if (Serenity.getInstance().getModuleManager().getModule(Wallhack.class).isEnabled()) {
                    k &= -0xC3000000;
                } else {
                    k &= -16777216;
                }
            } catch (Exception e) {
                k &= -16777216;
            }
            k |= j1 << 16 | i1 << 8 | l;
        } else {
            l = (int) ((float) (this.color >> 24 & 255) * red);
            i1 = (int) ((float) (this.color >> 16 & 255) * green);
            j1 = (int) ((float) (this.color >> 8 & 255) * blue);
            k &= 255;
            k |= l << 24 | i1 << 16 | j1 << 8;
        }

        if (this.needsUpdate) {
            k = -1;
        }

        this.rawIntBuffer.put(j, k);
    }
}
