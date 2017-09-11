package BoolNets;

import java.util.UUID;

/**
 * Base class for {@link Identifier_Node}. Previous versions had multiple Identifier types extending this one. This
 * class may no longer be required.
 */
public class Identifier{
    private UUID uuid = UUID.randomUUID();
    private String label = null;

//TODO Ensure that labels are unique

    public Identifier(String label){ this.label = label;}

    public UUID getUUID() {
        return uuid;
    }

    public String getLabel() {
        return label;
    }


}
