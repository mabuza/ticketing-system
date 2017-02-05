package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.util.GenerateKey;

import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class ActivityDTO implements Jsonable{
    private String id;
    private String name;
    private String logged;

    public ActivityDTO(){}

    @JsonCreator
    public ActivityDTO(@JsonProperty("id") String id,
                       @JsonProperty("name") String name,
                       @JsonProperty("logged") String logged){
        this.id = Optional.ofNullable(id).orElseGet(() -> GenerateKey.generateEntityId());
        this.name = Preconditions.checkNotNull(name, "name");
        this.logged = Preconditions.checkNotNull(logged, "logged");
    }
}
