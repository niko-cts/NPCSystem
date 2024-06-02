package chatzis.nikolas.mc.npcsystem.utils;

import org.bukkit.Location;

import java.util.*;

/**
 * This class is for testing and will be redesigned in the future.
 * it is planned that the NPC's will walk a route.
 * @author Niko
 * @since 0.0.1
 */
public class Pathfinding {

    private final Location start;
    private final Location end;
    private final List<Location> pathLocs;
    private final Set<Location> blockedLocs;
    protected long time;
    protected int runs;
    protected int maxRuns;
    
    /**
     * Instantiates the Pathfinding algo.
     * @param start Location - Startlocation of the path
     * @param end Location - Endlocation of the path
     * @since 0.0.1
     */
    public Pathfinding(Location start, Location end) {
        this.start = start;
        this.end = end;
        this.pathLocs = new ArrayList<>();
        this.blockedLocs = new HashSet<>();
    }

    /**
     * Calculates the path from the start Location to the end Location.
     * The maximum path will be the distance between start and end times 10.
     * It is only possible to go up by plus/miuns one Y coordination at a step.
     * Also it's possible to move diagonal.
     * @return List<Location> including all Location to the paths (+ start and end). Returns a empty list, when no path was found
     * @since 0.0.1
     */
    public List<Location> getPath() {
        this.time = System.currentTimeMillis();
        this.pathLocs.add(start);
        this.maxRuns = (int) Math.round(start.distance(end) * 10);
        this.runs = 0;

        Location current = start;
        while (current != null && !isEnd(current)) {
            runs++;
            if (runs > maxRuns)
                break;


            HashMap<Location, Double> locationValue = new HashMap<>();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {

                    Location clone = current.clone();
                    clone.add(x, 0, z);

                    if (!clone.clone().subtract(0, 1, 0).getBlock().getType().isSolid())
                        clone.subtract(0, 1, 0);

                    if (clone.getBlock().getType().isSolid()) {
                        clone.add(0, 1, 0);

                        if (!clone.getBlock().getType().isSolid() && !clone.clone().add(0, 1, 0).getBlock().getType().isSolid())
                            locationValue.put(clone, clone.distance(start) + clone.distance(end) * 10);

                    } else
                        locationValue.put(clone, clone.distance(start) + clone.distance(end) * 10);
                }
            }

            List<Map.Entry<Location, Double>> list = new ArrayList<>(locationValue.entrySet());
            list.sort(Comparator.comparingDouble(Map.Entry::getValue));

            short i = 0;
            for (Map.Entry<Location, Double> locationDoubleEntry : list) {
                Location loc = locationDoubleEntry.getKey();
                if (hasSame(blockedLocs, loc) || hasSame(pathLocs, loc)) continue;
                current = loc;
                i = 1;
                break;
            }

            if (i == 0) {
                blockedLocs.add(current); // current has no valid path
                pathLocs.remove(current); // Remove the current from path -> doesnt contain valid path
                if (pathLocs.isEmpty())
                    return new ArrayList<>();

                current = pathLocs.get(pathLocs.size() - 1);
            } else
                pathLocs.add(current);
        }

        time -= System.currentTimeMillis();
        return pathLocs;
    }

    /**
     * Checks if a location is included in a list.
     * @param list List<Location> - List to look up
     * @param loc Location - Location to check
     * @return boolean - Location loc is in the List
     * @since 0.0.1
     */
    private boolean hasSame(Set<Location> list, Location loc) {
        for (Location location : list) {
            if (location.getBlockX() == loc.getBlockX() && location.getBlockY() == loc.getBlockY()
                    && location.getBlockZ() == loc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a location is included in a list.
     * @param list List<Location> - List to look up
     * @param loc Location - Location to check
     * @return boolean - Location loc is in the List
     * @since 0.0.1
     */
    private boolean hasSame(List<Location> list, Location loc) {
        for (Location location : list) {
            if (location.getBlockX() == loc.getBlockX() && location.getBlockY() == loc.getBlockY()
                    && location.getBlockZ() == loc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param current Location - the Location to check
     * @return true if current Location is the end location
     * @since 0.0.1
     */
    private boolean isEnd(Location current) {
        return current.getBlockX() == end.getBlockX() &&
                current.getBlockY() == end.getBlockY() &&
                current.getBlockZ() == end.getBlockZ();
    }

}