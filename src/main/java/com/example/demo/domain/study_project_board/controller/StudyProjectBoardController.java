package com.example.demo.domain.study_project_board.controller;

import com.example.demo.domain.board.domain.dto.vo.Status;
import com.example.demo.domain.study_project_board.domain.dto.request.StudyProjectBoardInfoAndFormRequest;
import com.example.demo.domain.study_project_board.domain.dto.response.*;
import com.example.demo.domain.study_project_board.domain.dto.vo.StudyProjectBoardType;
import com.example.demo.domain.study_project_board.service.StudyProjectBoardService;
import com.example.demo.global.aop.AssignUserId;
import com.example.demo.global.base.dto.ResponseBody;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.example.demo.global.base.dto.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/api/v1/study-project-boards")
@RequiredArgsConstructor
public class StudyProjectBoardController {
    private final StudyProjectBoardService studyProjectBoardService;
    private final Validator validator;

    // TODO : 스터디, 프로젝트 게시물 작성 권한 수정 -> 유저 인적사항 작성 시 작성 권한 획득

    // TODO : 마감기한이 지한 게시물은 삭제 처리?
    // TODO : 게시물이 수정되어 질문이 변경된다면, 이미 신청한 신청자들은 어떻게 되는가? -> 신청자들에게 알림을 주는 서비스?
    // TODO : 신청을 눌렀을 때, 질문말고 사용자가 입력할 소요가 있나? -> 인적사항이 그대로 기입되는가? 변경은 불가능한가? -> 그러면 인적사항을 보여주는 창이 굳이 필요한가?

    /**
     * 게시물 저장 및 임시저장 API
     *
     * @param : status[published, draft]
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping()
    public ResponseEntity<ResponseBody<StudyProjectBoardInfoAndFormResponse>> createStudyProjectBoardAndForm(
            Long userId,
            @RequestParam String status,
            @RequestBody StudyProjectBoardInfoAndFormRequest studyProjectBoardInfoAndFormRequest) throws MethodArgumentNotValidException {
        Status bordStatus = Status.valueOf(status.toUpperCase());
        validateStudyProjectBoardInfoAndFormRequest(bordStatus, studyProjectBoardInfoAndFormRequest);
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.saveBoardAndForm(userId, studyProjectBoardInfoAndFormRequest, bordStatus)));
    }

    /**
     * 홈 화면 스터디, 프로젝트 Published 게시물 리스트 조회 API(No-Offset)
     *
     * @param : size(페이징 사이즈), lastBoardId(전 페이지 마지막 게시물 Id), boardType[study, project]
     */
    @GetMapping("/no-offset")
    public ResponseEntity<ResponseBody<StudyProjectBoardNoOffsetResponse>> getStudyProjectBoardListByNoOffset(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lastBoardId,
            @RequestParam String boardType
    ) {
        // TODO : 차단 기능 추가
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getPublishedBoardListByNoOffset(size, lastBoardId, StudyProjectBoardType.valueOf(boardType.toUpperCase()))));
    }

    /**
     * 더보기 스터디, 프로젝트 Published 게시물 리스트 조회 API(PageNum)
     *
     * @param : size, page, boardType[study, project]
     */
    @GetMapping("/page-num")
    public ResponseEntity<ResponseBody<StudyProjectBoardPageNumResponse>> getStudyProjectBoardListByPageNum(
            @PageableDefault(page = 0, size = 10, sort = "recruitmentDeadline", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam String boardType
    ) {
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getPublishedBoardListByPageNum(pageable, StudyProjectBoardType.valueOf(boardType.toUpperCase()))));
    }

    /**
     * 스터디, 프로젝트 게시물 상세조회 API
     */
    @GetMapping("/{studyProjectBoardId}/board")
    public ResponseEntity<ResponseBody<StudyProjectBoardInfoResponse>> getStudyProjectBoardInfo(@PathVariable Long studyProjectBoardId) {
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getBoardInfo(studyProjectBoardId)));
    }

    /**
     * 스터디, 프로젝트 신청폼 상세조회 API
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{studyProjectBoardId}/form")
    public ResponseEntity<ResponseBody<List<StudyProjectFormQuestionResponse>>> getStudyProjectFormInfo(@PathVariable Long studyProjectBoardId) {
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getFormInfoList(studyProjectBoardId)));
    }

    /**
     * 스터디, 프로젝트 게시물 및 신청폼 수정 API
     *
     * @param : status[published, draft]
     */
    // 게시물 작성 전 임시저장 게시물을 불러온 후 저장하면 해당 API 요청
    // TODO : 신청이 들어오면 수정못하도록
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PatchMapping("/{studyProjectBoardId}")
    public ResponseEntity<ResponseBody<StudyProjectBoardInfoAndFormResponse>> updateStudyProjectBoardAndForm(
            Long userId,
            @PathVariable Long studyProjectBoardId,
            @RequestParam String status,
            @RequestBody StudyProjectBoardInfoAndFormRequest studyProjectBoardInfoAndFormRequest) throws MethodArgumentNotValidException {
        Status bordStatus = Status.valueOf(status.toUpperCase());
        validateStudyProjectBoardInfoAndFormRequest(bordStatus, studyProjectBoardInfoAndFormRequest);
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.updateBoardAndForm(userId, studyProjectBoardId, studyProjectBoardInfoAndFormRequest, bordStatus)));

    }

    /**
     * 스터디, 프로젝트 게시물 및 신청폼 삭제 API
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{studyProjectBoardId}")
    public ResponseEntity<ResponseBody<Void>> deleteStudyProjectBoardAndForm(
            Long userId,
            @PathVariable Long studyProjectBoardId) {
        studyProjectBoardService.deleteBoardAndForm(userId, studyProjectBoardId);
        return ResponseEntity.ok(createSuccessResponse());
    }

    /**
     * 최근 임시저장 게시물 get
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/draft/latest")
    public ResponseEntity<ResponseBody<StudyProjectBoardInfoAndFormResponse>> getDraftStudyProjectBoard(
            Long userId) {
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getLatestDraftBoardAndForm(userId)));
    }

    /**
     * 사용자의 임시저장 게시물 목록 get(No-Offset)
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/draft")
    public ResponseEntity<ResponseBody<StudyProjectBoardNoOffsetResponse>> getDraftStudyProjectBoardList(
            Long userId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lastBoardId) {
        return ResponseEntity.ok(createSuccessResponse(studyProjectBoardService.getDraftBoardListByUserId(userId, size, lastBoardId)));
    }

    /**
     * 사용자가 작성한 글 리스트 조회(PageNum)
     *
     * @param : size, page, boardType[study, project]
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/my-boards")
    public ResponseEntity<ResponseBody<StudyProjectBoardPageNumResponse>> getPublishedUserStudyProjectBoardList(
            Long userId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam String boardType) {
        return ResponseEntity.ok(createSuccessResponse(
                studyProjectBoardService.getPublishedBoardListByUserId(userId, pageable, StudyProjectBoardType.valueOf(boardType.toUpperCase()))));
    }

    // Valid 검사 메서드
    // 임시저장은 valid 검사를 하지 않음
    private void validateStudyProjectBoardInfoAndFormRequest(Status status, @Valid StudyProjectBoardInfoAndFormRequest request) throws MethodArgumentNotValidException {
        switch (status) {
            case PUBLISHED -> {
                Set<ConstraintViolation<StudyProjectBoardInfoAndFormRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    BindingResult bindingResult = new BeanPropertyBindingResult(request, "studyProjectBoardInfoAndFormRequest");
                    for (ConstraintViolation<StudyProjectBoardInfoAndFormRequest> violation : violations) {
                        bindingResult.addError(new ObjectError("studyProjectBoardInfoAndFormRequest", violation.getMessage()));
                    }
                    throw new MethodArgumentNotValidException(null, bindingResult);
                }
            }
            case DRAFT -> {
                return;
            }
        }
    }
}
