package org.revature.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class PKey {
    @Id
    private long id;
    private boolean admin;
    private boolean superAdmin;
    private boolean active;
    private String name;
    private int batchId;
    @OneToMany(fetch = FetchType.LAZY)
    private List<Saved> saveds;

}
