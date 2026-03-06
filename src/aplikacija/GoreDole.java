package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** Vertical pipe: connects UP and DOWN */
public class GoreDole extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.UP, Direction.DOWN);
    }
}
