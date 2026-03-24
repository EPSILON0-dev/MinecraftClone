package com.ee.Common;

public enum BlockType {
    Air,
    Stone,
    Cobblestone,
    Bedrock,
    Dirt,
    Grass,
    Sand,
    OakLog,
    OakLeaves;

    public static BlockType next(BlockType type) {
        var next = values()[(type.ordinal() + 1) % values().length];
        if (next == Air) {
            next = values()[(next.ordinal() + 1) % values().length];
        }
        return next;
    }

    public static BlockType previous(BlockType type) {
        var prev = values()[(type.ordinal() - 1 + values().length) % values().length];
        if (prev == Air) {
            prev = values()[(prev.ordinal() - 1 + values().length) % values().length];
        }
        return prev;
    }
}
