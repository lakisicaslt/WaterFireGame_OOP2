package aplikacija;

import java.util.EnumSet;
import java.util.Set;

/** L-bend pipe: connects LEFT and UP */
public class LevoGore extends Cev {

    @Override
    public Set<Direction> getConnections() {
        return EnumSet.of(Direction.LEFT, Direction.UP);
    }
}
