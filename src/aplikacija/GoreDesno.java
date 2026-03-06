package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** L-bend pipe: connects UP and RIGHT */
public class GoreDesno extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.UP, Direction.RIGHT);
    }
}
