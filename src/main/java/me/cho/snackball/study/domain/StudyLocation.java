package me.cho.snackball.study.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.location.domain.UserLocation;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String city;

    @Override
    public String toString() {
        return String.format("%s / %s", province, city);
    }

    //==생성 메서드==//
    public static StudyLocation create(Study study, Location location) {
        StudyLocation studyLocation = new StudyLocation();
        studyLocation.study = study;
        studyLocation.location = location;
        studyLocation.province = location.getProvince();
        studyLocation.city = location.getCity();
        study.getStudyLocations().add(studyLocation);

        return studyLocation;
    }
}
