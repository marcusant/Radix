package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.values.world.block.BlockChangeRecord;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

public class MultiBlockChangeHandler implements PacketHandler<ServerMultiBlockChangePacket> {

    private final RadixClient game;

    public MultiBlockChangeHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerMultiBlockChangePacket packet) {
        Gdx.app.debug("", "ServerMultiBlockChangePacket"
                + ", recordsLength=" + packet.getRecords().length);

        for(BlockChangeRecord r : packet.getRecords()) {
            int x = r.getPosition().getX();
            int y = r.getPosition().getY();
            int z = r.getPosition().getZ();
            int chunkRelativeX = x & (game.getWorld().getChunkSize()-1);
            int chunkRelativeZ = z & (game.getWorld().getChunkSize()-1);
            int block = r.getBlock();
            int id = block >> 4;
            int meta = block & 15;
            IChunk chunk = game.getWorld().getChunk(x, z);
            if(chunk != null) {
                if (id > 0) {
                    boolean blockExists = RadixAPI.instance.getBlocks()[id] != null;
                    try {
                        chunk.setBlock(blockExists? id : BuiltInBlockIds.UNKNOWN_ID, chunkRelativeX, y, chunkRelativeZ);
                        chunk.setMeta((short) (blockExists ? meta : 0), chunkRelativeX, y, chunkRelativeZ);
                    } catch (CoordinatesOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        chunk.removeBlock(chunkRelativeX, y, chunkRelativeZ);
                        chunk.setMeta((short) 0, chunkRelativeX, y, chunkRelativeZ);
                    } catch (CoordinatesOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Gdx.graphics.requestRendering();
    }

}
