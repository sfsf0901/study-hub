package me.cho.snackball.settings.location.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String province;

    @Column(nullable = false)
    private String city;
    
    @Override
    public String toString() {
        return String.format("%s / %s", province, city);
    }
}
