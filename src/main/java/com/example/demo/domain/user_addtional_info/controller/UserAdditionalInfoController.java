package com.example.demo.domain.user_addtional_info.controller;

import com.example.demo.domain.user_addtional_info.domain.dto.request.CreateUserAdditionalInfoRequest;
import com.example.demo.domain.user_addtional_info.domain.dto.response.UserAdditionalInfoResponse;
import com.example.demo.domain.user_addtional_info.service.UserAdditionalInfoService;
import com.example.demo.global.aop.AssignUserId;
import com.example.demo.global.base.dto.ResponseBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.base.dto.ResponseUtil.createSuccessResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userAdditionalInfos")
public class UserAdditionalInfoController {

    private final UserAdditionalInfoService userAdditionalInfoService;

    /**
     * 기본 사용자 정보 확인 api
     * 존재하지 않으면 404
     * 존재하면 UserAdditionalInfoResponse
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ResponseBody<UserAdditionalInfoResponse>> getUserAdditionalInfo(Long userId) {
        return ResponseEntity.ok(createSuccessResponse(userAdditionalInfoService.getUserAdditionalInfo(userId)));
    }

    /**
     * 사용자 추가 정보 생성 api
     * 존재하면 404
     */
    @AssignUserId
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/me")
    public ResponseEntity<ResponseBody<Void>> createUserAdditionalInfo(Long userId,
                                                                       @RequestBody @Valid CreateUserAdditionalInfoRequest request) {
        userAdditionalInfoService.createUserAdditionalInfo(userId, request);
        return ResponseEntity.ok(createSuccessResponse());
    }
}
