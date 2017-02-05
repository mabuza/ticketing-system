package system.ticketing.utilities.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import system.ticketing.utilities.util.RefGen;

import java.math.BigDecimal;

/**
 * Created by Ncube on 2/5/17.
 */
@Builder
@Getter
@ToString
public class ApplicableTariffDTO {
    private String generatedRef;
    private String name;
    private String type;
    private String sourceAccount;
    private String targetAccount;
    private BigDecimal value;
    private long remainingAmt;
    private String valueType;
    private String unitType;
    private String narrative;

    @JsonCreator
    public ApplicableTariffDTO(
            @JsonProperty("generatedRef") String generatedRef,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("sourceAccount") String sourceAccount,
            @JsonProperty("targetAccount") String targetAccount,
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("remainingAmt") long remainingAmt,
            @JsonProperty("valueType") String valueType,
            @JsonProperty("unitType") String unitType,
            @JsonProperty("narrative") String narrative) {
        this.generatedRef = generatedRef;
        this.name = name;
        this.type = type;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.value = value;
        this.remainingAmt = remainingAmt;
        this.valueType = valueType;
        this.unitType = unitType;
        this.narrative = narrative;
    }

    public String getGeneratedRef() {
        if (generatedRef == null) {
            generatedRef = RefGen.getReference("C");
        }
        return generatedRef;
    }
}
