package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class PasswordDTO implements Jsonable{
    private String profileId;
    private String oldPassword;
    private String userName;
    private String newPassword;
    private String confirmPassword;

    public PasswordDTO(){}

    @JsonCreator
    public PasswordDTO(
            @JsonProperty("profileId") String profileId,
            @JsonProperty("oldPassword") String oldPassword,
            @JsonProperty("userName") String userName,
            @JsonProperty("newPassword") String newPassword,
            @JsonProperty("confirmPassword") String confirmPassword
    ){
        this.profileId = profileId;
        this.oldPassword = oldPassword;
        this.userName = userName;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
