package org.revature.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class SavedSlimDTO {
    private long id;
    private long productKey;
    private long canonical;
    private Timestamp lastUpdated;
}
