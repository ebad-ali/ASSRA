package assra.bahria.fyp.Common.Models;

import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Directions;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.DistanceMatrixResult;

public class CombinedDirectionAndDuration {
    private Directions directions;
    private DistanceMatrixResult distanceMatrixResult;

    public CombinedDirectionAndDuration(Directions directions, DistanceMatrixResult distanceMatrixResult) {
        this.directions = directions;
        this.distanceMatrixResult = distanceMatrixResult;
    }

    public Directions getDirections() {
        return directions;
    }

    public void setDirections(Directions directions) {
        this.directions = directions;
    }

    public DistanceMatrixResult getDistanceMatrixResult() {
        return distanceMatrixResult;
    }

    public void setDistanceMatrixResult(DistanceMatrixResult distanceMatrixResult) {
        this.distanceMatrixResult = distanceMatrixResult;
    }
}
