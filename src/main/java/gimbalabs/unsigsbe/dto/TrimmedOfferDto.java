package gimbalabs.unsigsbe.dto;

import lombok.Data;

@Data
public class TrimmedOfferDto {
    public String txHash;
    public Integer txIndex;
    private String unsigId;
    private String owner;
    private Long amount;
}
