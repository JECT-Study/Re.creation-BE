package org.ject.recreation.core.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.ject.recreation.core.api.controller.request.BlockUserRequestDto;
import org.ject.recreation.core.api.controller.request.GameDeleteRequestDto;
import org.ject.recreation.core.api.controller.response.GameListResponseDto;
import org.ject.recreation.core.api.controller.response.GetAllUserResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameResponseDto;
import org.ject.recreation.core.domain.admin.AdminService;
import org.ject.recreation.core.support.response.ApiResponse;
import org.ject.recreation.core.support.response.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/games")
    public ApiResponse<PageResponseDto<ReportGameResponseDto>> getReportGames(
            @RequestParam(defaultValue = "0") int page) {
        return ApiResponse.success(adminService.getReportedGames(page));
    }

    // report 상세 내역 조회
    @GetMapping("games/{reportId}")
    public ApiResponse<ReportGameDetailResponseDto> getReportGame(
            @PathVariable Long reportId
    ){
        return ApiResponse.success(adminService.getReportedDetailGames(reportId));
    }

    @PostMapping("/games/delete")
    public ApiResponse<Void> deleteReportGame(@RequestBody GameDeleteRequestDto gameDeleteRequestDto){
        return ApiResponse.success(adminService.deleteReportedDetailGames(gameDeleteRequestDto));
    }


    @GetMapping("games/admin")
    public ApiResponse<PageResponseDto<GameListResponseDto.GameDto>> getAdminGames(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ApiResponse.success(adminService.getAdminGames(page));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponseDto<GetAllUserResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ApiResponse.success(adminService.getAllUsers(page));
    }

    @PostMapping("/users/block")
    public ApiResponse<Void> blockUser(@RequestBody BlockUserRequestDto blockUserRequestDto){
        return ApiResponse.success(adminService.blockUser(blockUserRequestDto));
    }

    @PostMapping("/users/unblock")
    public ApiResponse<Void> unBlockUser(@RequestBody BlockUserRequestDto blockUserRequestDto) {
        return ApiResponse.success(adminService.unBlockUser(blockUserRequestDto));
    }
}
