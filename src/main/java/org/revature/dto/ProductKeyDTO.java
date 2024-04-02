package org.revature.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class ProductKeyDTO {
    private long id;
    private boolean admin;
    private boolean superAdmin;
    private boolean active;
    private String name;
    private int batchId;
}
