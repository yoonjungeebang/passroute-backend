package passroutebackend.selfintro.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "si_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfIntroItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "si_id", nullable = false)
    private SelfIntro selfIntro;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    public static SelfIntroItem create(SelfIntro selfIntro, String questionText, String answerText, int orderNum) {
        SelfIntroItem item = new SelfIntroItem();
        item.selfIntro = selfIntro;
        item.questionText = questionText;
        item.answerText = answerText;
        item.orderNum = orderNum;
        return item;
    }
}