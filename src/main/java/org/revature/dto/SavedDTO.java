package org.revature.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class SavedDTO {
    private long id;
    private long productKey;
    private long canonical;
    private Timestamp lastUpdated;
    private byte[] data;
}
