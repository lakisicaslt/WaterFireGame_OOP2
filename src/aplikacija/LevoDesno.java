package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** Horizontal pipe: connects LEFT and RIGHT */
public class LevoDesno extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.LEFT, Direction.RIGHT);
    }
}
