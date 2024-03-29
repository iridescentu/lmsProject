package project.lms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.lms.dto.CourseHistoryDto;
import project.lms.dto.ResponseDto;
import project.lms.enumstatus.ResultCode;
import project.lms.exception.InvalidRequestException;
import project.lms.model.ContentHistory;
import project.lms.model.Course;
import project.lms.model.CourseHistory;
import project.lms.model.ExamHistory;
import project.lms.model.Member;
import project.lms.repository.ContentHistoryRepository;
import project.lms.repository.CourseHistoryRepository;
import project.lms.repository.CourseRepository;
import project.lms.repository.ExamHistoryRepository;
import project.lms.repository.MemberRepository;
import project.lms.service.CourseHistoryService;
import project.lms.util.SecurityUtil;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourseHistoryServiceImpl implements CourseHistoryService {
	
	private final CourseHistoryRepository courseHistoryRepository;
	private final CourseRepository courseRepository;
	private final ContentHistoryRepository contentHistoryRepository;
	private final MemberRepository memberRepository;
	private final ExamHistoryRepository examHistoryRepository;
	
	@Autowired
	public CourseHistoryServiceImpl(CourseHistoryRepository courseHistoryRepository, CourseRepository courseRepository, ContentHistoryRepository contentHistoryRepository, MemberRepository memberRepository, ExamHistoryRepository examHistoryRepository) {
		super();
		this.courseHistoryRepository = courseHistoryRepository;
		this.courseRepository = courseRepository;
		this.contentHistoryRepository = contentHistoryRepository;
		this.memberRepository = memberRepository;
		this.examHistoryRepository = examHistoryRepository;
	}

	// 전체 조회
	@Override
	public ResponseDto<List<CourseHistory>> getAllCourseHistories(){
		List<CourseHistory> courseHistories = courseHistoryRepository.findAll();
		return new ResponseDto<>(
				ResultCode.SUCCESS.name(),
				courseHistories,
				"모든 courseHistory를 조회하였습니다.");
	}
	
	// 강의별 조회
	@Override
	public ResponseDto<List<CourseHistory>> getCourseHistoriesByCourse(Long courseId){
		List<CourseHistory> courseHistories = courseHistoryRepository.findByCourseCourseId(courseId);
		return new ResponseDto<>(
				ResultCode.SUCCESS.name(),
				courseHistories,
				"courseHistory를 course에 따라 조회하였습니다.");
	}
	
	// 로그인 유저의 조회
	private Member getCurrentUser() {
        String username = SecurityUtil.getCurrentloginId()
                .orElseThrow(() -> new InvalidRequestException("not found username", "현재 해당 사용자를 찾을 수 없습니다."));
        
        return memberRepository.findByLoginId(username);
    }
	
	// 로그인 유저가 수강 중인 CourseHistory 조회
	public ResponseDto<List<CourseHistory>> getMyCourseHistories() {
		Member member = getCurrentUser();
		List<CourseHistory> courseHistories = courseHistoryRepository.findByMember(member);
		
		return new ResponseDto<>(
				ResultCode.SUCCESS.name(),
				courseHistories,
				"로그인한 사용자가 수강 중인 courseHistory를 조회하였습니다.");
	}
	
	// 수료증 자격 업데이트
    public ResponseDto<CourseHistoryDto> updateCourseHistoryStatus(Long courseHistoryId) {
        CourseHistory courseHistory = courseHistoryRepository.findById(courseHistoryId)
                .orElseThrow(() -> new InvalidRequestException("not found courseHistory", "courseHistory를 찾을 수 없습니다."));
        Long courseId = courseHistory.getCourse().getCourseId();
        Long memberId = courseHistory.getMember().getMemberId();

        Long totalContents = courseRepository.countContentsByCourseId(courseId);
        Long completedContents = contentHistoryRepository.countByMemberMemberIdAndIsCompletedTrue(memberId);

        // 강의별 시험 이력 조회
        List<ExamHistory> examHistories = examHistoryRepository.findByMember_MemberId(memberId);
        boolean isExamCompleted = examHistories.stream()
                .filter(examHistory -> examHistory.getExam().getContent().getCourse().getCourseId().equals(courseId)) 
                .allMatch(ExamHistory::isExamCompletionStatus);

        // 강의별 컨텐츠 이력 조회
        List<ContentHistory> contentHistories = contentHistoryRepository.findByContent_Course_CourseIdAndMember_MemberId(courseId, memberId);
        boolean isAllContentCompleted = contentHistories.stream().allMatch(ContentHistory::getIsCompleted);

        CourseHistoryDto courseHistoryDto = new CourseHistoryDto();
        courseHistoryDto.setCourseHistory(courseHistory);
        courseHistoryDto.setTotalContents(totalContents);
        courseHistoryDto.setCompletedContents(completedContents);

        if (totalContents.equals(completedContents) && isExamCompleted && isAllContentCompleted) {
            courseHistory.setContentStatus(true);
            courseHistoryRepository.save(courseHistory);
            return new ResponseDto<>(
                    ResultCode.SUCCESS.name(),
                    courseHistoryDto,
                    "CourseHistory의 status가 업데이트되었습니다."
                );
        } else {
            return new ResponseDto<>(
                    ResultCode.ERROR.name(),
                    null,
                    "CourseHistory의 status 업데이트에 실패하였습니다."
                );
        }
    }
}

//    private final CourseHistoryRepository courseHistoryRepository;
//
//    @Autowired
//    public CourseHistoryServiceImpl(CourseHistoryRepository courseHistoryRepository) {
//        this.courseHistoryRepository = courseHistoryRepository;
//    }
//
//    // 특정 회원의 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getCourseHistoryByMember(Member member) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByMember(member);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "회원의 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("회원의 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 강좌의 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getCourseHistoryByCourse(Course course) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByCourse(course);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "강좌의 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("강좌의 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 날짜 범위 내의 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getCourseHistoryInDateRange(LocalDate startDate, LocalDate endDate) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByStartDateBetween(startDate, endDate);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "날짜 범위 내의 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("날짜 범위 내의 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 회원과 강좌의 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getCourseHistoryByMemberAndCourse(Member member, Course course) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByMemberAndCourse(member, course);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "회원과 강좌의 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("회원과 강좌의 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 종료일이 null인(아직 종료되지 않은) 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getOngoingCourseHistory() {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByEndDateIsNull();
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "아직 종료되지 않은 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("아직 종료되지 않은 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 날짜 이후에 종료된 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getCourseHistoryEndedAfter(LocalDate date) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByEndDateAfter(date);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "특정 날짜 이후에 종료된 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("특정 날짜 이후에 종료된 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 회원의 가장 최근 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<CourseHistory> getLatestCourseHistoryForMember(Member member) {
//        try {
//            CourseHistory latestCourseHistory = courseHistoryRepository.findTopByMemberOrderByStartDateDesc(member);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), latestCourseHistory, "회원의 가장 최근 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("회원의 가장 최근 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 특정 강좌의 수강 이력 개수 조회
//    @Transactional
//    @Override
//    public ResponseDto<Long> getCourseHistoryCountByCourse(Course course) {
//        try {
//            long count = courseHistoryRepository.countByCourse(course);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), count, "강좌의 수강 이력 개수를 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("강좌의 수강 이력 개수 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//
//    // 종료일이 null이고 시작일이 특정 날짜 이전인 수강 이력 조회
//    @Transactional
//    @Override
//    public ResponseDto<List<CourseHistory>> getOngoingCourseHistoryBeforeDate(LocalDate date) {
//        try {
//            List<CourseHistory> courseHistories = courseHistoryRepository.findByEndDateIsNullAndStartDateBefore(date);
//            return new ResponseDto<>(ResultCode.SUCCESS.name(), courseHistories, "특정 날짜 이전에 시작되고 아직 종료되지 않은 수강 이력을 성공적으로 조회하였습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InvalidRequestException("특정 날짜 이전에 시작되고 아직 종료되지 않은 수강 이력 조회 중 오류가 발생했습니다.", e.getMessage());
//        }
//    }
//}