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
public class Saved {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JoinColumn(name="canonical_fk")
    private Canonical canonical;
    @ManyToOne
    @JoinColumn(name="productkey_fk")
    private PKey pKey;
    private Timestamp lastUpdated;
    @Transient
    private byte[] data;
}
