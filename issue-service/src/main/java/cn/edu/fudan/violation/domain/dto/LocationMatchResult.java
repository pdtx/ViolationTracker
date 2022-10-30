package cn.edu.fudan.violation.domain.dto;

import cn.edu.fudan.violation.domain.dbo.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMatchResult {

    String matchedLocationId;
    Boolean bestMatch;
    Double matchingDegree;

    Location location;

    public static LocationMatchResult newInstance(Location location2, double matchDegree) {
        return LocationMatchResult.builder().location(location2)
                .matchedLocationId(location2.getUuid()).matchingDegree(matchDegree).build();
    }
}
