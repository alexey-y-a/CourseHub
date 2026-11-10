package ru.alexey.coursehub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "quiz_answers",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_quiz_answers_submission_question",
                        columnNames = {"quiz_submission_id", "question_id"}),
        indexes = @Index(name = "idx_quiz_answers_option", columnList = "selected_option_id"))
public class QuizAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "quiz_submission_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_answers_submission"))
    private QuizSubmission quizSubmission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quiz_answers_question"))
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "selected_option_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_answers_option"))
    private AnswerOption selectedOption;
}
