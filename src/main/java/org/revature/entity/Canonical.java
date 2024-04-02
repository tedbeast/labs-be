package org.revature.entity;

import lombok.*;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class Canonical {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String name;
    private Timestamp lastTimeChecked;
    private String commitHash;
    @Transient
    private byte[] data;
    public Canonical(String name, String commitHash, Timestamp lastTimeChecked, byte[] data){
        this.name = name;
        this.commitHash = commitHash;
        this.lastTimeChecked = lastTimeChecked;
        this.data = data;
    }
}
