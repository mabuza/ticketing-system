package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class SearchDTO implements Jsonable{
    private String searchString;

    public SearchDTO(){}

    @JsonCreator
    public SearchDTO(
            @JsonProperty("searchString") String searchString
    ){
        this.searchString = searchString;
    }
}
