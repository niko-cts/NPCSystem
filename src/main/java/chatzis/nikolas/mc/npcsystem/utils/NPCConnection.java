package chatzis.nikolas.mc.npcsystem.utils;

import chatzis.nikolas.mc.nikoapi.util.ReflectionHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import java.net.SocketAddress;

public class NPCConnection extends Connection {

    public NPCConnection(PacketFlow flag) {
        super(flag);
        channel = new NPCChannel(null);
        address = new SocketAddress() {
            //private static final long serialVersionUID = 8207338859896320185L;
        };
    }


    @Override
    public PacketFlow getReceiving() {
        return PacketFlow.SERVERBOUND;
    }

    public void flushChannel() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener genericfuturelistener) {
    }

    public void send(Packet<?> packet, PacketSendListener genericfuturelistener, boolean flag) {
    }

//    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocolinfo, T t0) {
//
//    }

    public void setListenerForServerboundHandshake(PacketListener pl) {
        ReflectionHelper.set(Connection.class, this, "q", pl);
        ReflectionHelper.set(Connection.class, this, "p", null);
    }
}