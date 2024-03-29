package com.nexia.world.structure;

import com.combatreforged.metis.api.util.Identifier;

import java.io.File;

public class MapUtils {
    public boolean deleteStructure(Identifier identifier) {
        return new File(String.format("/world/generated/%s/structures", identifier.getNamespace()), identifier.getId() + ".nbt").delete();
    }
}
