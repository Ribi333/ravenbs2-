package keystrokesmod.script.packet.serverbound;

import net.minecraft.network.play.client.C0APacketAnimation;

public class C0A extends CPacket {
    public C0A(C0APacketAnimation packet) {
        super(packet);
    }

    public C0A() {
        super(new C0APacketAnimation());
    }
}
