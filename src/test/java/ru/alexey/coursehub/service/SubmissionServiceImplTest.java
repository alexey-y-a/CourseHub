package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.dto.request.GradeSubmissionRequest;
import ru.alexey.coursehub.dto.request.SubmitAssignmentRequest;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Submission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.entity.enums.SubmissionStatus;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.repository.AssignmentRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.SubmissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Test
    void submitsSolutionAndNotifiesTeacher() {
        Assignment assignment = assignmentWithMaxScore(100);
        User student = new User();
        student.setId(2L);
        student.setName("Anna");
        student.setRole(Role.STUDENT);

        when(assignmentRepository.findByIdWithLesson(10L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission submission = invocation.getArgument(0);
            submission.setId(77L);
            return submission;
        });

        Submission submission = submissionService.submit(10L, 2L, new SubmitAssignmentRequest("answer"));

        assertThat(submission.getId()).isEqualTo(77L);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submission.getContent()).isEqualTo("answer");
        verify(notificationService).notify(any(), any(), any());
    }

    @Test
    void rejectsSecondSubmission() {
        Assignment assignment = assignmentWithMaxScore(100);
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);

        when(assignmentRepository.findByIdWithLesson(10L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.submit(10L, 2L, new SubmitAssignmentRequest("answer")))
                .isInstanceOf(ConflictException.class);

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void gradesSubmissionAndSetsStatus() {
        Assignment assignment = assignmentWithMaxScore(100);
        User student = new User();
        student.setId(2L);
        Submission submission = new Submission();
        submission.setId(77L);
        submission.setAssignment(assignment);
        submission.setStudent(student);

        when(submissionRepository.findById(77L)).thenReturn(Optional.of(submission));

        Submission graded = submissionService.grade(77L, new GradeSubmissionRequest(90, "good"));

        assertThat(graded.getScore()).isEqualTo(90);
        assertThat(graded.getFeedback()).isEqualTo("good");
        assertThat(graded.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        verify(notificationService).notify(any(), any(), any());
    }

    @Test
    void rejectsScoreAboveMaxScore() {
        Assignment assignment = assignmentWithMaxScore(50);
        Submission submission = new Submission();
        submission.setId(77L);
        submission.setAssignment(assignment);
        submission.setStudent(new User());

        when(submissionRepository.findById(77L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.grade(77L, new GradeSubmissionRequest(60, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds max score");
    }

    private Assignment assignmentWithMaxScore(int maxScore) {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);
        Course course = new Course();
        course.setId(1L);
        course.setTeacher(teacher);
        Module module = new Module();
        module.setId(1L);
        module.setCourse(course);
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setModule(module);
        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setLesson(lesson);
        assignment.setTitle("Homework");
        assignment.setMaxScore(maxScore);
        return assignment;
    }
}
