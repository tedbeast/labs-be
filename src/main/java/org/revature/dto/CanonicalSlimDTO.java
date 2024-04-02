package org.revature.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class CanonicalSlimDTO {
    public long id;
    public String name;
    public Timestamp lastTimeChecked;
}
