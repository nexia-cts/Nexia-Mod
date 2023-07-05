package com.nexia.world.structure;

import com.combatreforged.factory.api.util.Identifier;

import java.io.File;

public class MapUtils {
    public boolean deleteStructure(Identifier identifier) {
        // namespace, id
        return new File(identifier.getId() + ".json", String.format("/world/generated/%s/structures", identifier.getNamespace())).delete();
    }
}
