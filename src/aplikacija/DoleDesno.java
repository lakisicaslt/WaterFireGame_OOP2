package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** L-bend pipe: connects DOWN and RIGHT */
public class DoleDesno extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.DOWN, Direction.RIGHT);
    }
}
