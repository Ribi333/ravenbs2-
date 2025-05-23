package keystrokesmod.module.impl.render;

import keystrokesmod.mixin.impl.accessor.IAccessorPlayerControllerMP;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class BreakProgress extends Module {
    private SliderSetting mode;
    private ButtonSetting manual;
    private ButtonSetting bedAura;
    private ButtonSetting fadeIn;

    private String[] modes = new String[] { "Percentage", "Second", "Decimal" };

    private float progress;
    private BlockPos block;
    private String progressStr;

    public BreakProgress() {
        super("BreakProgress", category.render);
        this.registerSetting(mode = new SliderSetting("Mode", 0, modes));
        this.registerSetting(manual = new ButtonSetting("Show manual", true));
        this.registerSetting(bedAura = new ButtonSetting("Show BedAura", true));
        this.registerSetting(fadeIn = new ButtonSetting("Fade in", false));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (this.progress == 0.0f || this.block == null || !Utils.nullCheck()) {
            return;
        }
        final double x = this.block.getX() + 0.5 - mc.getRenderManager().viewerPosX;
        final double y = this.block.getY() + 0.5 - mc.getRenderManager().viewerPosY;
        final double z = this.block.getZ() + 0.5 - mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((mc.gameSettings.thirdPersonView == 2 ? -1 : 1) * mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-0.02266667f, -0.02266667f, -0.02266667f);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_BLEND);
        int colorAlpha = Utils.mergeAlpha(-1, Math.max(10, (int) (255 * progress)));
        mc.fontRendererObj.drawString(this.progressStr, (float) (-mc.fontRendererObj.getStringWidth(this.progressStr) / 2), -3.0f, fadeIn.isToggled() ? colorAlpha : -1, true);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    private void setProgress() {
        switch ((int) mode.getInput()) {
            case 0: {
                this.progressStr = (int) (100.0 * (this.progress / 1.0)) + "%";
                break;
            }
            case 1: {
                double timeLeft = Utils.round((double) ((1.0f - this.progress) / BlockUtils.getBlockHardness(BlockUtils.getBlock(this.block), mc.thePlayer.getHeldItem(), false, false)) / 20.0, 1);
                this.progressStr = timeLeft == 0 ? "0" : timeLeft + "s";
                break;
            }
            case 2: {
                this.progressStr = String.valueOf(Utils.round(this.progress, 2));
                break;
            }
        }
    }

    public void onUpdate() {
        if (mc.thePlayer.capabilities.isCreativeMode || !mc.thePlayer.capabilities.allowEdit) {
            this.resetVariables();
            return;
        }
        if (bedAura.isToggled() && ModuleManager.bedAura != null && ModuleManager.bedAura.isEnabled() && ModuleManager.bedAura.breakProgress != 0.0f && ModuleManager.bedAura.currentBlock != null && !(BlockUtils.getBlock(ModuleManager.bedAura.currentBlock) instanceof BlockBed)) {
            this.progress = Math.min(1.0f, ModuleManager.bedAura.breakProgress);
            this.block = ModuleManager.bedAura.currentBlock;
            if (this.block == null) {
                return;
            }
            this.setProgress();
            return;
        }
        if (!manual.isToggled() || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            this.resetVariables();
            return;
        }
        this.progress = ((IAccessorPlayerControllerMP) mc.playerController).getCurBlockDamageMP();
        if (this.progress == 0.0f) {
            this.resetVariables();
            return;
        }
        this.block = mc.objectMouseOver.getBlockPos();
        this.setProgress();
    }

    public void onDisable() {
        this.resetVariables();
    }

    private void resetVariables() {
        this.progress = 0.0f;
        this.block = null;
        this.progressStr = "";
    }
}
