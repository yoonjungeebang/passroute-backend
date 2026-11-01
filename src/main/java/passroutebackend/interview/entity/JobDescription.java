package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_descriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobDescription {

    @Id
    private String url;

    private String source;

    private String companyName;

    private String companyNameNorm;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String document;
}
