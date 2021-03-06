package me.dags.converter.datagen.block;

import me.dags.converter.datagen.writer.DataWriter;
import me.dags.converter.datagen.writer.ValueWriter;

import java.io.IOException;

public class BlockWriterLegacy implements ValueWriter<BlockData> {
    @Override
    public void write(BlockData block, DataWriter writer) throws IOException {
        writer.name(block.getName()).beginObject();
        {
            writer.name("id").value(block.getId());
            writer.name("upgrade").value(block.upgrade());
            if (BlockWriter.hasState(block)) {
                writer.name("default").value(block.getDefaultState().getName());
                writer.name("states").beginObject();
                {
                    for (StateData state : block.getStates()) {
                        writer.name(state.getName()).value(state.getId());
                    }
                }
                writer.endObject();
            }
        }
        writer.endObject();
    }
}
