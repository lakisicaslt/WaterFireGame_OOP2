package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** Vertical pipe (reverse label): connects UP and DOWN */
public class DoleGore extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.UP, Direction.DOWN);
    }
}
