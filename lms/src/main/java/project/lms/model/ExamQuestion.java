package project.lms.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "examQuestions")
public class ExamQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long examQuestionId;

	@ManyToOne
	@JoinColumn(name = "examId", nullable = false)
	private Exam exam;
	
	private String questionParagraph;
	
	@Column(nullable = false)
	private String questionText;
	
	@ElementCollection
    @Column(nullable = false)
	private List<String> options;
	
	@Column(nullable = false)
	private Integer correctOptionIndex;

	public ExamQuestion() {
		super();
	}

	public ExamQuestion(Long examQuestionId, Exam exam, String questionParagraph, String questionText,
			List<String> options, Integer correctOptionIndex) {
		super();
		this.examQuestionId = examQuestionId;
		this.exam = exam;
		this.questionParagraph = questionParagraph;
		this.questionText = questionText;
		this.options = options;
		this.correctOptionIndex = correctOptionIndex;
	}

	public Long getExamQuestionId() {
		return examQuestionId;
	}

	public void setExamQuestionId(Long examQuestionId) {
		this.examQuestionId = examQuestionId;
	}

	public Exam getExam() {
		return exam;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public String getQuestionParagraph() {
		return questionParagraph;
	}

	public void setQuestionParagraph(String questionParagraph) {
		this.questionParagraph = questionParagraph;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public Integer getCorrectOptionIndex() {
		return correctOptionIndex;
	}

	public void setCorrectOptionIndex(Integer correctOptionIndex) {
		this.correctOptionIndex = correctOptionIndex;
	}
	
}